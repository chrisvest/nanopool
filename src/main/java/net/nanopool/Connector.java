package net.nanopool;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;

import net.nanopool.cas.CasArray;

public final class Connector {
    private final ConnectionPoolDataSource source;
    private final CasArray<Connector> connectors;
    private final int idx;
    private final long timeToLive;
    private PooledConnection connection;
    private long deadTime;
    
    /**
     * Used for asserting that we're not producing multiple leases for the
     * same connection.
     * Does not put contention on the memory bus unless we have assertions
     * enabled.
     */
    private final AtomicInteger leaseCount = new AtomicInteger();
    
    Connector(ConnectionPoolDataSource source,
            CasArray<Connector> connectors, int idx, long timeToLive) {
        this.source = source;
        this.connectors = connectors;
        this.idx = idx;
        this.timeToLive = timeToLive;
    }
    
    Connector() {
        this(null, null, 0, 0);
    }
    
    Connection getConnection() throws SQLException {
        if (deadTime < System.currentTimeMillis())
            invalidate();
        if (connection == null) {
            connection = source.getPooledConnection();
            connection.addConnectionEventListener(new ConnectionListener(this));
            deadTime = System.currentTimeMillis() + timeToLive;
        }
        assert leaseCount.incrementAndGet() == 1:
            "Connector is used by more than one thread at a time";
        return connection.getConnection();
    }
    
    void returnToPool() throws SQLException {
        if (deadTime < System.currentTimeMillis())
            invalidate();
        Connector marker = connectors.get(idx);
        assert leaseCount.decrementAndGet() == 0:
            "Connector was used by more than one thread at a time";
        if (marker != FsmMixin.shutdownMarker) {
            assert marker == FsmMixin.reservationMarker:
                "Invalid state of CasArray<Connector> on index " + idx;
            connectors.cas(idx, this, marker);
        } else {
            // we've been shut down, so let's clean up.
            invalidate();
        }
    }
    
    void invalidate() throws SQLException {
        if (connection == null)
            return;
        try {
            connection.close();
        } finally {
            connection = null;
        }
    }
}
