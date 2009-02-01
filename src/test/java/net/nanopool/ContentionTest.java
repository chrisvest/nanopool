package net.nanopool;

import java.util.logging.Level;
import java.util.logging.Logger;
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

        final Thread thisThread = Thread.currentThread();
        final AtomicBoolean finishedMarker = new AtomicBoolean(false);
        Thread killer = new Thread(new Runnable() {
            @SuppressWarnings("deprecation")
            public void run() {
                try {
                    Thread.sleep(10000);
                    if (!finishedMarker.get()) {
                        thisThread.stop(new SQLException(
                                "Die you gravy sucking pig-dog."));
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }, "killer");
        killer.setDaemon(true);
        killer.start();

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
