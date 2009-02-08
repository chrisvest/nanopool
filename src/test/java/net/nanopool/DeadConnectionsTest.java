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

import java.sql.CallableStatement;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLWarning;
import java.sql.Savepoint;
import java.util.Map;
import javax.sql.ConnectionEventListener;
import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.Test;
import org.mockito.Mockito;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;
import net.nanopool.dummy.DummyConnection;
import net.nanopool.dummy.DummyPooledConnection;

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
        PooledConnection pc = new DummyPooledConnection() {
            ConnectionEventListener cel;
            @Override
            public Connection getConnection() throws SQLException {
                final ConnectionEventListener c = cel;
                return new DummyConnection() {
                    @Override
                    public void close() throws SQLException {
                        c.connectionClosed(null);
                    }
                };
            }
            @Override
            public void close() throws SQLException {
                if (throwOnPcClose.get()) {
                    throwOnPcClose.set(false);
                    throw new SQLException("PooledConnection boom.");
                }
            }
            @Override
            public void addConnectionEventListener(ConnectionEventListener cel) {
                this.cel = cel;
            }
        };

        ConnectionPoolDataSource cpds = Mockito.mock(ConnectionPoolDataSource.class);
        Mockito.doReturn(pc).when(cpds).getPooledConnection();
        return cpds;
    }
}
