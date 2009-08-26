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
import java.util.List;

import javax.sql.ConnectionPoolDataSource;

import net.nanopool.contention.ContentionHandler;

/**
 * NanoPool is a lightweight and fast JDBC2 connection pool.
 * <p>
 * It is designed to scale well to many concurrent threads with a low overhead.
 * The implementation does a sequential search in an array of  connections from
 * a random starting point, to find a possibly available connection. Then it
 * does a CAS on a marker of that connection to mark it as reserved and returns
 * that connection if the CAS succeeds. But if the CAS fails, then it means
 * that some other thread got the connection first, and so we continue our
 * sequential search, wrapping around the pool if we got to the end. While a
 * sequential search may not sound very fast, it actually usually is. It ends
 * up that this implementation, given a reasonably sized pool, is faster than
 * maintaining a complex data structure that may theoretically operate with a
 * less-than O(N) time complexity.
 * <p>
 * NanoPool uses no internal threads for maintenance. This makes it lighter in
 * resource consumption, and life-cycling easier, but it also has some negative
 * consequences. For instance, the only threads that can do pool maintenance,
 * such as renewing aging connections, are the threads that enter the pool to
 * do either acquire or release connections. This will likely have an effect on
 * the maximum and standard deviation of latency, compared to other pools.
 * 
 * @author vest
 *
 */
public final class NanoPoolDataSource extends PoolingDataSourceSupport
    implements ManagedNanoPool {
  private final NanoPoolManagement poolManagement;
  
  /**
   * Create a new {@link net.nanopool.NanoPoolDataSource} based on the
   * specified {@link ConnectionPoolDataSource}, and with the specified pool
   * size and time-to-live.
   * 
   * The pool will, apart from the pool size and time-to-live, use default
   * values for all configurable parameters. Merely constructing a pool does
   * not open any connections. The connections themselves are created lazily
   * when they are requested.
   * 
   * @param source
   *          the {@link ConnectionPoolDataSource} instance that will provide
   *          the raw connections to this pool. You usually get these instances
   *          from your JDBC driver. If your driver of choice does not have an
   *          implementation for this interface, then you either have to write
   *          it yourself or give up and cry in a corner. Thankfully, most
   *          modern JDBC drivers support this feature of the JDBC
   *          specification.
   * 
   * @param poolSize
   *          The total number of connections this pool can contain. The size
   *          of the pool cannot be changed once set.
   * 
   * @param timeToLive
   *          The maximum allowed age of connections, specified in
   *          milliseconds. A connection will be closed if it return to the
   *          pool older than this, and connections will be reopened if they
   *          are older than this when acquired. A connection that grows older
   *          than this while in use, will not be closed from under you.
   * 
   * @since 1.0
   */
  public NanoPoolDataSource(ConnectionPoolDataSource source, int poolSize,
      long timeToLive) {
    this(source,
        new Settings().setPoolSize(poolSize).setTimeToLive(timeToLive));
  }
  
  /**
   * Create a new {@link NanoPoolDataSource} based on the specified
   * {@link ConnectionPoolDataSource} and {@link Settings} instance.
   * 
   * The pool will take a snapshot of the Settings config parsed to this
   * constructor. This means that it is safe to share the same Settings
   * instance among multiple pools, and it is safe to mutate the Settings
   * instance - the changes will not affect any previously created pools.
   * 
   * Merely constructing a pool does not open any connections. The connections
   * themselves are created lazily when they are requested.
   * 
   * @param source
   *          the {@link ConnectionPoolDataSource} instance that will provide
   *          the raw connections to this pool. You usually get these instances
   *          from your JDBC driver. If your driver of choice does not have an
   *          implementation for this interface, then you either have to write
   *          it yourself or give up and cry in a corner. Thankfully, most
   *          modern JDBC drivers support this feature of the JDBC
   *          specification.
   * 
   * @param settings
   *          a {@link Settings} instance that fully specifies how this
   *          NanoPoolDataSource should be configured. A snapshot of the
   *          configuration config will be taken, so the Settings instance can
   *          be freely modified afterwards and even concurrently, without
   *          affecting this new NanoPoolDataSource instance.
   * 
   * @since 1.0
   * @see Settings
   */
  public NanoPoolDataSource(
      ConnectionPoolDataSource source, Settings settings) {
    super(source, settings);
    poolManagement = new NanoPoolManagement(state);
  }
  
  /**
   * Lease a new {@link Connection} from the pool. This method will attempt to
   * reserve one of the connections that is available from the pool. If the
   * pool have not been completely saturated with connections, then a new
   * connection will be created at an available slot and immediately leased. If
   * there are no connections available to lease, then we will sit down around
   * the camp fire and wait until one <em>becomes</em> available, and the exact
   * procedure of how this happens is dictated by this pools configured
   * {@link ContentionHandler} (which does a {@link Thread#yield()} by
   * default).
   * 
   * @return A new/old {@link Connection} from the pool. This object is
   *         guaranteed to only be available to a single thread
   *         <em>provided</em> that you yourself do not share it amongst more
   *         than one thread (and I will spank you if you do -- hard)
   *         <em>and</em> you are not keeping connection objects around after
   *         you close them. And be sure that you close your connection when
   *         you're done with it - it will not return to the pool if you forget
   *         this!
   * @throws SQLException
   *           Thrown if we tried to establish a sparkly-new connection with
   *           the configured {@link ConnectionPoolDataSource} and it
   *           <em>fails!</em>
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
   * Initiate a close sequence on the pool. This method will return before the
   * pool has completely close, however the pool <strong>will</strong> be
   * unable to grant any new connections. Calling
   * {@link NanoPoolDataSource#getConnection()} on a closed pool will result in
   * an {@link IllegalStateException}. Calling
   * {@link net.nanopool.NanoPoolDataSource#close()} on a pool that has already
   * been shut down, has no effect. Connections that are active and in use when
   * the pool is shut down will <strong>not</strong> be forcibly killed.
   * Instead, all active connections will be allowed to operate for as long as
   * they please, until they are closed normally or closed forcibly by the
   * database or similar external factors. Any connections that are not in use,
   * will be marked unavailable and closed. This behavior means that it is
   * (relatively) safe to shut down a pool that is in use. Any
   * {@link SQLException}s encountered during the shut down procedure are
   * aggregated in a {@link List}, and that list is returned as the result of
   * this method, for perusal by client code.
   * 
   * @return A {@link List} of all {@link SQLException}s caught when shutting
   *         the pool down.
   * @since 1.0
   */
  public List<SQLException> close() {
    return FsmMixin.close(state);
  }
  
  /**
   * This method will always throw UnsupportedOperationException.
   * 
   * @param username
   *          not used
   * @param password
   *          not used
   * @return never returns
   * @throws SQLException
   *           never throws SQLException
   * @throws UnsupportedOperationException
   *           always.
   * @since 1.0
   */
  @Override
  public Connection getConnection(String username, String password)
      throws SQLException {
    throw new UnsupportedOperationException("Not supported.");
  }
  
  /**
   * Get the {@link NanoPoolManagementMBean} instance for this
   * NanoPoolDataSource.
   * 
   * @return Always the same instance.
   * @since 1.0
   * @see ManagedNanoPool#getMXBean()
   */
  public NanoPoolManagementMBean getMXBean() {
    return poolManagement;
  }
  
  void resizePool(int newSize) {
    FsmMixin.resizePool(state, newSize);
  }
}
