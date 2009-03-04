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


import static org.hamcrest.CoreMatchers.*;

import javax.sql.ConnectionEventListener;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Test;
import org.mockito.Mockito;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * When a Connection has been idling for too long, the database server
 * may decide to close it.
 * This may cause close() to throw an SQLException and we need to handle that
 * correctly - that is, in a way that makes sense.
 * @author cvh
 */
public class DeadConnectionsTest extends NanoPoolTestBase {
    private final AtomicBoolean throwOnPcClose = new AtomicBoolean();
    private final AtomicLong time = new AtomicLong();

    @Test
    public void deadConnectionsMustNeverFoilGetConnection() throws SQLException, InterruptedException {
        pool = npds();
        Connection con = pool.getConnection();
        con.close();
        time.incrementAndGet();
        throwOnPcClose.set(true);
        
        // this should not throw, because a failure to close PC is meaningless
        // when we're just going to throw it away and create a new one anyway.
        con = pool.getConnection();
        con.close();
    }

    @Test
    public void deadConnectionsMustNeverPropegateSQLExceptionsOnClose() throws SQLException {
        pool = npds();
        Connection con = pool.getConnection();

        time.incrementAndGet();
        throwOnPcClose.set(true);

        // this should not throw because why on earth would an application
        // care about what happens to the physical connection?
        con.close();
    }

    @Override
    protected Configuration buildConfig() {
        TimeSource t = new MilliTime() {
            @Override
            public long now() {
                return time.get();
            }
        };
        return super.buildConfig()
                .setTimeToLive(0)
                .setPoolSize(1)
                .setTimeSource(t);
    }

    @Override
    protected ConnectionPoolDataSource buildCpds() throws SQLException {
        final AtomicReference<ConnectionEventListener> cel = new AtomicReference();

        Connection con = Mockito.mock(Connection.class);
        Mockito.doAnswer(new Answer() {
            public Object answer(InvocationOnMock invocation) throws Throwable {
                cel.get().connectionClosed(null);
                return null;
            }
        }).when(con).close();

        PooledConnection pc = Mockito.mock(PooledConnection.class);
        Mockito.doReturn(con).when(pc).getConnection();
        Mockito.doAnswer(new Answer() {
            public Object answer(InvocationOnMock invocation) throws Throwable {
                if (throwOnPcClose.get()) {
                    throwOnPcClose.set(false);
                    throw new SQLException("PooledConnection boom.");
                }
                return null;
            }
        }).when(pc).close();
        Mockito.doAnswer(new Answer() {
            public Object answer(InvocationOnMock invocation) throws Throwable {
                cel.set((ConnectionEventListener)invocation.getArguments()[0]);
                return null;
            }
        }).when(pc).addConnectionEventListener(Mockito.argThat(is(any(ConnectionEventListener.class))));

        ConnectionPoolDataSource cpds = Mockito.mock(ConnectionPoolDataSource.class);
        Mockito.doReturn(pc).when(cpds).getPooledConnection();
        return cpds;
    }
}
