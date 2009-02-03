/*
   Copyright 2008-2009 Christian Vest Hansen

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package net.nanopool;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;

import net.nanopool.hooks.EventType;
import net.nanopool.hooks.Hook;

final class Connector {
    public static final int AVAILABLE = 0;
    public static final int RESERVED = 1;
    public static final int SHUTDOWN = 2;
    public static final int OUTDATED = 2;
    public final AtomicInteger state = new AtomicInteger(AVAILABLE);
    private final ConnectionPoolDataSource source;
    private final long timeToLive;
    private PooledConnection connection;
    private long deadTime;
    private Connection currentLease;

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
    private volatile Thread owner;

    /**
     * Hooks. We don't need to sync these attributes because they are only
     * ever touched by one thread during a lease period.
     */
    private Cons<Hook> preReleaseHooks;
    private Cons<Hook> postReleaseHooks;
    private Cons<Hook> connectionInvalidationHooks;
    
    /**
     * Used for asserting that we're not producing multiple leases for the
     * same connection.
     * Does not put contention on the memory bus unless we have assertions
     * enabled. However, enabling assertions will now introduce memory barriers
     * and that might mask visibility bugs, so take care!
     */
    private final AtomicInteger leaseCount = new AtomicInteger();
    
    Connector(ConnectionPoolDataSource source, long timeToLive) {
        this.source = source;
        this.timeToLive = timeToLive;
    }
    
    Connection getConnection(
            Cons<Hook> preReleaseHooks,
            Cons<Hook> postReleaseHooks,
            Cons<Hook> connectionInvalidationHooks) throws SQLException {
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
            "Connector is used by more than one thread at a time: " +
            leaseCount.get();
        currentLease = connection.getConnection();
        this.owner = Thread.currentThread();
        this.preReleaseHooks = preReleaseHooks;
        this.postReleaseHooks = postReleaseHooks;
        this.connectionInvalidationHooks = connectionInvalidationHooks;
        connectionsLeased++;
        return currentLease;
    }
    
    void returnToPool() throws SQLException {
        FsmMixin.runHooks(preReleaseHooks, EventType.preRelease,
                source, currentLease, null);
        try {
            if (flagReset) doReset();
            if (deadTime < System.currentTimeMillis())
                invalidate();
            assert leaseCount.decrementAndGet() == 0:
                "Connector is used by more than one thread at a time: " +
                leaseCount.get();
            int st = state.get();
            if (st == RESERVED) {
                if (!state.compareAndSet(st, AVAILABLE)) {
                    st = state.get();
                    if (st == SHUTDOWN || st == OUTDATED) {
                        invalidate();
                    } else {
                        throw new IllegalStateException(
                                "Cannot return to pool in state: " + st);
                    }
                }
            } else if (st == SHUTDOWN || st == OUTDATED) {
                invalidate();
            } else {
                throw new IllegalStateException(
                        "Unexpected state when returning to pool: " + st);
            }
        } finally {
            Connection tmpLease = currentLease;
            currentLease = null;
            owner = null;
            FsmMixin.runHooks(postReleaseHooks, EventType.postRelease,
                    source, tmpLease, null);
        }
    }
    
    void invalidate() throws SQLException {
        currentLease = null;
        SQLException sqle = null;
        if (connection == null)
            return;
        try {
            connection.close();
        } catch (SQLException e) {
            sqle = e;
            throw e;
        } finally {
            connection = null;
            FsmMixin.runHooks(connectionInvalidationHooks,
                    EventType.invalidation, source, null, sqle);
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
