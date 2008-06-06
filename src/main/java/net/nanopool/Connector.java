package net.nanopool;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;

import net.nanopool.cas.CasArray;

public class Connector {
    private final ConnectionPoolDataSource source;
    private final CasArray<Connector> connectors;
    private final int idx;
    private final long timeToLive;
    private PooledConnection connection;
    private long deadTime;
    
    /**
     * Used for asserting that we're not producing multiple leases for the
     * same connection.
     */
    private final AtomicInteger leaseCount = new AtomicInteger();
    
    public Connector(ConnectionPoolDataSource source,
            CasArray<Connector> connectors, int idx, long timeToLive) {
        this.source = source;
        this.connectors = connectors;
        this.idx = idx;
        this.timeToLive = timeToLive;
    }
    
    Connector() {
        this(null, null, 0, 0);
    }
    
    public Connection getConnection() throws SQLException {
        if (deadTime < System.currentTimeMillis())
            invalidate();
        if (connection == null) {
            connection = source.getPooledConnection();
            connection.addConnectionEventListener(new ConnectionListener(this));
            deadTime = System.currentTimeMillis() + timeToLive;
        }
        assert leaseCount.incrementAndGet() == 1;
        return connection.getConnection();
    }
    
    public void returnToPool() throws SQLException {
        if (deadTime < System.currentTimeMillis())
            invalidate();
        Connector ticket = connectors.get(idx);
        assert leaseCount.decrementAndGet() == 0;
        connectors.cas(idx, this, ticket);
    }
    
    public void invalidate() throws SQLException {
        if (connection == null)
            return;
        try {
            connection.close();
        } finally {
            connection = null;
        }
    }
}
