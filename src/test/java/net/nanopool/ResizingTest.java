package net.nanopool;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;
import net.nanopool.contention.ThrowingContentionHandler;
import org.junit.Test;

/**
 *
 * @author cvh
 */
public class ResizingTest extends NanoPoolTestBase {
    @Test
    public void poolMustWorkAfterGrowing() throws SQLException {
        pool = npds();
        assertWorking(pool);
        pool.resizePool(15);
        assertWorking(pool);
        pool.shutdown();
    }

    @Test
    public void poolMustWorkAfterShrinking() throws SQLException {
        pool = npds();
        assertWorking(pool);
        pool.resizePool(5);
        assertWorking(pool);
        pool.shutdown();
    }

    private void assertWorking(NanoPoolDataSource pool) throws SQLException {
        int len = pool.connectors.length();
        assertEquals(len, pool.allConnectors.length);
        Connection[] cons = new Connection[len];

        for (int i = 0; i < len; i++) {
            cons[i] = pool.getConnection();
        }

        final AtomicBoolean finishedMarker = new AtomicBoolean(false);
        killMeLaterCheck(finishedMarker, 10000,
                new SQLException("Die you gravy sucking pig-dog."));

        try {
            pool.getConnection();
            fail("Expected getConnection to throw.");
        } catch (NanoPoolRuntimeException npre) {
            assertEquals(ThrowingContentionHandler.MESSAGE, npre.getMessage());
        }
        finishedMarker.set(true);

        for (int i = 0; i < cons.length; i++) {
            cons[i].close();
        }
    }

    @Override
    protected Configuration buildConfig() {
        return super.buildConfig().setContentionHandler(
                new ThrowingContentionHandler());
    }
}
