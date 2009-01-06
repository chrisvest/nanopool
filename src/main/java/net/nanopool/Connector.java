package net.nanopool;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;

import net.nanopool.cas.CasArray;

final class Connector {
    private final ConnectionPoolDataSource source;
    private final CasArray<Connector> connectors;
    private final int idx;
    private final long timeToLive;
    private PooledConnection connection;
    private long deadTime;
    private volatile Thread owner;

    /**
     * Used for display through the JMX interface.
     * 'volatile' is safe enough for these variables because we are guaranteed
     * that only one thread will ever write to them at a time, thus we're not
     * exposed to lost-update or ordering bugs.
     * The downside of this approach is that resetting the counters isn't eager.
     */
    private volatile int realConnectionsCreated = 0;
    private volatile int connectionsLeased = 0;
    private volatile boolean flagReset = false;

    
    /**
     * Used for asserting that we're not producing multiple leases for the
     * same connection.
     * Does not put contention on the memory bus unless we have assertions
     * enabled. However, enabling assertions will now introduce memory barriers
     * and that might mask visibility bugs, so take care!
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
        if (flagReset) doReset();
        if (deadTime < System.currentTimeMillis())
            invalidate();
        if (connection == null) {
            connection = source.getPooledConnection();
            connection.addConnectionEventListener(new ConnectionListener(this));
            deadTime = System.currentTimeMillis() + timeToLive;
            realConnectionsCreated++;
        }
        assert leaseCount.incrementAndGet() == 1:
            "Connector is used by more than one thread at a time";
        owner = Thread.currentThread();
        Connection con = connection.getConnection();
        connectionsLeased++;
        return con;
    }
    
    void returnToPool() throws SQLException {
        if (flagReset) doReset();
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
        owner = null;
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

    int getConnectionsLeased() {
        return connectionsLeased;
    }

    int getRealConnectionsCreated() {
        return realConnectionsCreated;
    }

    void resetCounters() {
        flagReset = true;
    }

    private void doReset() {
        realConnectionsCreated = 0;
        connectionsLeased = 0;
        flagReset = false;
    }

    Thread getOwner() {
        return owner;
    }
}
