package net.nanopool.hooks;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicLong;
import javax.sql.ConnectionPoolDataSource;

/**
 *
 * @author cvh
 */
public class CountingHook implements Hook {
    private final AtomicLong counter = new AtomicLong();

    public void run(EventType type, ConnectionPoolDataSource source, Connection con, SQLException sqle) {
        counter.incrementAndGet();
    }

    public long getCount() {
        return counter.get();
    }

    public void resetCount() {
        counter.set(0);
    }
}
