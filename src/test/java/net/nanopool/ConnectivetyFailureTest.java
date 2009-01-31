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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.ConnectionPoolDataSource;
import org.junit.Test;
import org.mockito.Mockito;

/**
 *
 * @author cvh
 */
public class ConnectivetyFailureTest extends NanoPoolTestBase {
    @Test
    public void mustNormallyConnect() throws SQLException {
        NanoPoolDataSource pool = npds();
        Connection con = pool.getConnection();
        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("select 1");
            assertTrue(rs.next());
            assertEquals(1, rs.getInt(1));
        } finally {
            con.close();
        }
    }

    @Test
    public void mustHandleThrowingConnector() {
        String msg = "Bomb.";
        NanoPoolDataSource pool = buildPoolThrowingOnConnect(msg);
        try {
            pool.getConnection();
            fail("Expected thrown exception.");
        } catch (SQLException ex) {
            assertEquals(msg, ex.getMessage());
        }
    }

    @Test
    public void throwingConnectorsMustNotLeakReservationTickets() {
        int connectAttempts = 1000;
        String msg = "Bomb.";
        NanoPoolDataSource pool = buildPoolThrowingOnConnect(msg);
        for (int i = 0; i < connectAttempts; i++) {
            try {
                pool.getConnection();
                fail("Expected thrown exception.");
            } catch (SQLException ex) {
                assertEquals(msg, ex.getMessage());
            }
        }
        for (int i = 0; i < pool.allConnectors.length; i++) {
            assertNull(pool.allConnectors[i]);
            assertNull(pool.connectors.get(i));
        }
    }

    private NanoPoolDataSource buildPoolThrowingOnConnect(String msg) {
        ConnectionPoolDataSource cpds = Mockito.mock(ConnectionPoolDataSource.class);
        try {
            Mockito.doThrow(new SQLException(msg)).when(cpds).getPooledConnection();
        } catch (SQLException ex) {
            fail("mock setup failure.");
        }
        NanoPoolDataSource pool = buildNpds(cpds, buildConfig());
        return pool;
    }
}
