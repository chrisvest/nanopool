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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * 
 * @author cvh
 */
public class InterruptionTest extends NanoPoolTestBase {
  @Test public void
  mustPreserveInterruptionState() throws SQLException {
    Thread.currentThread().interrupt();
    pool = npds();
    Connection con = pool.getConnection();
    con.close();
    List<SQLException> sqles = pool.close();
    for (SQLException sqle : sqles) {
      sqle.printStackTrace();
    }
    assertTrue(sqles.isEmpty());
    assertTrue(Thread.interrupted());
  }

  @Override
  protected ConnectionPoolDataSource buildCpds() throws SQLException {
    final AtomicReference<ConnectionEventListener> connectionEventListener =
      new AtomicReference<ConnectionEventListener>();
    ConnectionPoolDataSource source = mock(ConnectionPoolDataSource.class);
    final PooledConnection pcon = mock(PooledConnection.class);
    Connection con = mock(Connection.class);
    
    when(source.getPooledConnection()).thenReturn(pcon);
    doAnswer(new Answer<Object>() {
      public Object answer(InvocationOnMock invocation) throws Throwable {
        connectionEventListener.set(
            (ConnectionEventListener) invocation.getArguments()[0]);
        return null;
      }
    }).when(pcon).addConnectionEventListener(
        (ConnectionEventListener) anyObject());
    doAnswer(new Answer<Object>() {
      public Object answer(InvocationOnMock invocation) throws Throwable {
        ConnectionEventListener cel = connectionEventListener.get();
        ConnectionEvent event = new ConnectionEvent(pcon);
        cel.connectionClosed(event);
        return null;
      }
    }).when(con).close();
    
    when(pcon.getConnection()).thenReturn(con);
    
    return source;
  }
}






