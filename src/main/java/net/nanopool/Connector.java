package net.nanopool;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Queue;
import java.util.concurrent.Semaphore;

import javax.sql.ConnectionPoolDataSource;

public class Connector extends ConnectorSupport {
    public Connector(ConnectionPoolDataSource source, long timeout,
            Queue<Connector> queue, Semaphore semaphore, Log log) {
        super(source, timeout, queue, semaphore, log);
    }

    synchronized Connection getConnection() throws SQLException {
        if (connection == null) {
            log.debug("Opening new physical connection.");
            connection = source.getPooledConnection();
            connectedTime = System.currentTimeMillis();
            connection.addConnectionEventListener(new ConnectionListener(this));
            Connection con = connection.getConnection();
            return con;
        }
        if (System.currentTimeMillis() > connectedTime + timeout) {
            invalidate();
            return getConnection();
        }
        return connection.getConnection();
    }

    synchronized void invalidate() {
        log.debug("Invalidating connection.");
        try {
            connection.close();
        } catch (SQLException e) {
            log.warn(e, "Failure when closing physical database connection.");
        }
        connection = null;
        semaphore.release();
    }

    void returnToPool() {
        log.debug("Returning to pool.");
        queue.add(this);
        semaphore.release();
    }
}
