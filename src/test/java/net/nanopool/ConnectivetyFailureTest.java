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

import java.sql.SQLException;
import javax.sql.ConnectionPoolDataSource;
import org.junit.Test;
import org.mockito.Mockito;

/**
 *
 * @author cvh
 */
public class ConnectivetyFailureTest extends NanoPoolTestBase {
    private static final String MESSAGE = "Bomb.";

    @Test
    public void mustHandleThrowingConnector() throws SQLException {
        pool = npds();
        try {
            pool.getConnection();
            fail("Expected thrown exception.");
        } catch (SQLException ex) {
            assertEquals(MESSAGE, ex.getMessage());
        }
    }

    @Test
    public void throwingConnectorsMustNotLeakReservationTickets() throws SQLException {
        int connectAttempts = 1000;
        pool = npds();
        for (int i = 0; i < connectAttempts; i++) {
            try {
                pool.getConnection();
                fail("Expected thrown exception.");
            } catch (SQLException ex) {
                assertEquals(MESSAGE, ex.getMessage());
            }
        }
        for (int i = 0; i < pool.allConnectors.length; i++) {
            assertNull(pool.allConnectors[i]);
            assertNull(pool.connectors.get(i));
        }
    }

    @Override
    protected ConnectionPoolDataSource buildCpds() throws SQLException {
        ConnectionPoolDataSource cpds = Mockito.mock(ConnectionPoolDataSource.class);
        try {
            Mockito.doThrow(new SQLException(MESSAGE)).when(cpds).getPooledConnection();
        } catch (SQLException ex) {
            fail("mock setup failure.");
        }
        return cpds;
    }
}
