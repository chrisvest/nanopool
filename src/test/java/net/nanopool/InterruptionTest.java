package net.nanopool;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import org.junit.Test;

/**
 *
 * @author cvh
 */
public class InterruptionTest extends NanoPoolTestBase {
    @Test
    public void mustPreserveInterruptionState() throws SQLException {
        Thread.currentThread().interrupt();
        pool = npds();
        Connection con = pool.getConnection();
        con.close();
        List<SQLException> sqles = pool.close();
        for (SQLException sqle : sqles) sqle.printStackTrace();
        assertTrue(sqles.isEmpty());
        assertTrue(Thread.interrupted());
    }
}
