package net.nanopool;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.ConnectionPoolDataSource;

public class PoolingDataSource extends PoolingDataSourceSupport {
    public PoolingDataSource(ConnectionPoolDataSource source,
            int totalSize, long timeout, Log log) {
        super(source, totalSize, timeout, log);
        log.debug("New pooling datasource with %s", source);
    }

    public Connection getConnection() throws SQLException {
        if (semaphore.tryAcquire()) {
            Connector connector = queue.poll();
            return connector.getConnection();
        }
        // all semaphore permits are in use
        int contention = semaphore.getQueueLength();
        if (contention > 10) {
            log.warn("Connection pool contetion warning: %s threads are blocked!",
                    contention);
        }
        
        try {
            semaphore.acquire();
        } catch (final InterruptedException e) {
            throw new PoolException("Thread was interrupted.", e);
        }
        Connector connector = queue.poll();
        return connector.getConnection();
    }

    public Connection getConnection(String username, String password)
            throws SQLException {
        throw new UnsupportedOperationException();
    }
}
