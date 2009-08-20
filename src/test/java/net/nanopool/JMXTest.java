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
import static org.junit.Assert.*;

import java.sql.SQLException;
import org.junit.Test;

/**
 * 
 * @author cvh
 */
public class JMXTest extends NanoPoolTestBase {
  @Test
  public void variousCounters() throws SQLException {
    pool = npds();
    NanoPoolManagementMBean mbean = pool.getMXBean();
    
    assertEquals(0, mbean.getConnectionsCreated());
    assertEquals(0, mbean.getConnectionsLeased());
    assertEquals(1, mbean.getCurrentAvailableConnectionsCount());
    assertEquals(0, mbean.getCurrentLeasedConnectionsCount());
    assertFalse(mbean.isShutDown());
    
    Connection con = pool.getConnection();
    
    assertEquals(1, mbean.getConnectionsCreated());
    assertEquals(1, mbean.getConnectionsLeased());
    assertEquals(0, mbean.getCurrentAvailableConnectionsCount());
    assertEquals(1, mbean.getCurrentLeasedConnectionsCount());
    assertFalse(mbean.isShutDown());
    
    con.close();
    
    assertEquals(1, mbean.getConnectionsCreated());
    assertEquals(1, mbean.getConnectionsLeased());
    assertEquals(1, mbean.getCurrentAvailableConnectionsCount());
    assertEquals(0, mbean.getCurrentLeasedConnectionsCount());
    assertFalse(mbean.isShutDown());
    
    con = pool.getConnection();
    
    assertEquals(1, mbean.getConnectionsCreated());
    assertEquals(2, mbean.getConnectionsLeased());
    assertEquals(0, mbean.getCurrentAvailableConnectionsCount());
    assertEquals(1, mbean.getCurrentLeasedConnectionsCount());
    assertFalse(mbean.isShutDown());
    
    con.close();
    
    assertEquals(1, mbean.getConnectionsCreated());
    assertEquals(2, mbean.getConnectionsLeased());
    assertEquals(1, mbean.getCurrentAvailableConnectionsCount());
    assertEquals(0, mbean.getCurrentLeasedConnectionsCount());
    assertFalse(mbean.isShutDown());
    
    pool.close();
    
    assertEquals(1, mbean.getConnectionsCreated());
    assertEquals(2, mbean.getConnectionsLeased());
    assertEquals(0, mbean.getCurrentAvailableConnectionsCount());
    assertEquals(0, mbean.getCurrentLeasedConnectionsCount());
    assertTrue(mbean.isShutDown());
  }
  
  @Test
  public void resettingMustWorkForBothLiveAndShutDownPools()
      throws SQLException {
    pool = npds();
    NanoPoolManagementMBean mbean = pool.getMXBean();
    Connection con = pool.getConnection();
    con.close();
    
    assertEquals(1, mbean.getConnectionsCreated());
    assertEquals(1, mbean.getConnectionsLeased());
    assertEquals(1, mbean.getCurrentAvailableConnectionsCount());
    assertEquals(0, mbean.getCurrentLeasedConnectionsCount());
    assertFalse(mbean.isShutDown());
    
    mbean.resetCounters();
    
    assertEquals(0, mbean.getConnectionsCreated());
    assertEquals(0, mbean.getConnectionsLeased());
    assertEquals(1, mbean.getCurrentAvailableConnectionsCount());
    assertEquals(0, mbean.getCurrentLeasedConnectionsCount());
    assertFalse(mbean.isShutDown());
    
    con = pool.getConnection();
    con.close();
    mbean.shutDown();
    
    assertEquals(0, mbean.getConnectionsCreated());
    assertEquals(1, mbean.getConnectionsLeased());
    assertEquals(0, mbean.getCurrentAvailableConnectionsCount());
    assertEquals(0, mbean.getCurrentLeasedConnectionsCount());
    assertTrue(mbean.isShutDown());
    
    mbean.resetCounters();
    
    assertEquals(0, mbean.getConnectionsCreated());
    assertEquals(0, mbean.getConnectionsLeased());
    assertEquals(0, mbean.getCurrentAvailableConnectionsCount());
    assertEquals(0, mbean.getCurrentLeasedConnectionsCount());
    assertTrue(mbean.isShutDown());
  }
  
  @Test
  public void jmxInterfaceMustWorkAfterPoolShutDown() throws SQLException {
    pool = npds();
    NanoPoolManagementMBean mbean = pool.getMXBean();
    mbean.shutDown();
    
    Config config = buildSettings().getConfig();
    
    assertEquals(config.ttl, mbean.getConnectionTimeToLive());
    assertEquals(0, mbean.getConnectionsCreated());
    assertEquals(0, mbean.getConnectionsLeased());
    assertNotNull(mbean.getContentionHandler());
    assertEquals(config.contentionHandler.getClass().getName(),
        mbean.getContentionHandlerClassName());
    assertEquals(0, mbean.getCurrentAvailableConnectionsCount());
    assertEquals(0, mbean.getCurrentLeasedConnectionsCount());
    assertEquals(0, mbean.getPoolSize());
    assertNotNull(mbean.getSourceConnection());
    assertNotNull(mbean.getSourceConnectionClassName());
    assertNotNull(mbean.listConnectionOwningThreadsStackTraces());
    mbean.dumpConnectionOwningThreadsStackTraces(); // must not throw
    try {
      mbean.resizePool(2);
      fail("resizing a shut down pool did not throw");
    } catch (IllegalStateException ex) {
      // yay!
    }
    try {
      mbean.interruptConnection(0);
      fail("interrupting connection on closed pool did not throw");
    } catch (IllegalStateException ex) {
      // yay!
    }
    try {
      mbean.killConnection(0);
      fail("killing connection on closed pool did not throw");
    } catch (IllegalStateException ex) {
      // yay!
    }
  }
  
  @Test
  public void operationsMustNotNormallyFail() throws SQLException {
    pool = npds();
    NanoPoolManagementMBean mbean = pool.getMXBean();
    assertFalse(mbean.isShutDown());
    assertNotNull(mbean.listConnectionOwningThreadsStackTraces());
    mbean.resizePool(1);
    mbean.resizePool(2);
    mbean.resizePool(1);
    Connection con = pool.getConnection();
    assertFalse(Thread.interrupted());
    mbean.interruptConnection(0);
    assertTrue(Thread.interrupted());
    try {
      mbean.killConnection(0);
      fail("killing myself did not throw");
    } catch (Throwable th) {
      // yay!
    }
    con.close(); // must not throw
  }
  
  @Override
  protected Settings buildSettings() {
    return super.buildSettings().setPoolSize(1);
  }
}
