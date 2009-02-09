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

        pool.shutdown();

        assertEquals(1, mbean.getConnectionsCreated());
        assertEquals(2, mbean.getConnectionsLeased());
        assertEquals(0, mbean.getCurrentAvailableConnectionsCount());
        assertEquals(0, mbean.getCurrentLeasedConnectionsCount());
        assertTrue(mbean.isShutDown());
    }

    @Test
    public void resettingMustWorkForBothLiveAndShutDownPools() throws SQLException {
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

    @Override
    protected Configuration buildConfig() {
        return super.buildConfig().setPoolSize(1);
    }
}
