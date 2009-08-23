/*
   Copyright 2008-2009 Christian Vest Hansen

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package net.nanopool;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.List;

/**
 * 
 * @author cvh
 */
class NanoPoolManagement implements NanoPoolManagementMBean {
  private final PoolState pool;
  private volatile int connectionsLeased;
  private volatile int connectionsCreated;
  
  NanoPoolManagement(PoolState pool) {
    this.pool = pool;
  }
  
  public int getCurrentAvailableConnectionsCount() {
    if (pool.connectors == null) {
      return 0;
    }
    try {
      return FsmMixin.countAvailableConnections(pool.connectors);
    } catch (OutdatedException _) {
      return getCurrentAvailableConnectionsCount();
    }
  }
  
  public int getCurrentLeasedConnectionsCount() {
    if (pool.connectors == null) {
      return 0;
    }
    try {
      return FsmMixin.countLeasedConnections(pool.connectors);
    } catch (OutdatedException _) {
      return getCurrentAvailableConnectionsCount();
    }
  }
  
  public int getPoolSize() {
    Connector[] cons = pool.connectors;
    return cons == null ? 0 : cons.length;
  }
  
  public long getConnectionTimeToLive() {
    return pool.config.ttl;
  }
  
  public String getContentionHandlerClassName() {
    try {
      return pool.config.contentionHandler.getClass().getName();
    } catch (NullPointerException npe) {
      return "null";
    }
  }
  
  public String getContentionHandler() {
    return String.valueOf(pool.config.contentionHandler);
  }
  
  public boolean isShutDown() {
    Connector[] cons = pool.connectors;
    if (cons == null) {
      return true;
    }
    for (Connector cn : cons) {
      if (cn.state.get() == Connector.SHUTDOWN) {
        return true;
      }
    }
    return false;
  }
  
  public String shutDown() {
    // cache counters
    getConnectionsCreated();
    getConnectionsLeased();
    // then shut down
    List<SQLException> faults = FsmMixin.close(pool);
    if (faults.size() > 0) {
      StringWriter sos = new StringWriter();
      PrintWriter pout = new PrintWriter(sos);
      for (SQLException ex : faults) {
        ex.printStackTrace(pout);
      }
      pout.close();
      return sos.toString();
    }
    return "Shutdown completed successfully.";
  }
  
  public String getSourceConnectionClassName() {
    try {
      return pool.source.getClass().getName();
    } catch (NullPointerException npe) {
      return "null";
    }
  }
  
  public String getSourceConnection() {
    return String.valueOf(pool.source);
  }
  
  public int getConnectionsCreated() {
    int createdCount = 0;
    Connector[] connectors = pool.connectors;
    if (connectors == null) {
      return connectionsCreated;
    }
    for (Connector cn : connectors) {
      if (cn != null) {
        createdCount += cn.getRealConnectionsCreated();
      }
    }
    return connectionsCreated = createdCount;
  }
  
  public int getConnectionsLeased() {
    int leasedCount = 0;
    Connector[] connectors = pool.connectors;
    if (connectors == null) {
      return connectionsLeased;
    }
    for (Connector cn : connectors) {
      if (cn != null) {
        leasedCount += cn.getConnectionsLeased();
      }
    }
    return connectionsLeased = leasedCount;
  }
  
  public void resetCounters() {
    Connector[] connectors = pool.connectors;
    if (connectors != null) {
      for (Connector cn : connectors) {
        if (cn != null) {
          cn.resetCounters();
        }
      }
    }
    connectionsCreated = 0;
    connectionsLeased = 0;
  }
  
  public String listConnectionOwningThreadsStackTraces() {
    StringBuilder sb = new StringBuilder();
    Connector[] connectors = pool.connectors;
    if (connectors == null) {
      return "Pool is shut down.";
    }
    int i = -1;
    for (Connector cn : connectors) {
      i++;
      if (cn == null) {
        continue;
      }
      Thread owner = cn.getOwner();
      sb.append('[').append(i).append("] ").append(cn);
      if (owner == null) {
        sb.append(" is not currently owned by anyone.\n");
      } else {
        sb.append(" owned by " + owner.getName() + ":\n");
        StackTraceElement[] trace = owner.getStackTrace();
        if (trace.length == 0) {
          sb.append("    No stack trace available.\n");
        }
        for (StackTraceElement frame : trace) {
          sb.append("    ").append(frame).append("\n");
        }
      }
    }
    return sb.toString();
  }
  
  public void dumpConnectionOwningThreadsStackTraces() {
    System.err.print(listConnectionOwningThreadsStackTraces());
  }
  
  public void resizePool(int newSize) {
    FsmMixin.resizePool(pool, newSize);
  }
  
  public void interruptConnection(int id) {
    Connector[] cons = pool.connectors;
    if (cons == null) {
      throw new IllegalStateException(FsmMixin.MSG_SHUT_DOWN);
    }
    Connector cn = cons[id];
    if (cn.state.get() == Connector.SHUTDOWN) {
      throw new IllegalStateException(FsmMixin.MSG_SHUT_DOWN);
    }
    Thread owner = cn.getOwner();
    if (owner != null) {
      owner.interrupt();
    }
  }
  
  @SuppressWarnings("deprecation")
  public void killConnection(int id) {
    Connector[] cons = pool.connectors;
    if (cons == null) {
      throw new IllegalStateException(FsmMixin.MSG_SHUT_DOWN);
    }
    Connector cn = cons[id];
    if (cn.state.get() == Connector.SHUTDOWN) {
      throw new IllegalStateException(FsmMixin.MSG_SHUT_DOWN);
    }
    Thread owner = cn.getOwner();
    if (owner != null) {
      owner.stop();
      try {
        cn.invalidate();
      } catch (SQLException ex) {
        ex.printStackTrace();
      }
      try {
        cn.returnToPool();
      } catch (SQLException ex) {
        ex.printStackTrace();
      }
    }
  }
}
