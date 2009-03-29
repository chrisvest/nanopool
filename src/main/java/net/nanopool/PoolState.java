package net.nanopool;

import java.util.concurrent.locks.ReentrantLock;
import javax.sql.ConnectionPoolDataSource;

/**
 *
 * @author cvh
 */
class PoolState {
    final ConnectionPoolDataSource source;
    final ReentrantLock resizingLock;
    final Config config;
    volatile Connector[] connectors;

    PoolState(ConnectionPoolDataSource source, Config config,
            Connector[] connectors) {
        this.source = source;
        this.config = config;
        this.connectors = connectors;
        resizingLock = new ReentrantLock();
    }
}
