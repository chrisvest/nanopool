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

import net.nanopool.contention.DefaultContentionHandler;
import net.nanopool.contention.ContentionHandler;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import java.util.concurrent.locks.ReentrantLock;
import javax.sql.ConnectionPoolDataSource;

public final class NanoPoolDataSource extends PoolingDataSourceSupport
        implements ManagedNanoPool {
    /**
     * Create a new {@link NanoPoolDataSource} based on the specified
     * {@link ConnectionPoolDataSource}, and with the specified pool size and
     * time-to-live.
     * The pool will use a {@link DefaultContentionHandler} and create its own
     * {@link CasArray} that isn't shared with anyone else.
     * Merely constructing a pool does not open any connections. The connections
     * themselves are created lazily when they are requested.
     * @param source the {@link ConnectionPoolDataSource} instance that will
     * provide the raw connections to this pool. You usually get these instances
     * from your JDBC driver. If your driver of choice does not have an
     * implementation for this interface, then you either have to write it
     * yourself or give up and cry in a corner. Thankfully, most modern JDBC
     * drivers support this feature of the JDBC specification.
     * @param poolSize The total number of connections this pool can contain.
     * The size of the pool cannot be changed once set.
     * @param timeToLive The maximum allowed age of connections, specified in
     * milliseconds. A connection will be closed if it return to the pool older
     * than this, and connections will be reopened if they are older than this
     * when acquired. A connection that grows older than this while in use, will
     * not be closed from under you.
     * @since 1.0
     */
    public NanoPoolDataSource(ConnectionPoolDataSource source, int poolSize,
            long timeToLive) {
        this(source, new Configuration()
                .setPoolSize(poolSize).setTimeToLive(timeToLive));
    }
    
    /**
     * Create a new {@link NanoPoolDataSource} based on the specified
     * {@link ConnectionPoolDataSource}, {@link CasArray} implementation and
     * {@link ContentionHandler}, and connection time-to-live.
     * @param source the {@link ConnectionPoolDataSource} instance that will
     * provide the raw connections to this pool. You usually get these instances
     * from your JDBC driver. If your driver of choice does not have an
     * implementation for this interface, then you either have to write it
     * yourself or give up and cry in a corner. Thankfully, most modern JDBC
     * drivers support this feature of the JDBC specification.
     * @param connectors The specific {@link CasArray} implementation that this
     * NanoPoolDataSource instance should use for implementing the actual pool.
     * This CasArray also defines the size of the pool. The CasArray must be
     * empty when parsed to NanoPool, and must not be tampered with afterwards
     * unless you know how to do it without violating the integrity of the pool.
     * @param timeToLive The maximum allowed age of connections, specified in
     * milliseconds. A connection will be closed if it return to the pool older
     * than this, and connections will be reopened if they are older than this
     * when acquired. A connection that grows older than this while in use, will
     * not be closed from under you.
     * @param contentionHandler The {@link ContentionHandler} that will be
     * invoked when the pool feels that the level of contention is a bit high.
     * Specifically, when the pool, during a call to getConnection, have
     * searched the entier CasArray for any available connections and found
     * none. ContentionHandlers can safely throw a RuntimeException (to be
     * caught in client code) or briefly pause the thread in the hope that a
     * connection will become available in the mean time.
     * @since 1.0
     */
    public NanoPoolDataSource(ConnectionPoolDataSource source,
            Configuration config) {
        super(source, config);
    }

    /**
     * Lease a new {@link Connection} from the pool.
     * This method will attempt to reserve one of the connections that is
     * available from the pool. If the pool have not been completely saturated
     * with connections, then a new connection will be created at an available
     * slot and immediately leased.
     * If there are no connections available to lease, then we will sit down
     * around the camp fire and wait until one <em>becomes</em> available, and
     * the exact procedure of how this happens is dictated by this pools
     * configured {@link ContentionHandler} (which does a {@link Thread#yield()}
     * by default).
     * @return A new/old {@link Connection} from the pool. This object is
     * guaranteed to only be available to a single thread <em>provided</em>
     * that you yourself do not share it amongst more than one thread (and I
     * will spank you if you do -- hard) <em>and</em> you are not keeping
     * connection objects around after you close them. And be sure that you
     * close your connection when you're done with it - it will not return to
     * the pool if you forget this!
     * @throws SQLException Thrown if we tried to establish a sparkly-new
     * connection with the configured {@link ConnectionPoolDataSource} and it
     * <em>fails!</em>
     * @since 1.0
     */
    public Connection getConnection() throws SQLException {
        try {
            return FsmMixin.getConnection(this);
        } catch (OutdatedException _) {
            return getConnection();
        }
    }
    
    /**
     * Initiate a shutdown sequence on the pool.
     * This method will return before the pool has completely shutdown, however
     * the pool <strong>will</strong> be unable to grant any new connections.
     * Calling {@link NanoPoolDataSource#getConnection()} on a shut down pool
     * will result in an {@link IllegalStateException}.
     * Calling {@link NanoPoolDataSource#shutdown()} on a pool that has already
     * been shut down, has no effect.
     * Connections that are active and in use when the pool is shut down will
     * <strong>not</strong> be forcibly killed. Instead, all active connections
     * will be allowed to operate for as long as they please, until they are
     * closed normally or closed forcibly by the database or similar external
     * factors. Any connections that are not in use, will be marked unavailable
     * and closed. This behavior means that it is (relatively) safe to shut
     * down a pool that is in use.
     * Any {@link SQLException}s encountered during the shut down procedure
     * are aggregated in a {@link List}, and that list is returned as the
     * result of this method, for perusal by client code.
     * @return A {@link List} of all {@link SQLException}s caught when shutting
     * the pool down.
     * @since 1.0
     */
    public List<SQLException> shutdown() {
        try {
            Connector[] cons = connectors;
            connectors = null;
            return FsmMixin.shutdown(cons);
        } catch (OutdatedException _) {
            return shutdown();
        }
    }
    
    /**
     * This method will always throw UnsupportedOperationException.
     * @param username not used
     * @param password not used
     * @return never returns
     * @throws SQLException never throws SQLException
     * @throws UnsupportedOperationException always.
     * @since 1.0
     */
    public Connection getConnection(String username, String password)
            throws SQLException {
        throw new UnsupportedOperationException("Not supported.");
    }
    
    /**
     * Get the {@link NanoPoolManager} instance for this NanoPoolDataSource.
     * @return Always the same instance.
     * @since 1.0
     */
    public NanoPoolManagementMBean getMXBean() {
        try {
            poolManagementLock.lock();
            if (poolManagement == null) {
                poolManagement = new NanoPoolManagement(this);
            }
        } finally {
            poolManagementLock.unlock();
        }
        return poolManagement;
    }
    private NanoPoolManagement poolManagement;
    private final ReentrantLock poolManagementLock = new ReentrantLock();

    void resizePool(int newSize) {
        FsmMixin.resizePool(this, newSize);
    }
}
