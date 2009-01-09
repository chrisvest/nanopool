package net.nanopool.hooks;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;
import javax.sql.ConnectionPoolDataSource;

/**
 *
 * @author cvh
 */
public class CountDownHook implements Hook {
    public final AtomicInteger counter;

    public CountDownHook(AtomicInteger counter) {
        this.counter = counter;
    }

    public void run(EventType type, ConnectionPoolDataSource source, Connection con, SQLException sqle) {
        counter.decrementAndGet();
    }
}
