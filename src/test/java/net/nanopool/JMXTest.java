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
        assertFalse(mbean.isShutDown());

        Connection con = pool.getConnection();

        assertEquals(1, mbean.getConnectionsCreated());
        assertEquals(1, mbean.getConnectionsLeased());
        assertEquals(0, mbean.getCurrentAvailableConnectionsCount());
        assertFalse(mbean.isShutDown());

        con.close();

        assertEquals(1, mbean.getConnectionsCreated());
        assertEquals(1, mbean.getConnectionsLeased());
        assertEquals(1, mbean.getCurrentAvailableConnectionsCount());
        assertFalse(mbean.isShutDown());

        con = pool.getConnection();

        assertEquals(1, mbean.getConnectionsCreated());
        assertEquals(2, mbean.getConnectionsLeased());
        assertEquals(0, mbean.getCurrentAvailableConnectionsCount());
        assertFalse(mbean.isShutDown());

        con.close();

        assertEquals(1, mbean.getConnectionsCreated());
        assertEquals(2, mbean.getConnectionsLeased());
        assertEquals(1, mbean.getCurrentAvailableConnectionsCount());
        assertFalse(mbean.isShutDown());

        pool.shutdown();

        assertEquals(1, mbean.getConnectionsCreated());
        assertEquals(2, mbean.getConnectionsLeased());
        assertEquals(0, mbean.getCurrentAvailableConnectionsCount());
        assertTrue(mbean.isShutDown());
    }

    @Override
    protected Configuration buildConfig() {
        return super.buildConfig().setPoolSize(1);
    }
}
