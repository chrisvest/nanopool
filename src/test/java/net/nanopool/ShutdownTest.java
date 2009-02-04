package net.nanopool;

import java.sql.Connection;
import static org.junit.Assert.*;

import java.sql.SQLException;
import java.util.List;
import org.junit.Test;

/**
 *
 * @author cvh
 */
public class ShutdownTest extends NanoPoolTestBase {
    @Test
    public void shutDownPoolsMustRefuseToConnect() throws SQLException {
        pool = npds();
        Connection con = pool.getConnection();
        assertNotNull(con);
        con.close();
        List sqles = pool.shutdown();
        assertTrue("Got exceptions from shutdown.", sqles.isEmpty());
        try {
            pool.getConnection();
            fail("getConnection did not throw.");
        } catch (IllegalStateException ile) {
            assertEquals(FsmMixin.MSG_SHUT_DOWN, ile.getMessage());
        }
    }
}
