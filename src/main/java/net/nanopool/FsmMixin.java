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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import javax.sql.ConnectionPoolDataSource;

import net.nanopool.hooks.EventType;
import net.nanopool.hooks.Hook;

final class FsmMixin {
  static final String MSG_SHUT_DOWN = "Connection pool is shut down.";
  static final String MSG_TOO_SMALL = "Cannot resize. New size too small: ";
  
  private static final CheapRandom rand = new CheapRandom();
  private static final
    AtomicReferenceFieldUpdater<PoolState, Connector[]> connectorsField =
    AtomicReferenceFieldUpdater.newUpdater(
        PoolState.class, Connector[].class, "connectors");
  
  static Connection getConnection(final NanoPoolDataSource pool)
      throws SQLException {
    final PoolState state = pool.state;
    runHooks(state.config.preConnectHooks, EventType.preConnect, state.source,
        null, null);
    for (;;) {
      try {
        return getConnectionOrResize(pool);
      } catch (OutdatedException _) {
        continue;
      }
    }
  }
  
  private static Connection getConnectionOrResize(final NanoPoolDataSource pool)
      throws SQLException {
    final PoolState state = pool.state;
    final Connector[] connectors = state.connectors;
    if (connectors == null) {
      throw new IllegalStateException(MSG_SHUT_DOWN);
    }
    final int poolSize = connectors.length;
    final Config config = state.config;
    final int start = StrictMath.abs(rand.nextInt()) % poolSize;
    int idx = start;
    int contentionCounter = 0;
    for (;;) {
      Connector con = connectors[idx];
      int st = con.state.get();
      while (st != Connector.RESERVED) {
        if (st == Connector.OUTDATED) {
          throw OutdatedException.INSTANCE;
        }
        if (st == Connector.SHUTDOWN) {
          throw new IllegalStateException(MSG_SHUT_DOWN);
        }
        // we might have gotten one - reserve it
        if (con.state.compareAndSet(Connector.AVAILABLE, Connector.RESERVED)) {
          try {
            Connection connection = con.getConnection(config);
            runHooks(config.postConnectHooks, EventType.postConnect,
                state.source, connection, null);
            return connection;
          } catch (SQLException sqle) {
            try {
              con.invalidate();
            } finally {
              con.state.set(Connector.AVAILABLE);
              runHooks(config.postConnectHooks, EventType.postConnect,
                  state.source, null, sqle);
            }
            throw sqle;
          }
        }
        st = con.state.get();
      }
      if (++idx == poolSize) {
        idx = 0;
      }
      if (idx == start) {
        config.contentionHandler.handleContention(++contentionCounter, pool);
        if (connectors != state.connectors) {
          throw OutdatedException.INSTANCE;
        }
      }
    }
  }
  
  static List<SQLException> close(PoolState pool) {
    try {
      Connector[] cons = connectorsField.getAndSet(pool, null);
      return shutdown(cons);
    } catch (OutdatedException _) {
      return close(pool);
    }
  }
  
  private static List<SQLException> shutdown(Connector[] connectors) {
    List<SQLException> caughtExceptions = new ArrayList<SQLException>();
    if (connectors == null) {
      return caughtExceptions;
    }
    
    for (Connector con : connectors) {
      int st = con.state.getAndSet(Connector.SHUTDOWN);
      if (st == Connector.OUTDATED) {
        throw OutdatedException.INSTANCE;
      }
      if (st != Connector.RESERVED) {
        try {
          con.invalidate();
        } catch (SQLException ex) {
          caughtExceptions.add(ex);
        }
      }
    }
    
    return caughtExceptions;
  }
  
  static void resizePool(PoolState pool, int newSize) {
    if (newSize < 1) {
      throw new IllegalArgumentException(MSG_TOO_SMALL + newSize);
    }
    pool.resizingLock.lock();
    try {
      Connector[] ocons = pool.connectors;
      if (ocons == null || ocons[0].state.get() == Connector.SHUTDOWN) {
        throw new IllegalStateException(MSG_SHUT_DOWN);
      }
      if (ocons.length == newSize) {
        return;
      }
      Connector[] ncons = new Connector[newSize];
      if (ocons.length < newSize) {
        // grow pool
        System.arraycopy(ocons, 0, ncons, 0, ocons.length);
        for (int i = ocons.length; i < ncons.length; i++) {
          ncons[i] = new Connector(
              pool.source, pool.config.ttl, pool.config.time);
        }
        if (!connectorsField.compareAndSet(pool, ocons, ncons)) {
          // will only fail if the pool has been shut down
          shutdown(ncons);
        }
      } else {
        // shrink pool
        System.arraycopy(ocons, 0, ncons, 0, newSize);
        // following CAS will only fail if the pool has been shut down
        // if-statement prevents unnecessary OutdatedExceptions in shutdown
        if (connectorsField.compareAndSet(pool, ocons, ncons)) {
          for (int i = newSize; i < ocons.length; i++) {
            ocons[i].state.set(Connector.OUTDATED);
          }
        }
      }
    } finally {
      pool.resizingLock.unlock();
    }
  }
  
  private static int countConnections(Connector[] connectors, int ofState) {
    assert ofState != Connector.OUTDATED && ofState != Connector.SHUTDOWN :
      "Cannot count outdated or shut down state.";
    
    int openCount = 0;
    for (Connector cn : connectors) {
      int state = cn.state.get();
      if (state == Connector.OUTDATED) {
        throw OutdatedException.INSTANCE;
      }
      if (state == ofState) {
        openCount++;
      }
    }
    return openCount;
  }
  
  static int countAvailableConnections(Connector[] cons) {
    return countConnections(cons, Connector.AVAILABLE);
  }
  
  static int countLeasedConnections(Connector[] cons) {
    return countConnections(cons, Connector.RESERVED);
  }
  
  static void runHooks(Cons<Hook> hooks, EventType type,
      ConnectionPoolDataSource source, Connection con, SQLException sqle) {
    while (hooks != null) {
      hooks.first.run(type, source, con, sqle);
      hooks = hooks.rest;
    }
  }
}
