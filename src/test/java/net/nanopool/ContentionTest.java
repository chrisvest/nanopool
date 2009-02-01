package net.nanopool;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.SQLException;
import net.nanopool.contention.ThrowingContentionHandler;
import org.junit.Test;

/**
 *
 * @author cvh
 */
public class ContentionTest extends NanoPoolTestBase {
    @Override
    protected Configuration buildConfig() {
        return super.buildConfig().setContentionHandler(
                new ThrowingContentionHandler());
    }

    @Test
    public void contentionHandlerMustRun() throws SQLException {
        pool = npds();
        Connection[] cons = new Connection[pool.allConnectors.length];
        for (int i = 0; i < cons.length; i++) {
            cons[i] = pool.getConnection();
        }

        // pool is now empty. Next getConnection must throw.

        try {
            pool.getConnection();
            fail("Expected getConnection to throw.");
        } catch (NanoPoolRuntimeException npre) {
            assertEquals(ThrowingContentionHandler.MESSAGE, npre.getMessage());
        }

        for (int i = 0; i < cons.length; i++) {
            cons[i].close();
        }
    }
}
