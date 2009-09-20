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

import javax.sql.ConnectionPoolDataSource;

import net.nanopool.contention.ContentionHandler;

/**
 * The {@link NanoPoolManagementMBean} is the JMX management interface for the
 * {@link NanoPoolDataSource}. Objects that implement this interface can be
 * obtained from the {@link ManagedNanoPool#getMXBean()} method, which the
 * NanoPoolDataSource implements.
 * <p>
 * Implementors of this interface are expected to be thread-safe.
 * 
 * @author cvh
 */
public interface NanoPoolManagementMBean {
  // attributes
  /**
   * Get the number of connections in this pool that are not leased. The
   * connections are not necessarily open, they are just not reserved by anyone
   * at the moment. Note that this is an estimate and the actual number may
   * change while we are counting. Fetching this property can be expected to
   * have an O(N) time complexity on the size of the pool. The number of
   * available connections on a pool that has been shut down is zero.
   * @return The estimated count of the currently available connections.
   */
  int getCurrentAvailableConnectionsCount();
  
  /**
   * Get the number of connections in this pool that are currently leased.
   * Leased connections are the ones that are in use in the application.
   * When a leased connection is "closed" in the application code, it will
   * return to the pool and become available for lease again by other threads,
   * or indeed the same thread. Fetching this property can be expected to have
   * and O(N) time complexity on the size of the pool. The number of leased
   * connections in a pool that has been shut down is
   * <em>probabilistically</em> zero, that is, this method may return zero as
   * soon as the shutdown sequence has been initiated - even if the shutdown
   * has yet to complete and the pool technically still have connections that
   * are leased out.
   * @return The estimated count of the currently leased out connections.
   */
  int getCurrentLeasedConnectionsCount();
  
  /**
   * Get the size of the pool. The size is the maximum number of connections
   * that can be leased out at any time in the pools current configuration.
   * This property can be expected to operate with an O(1) time complexity.
   * @return The size of the connection pool.
   */
  int getPoolSize();
  
  /**
   * Get the maximum permitted age, in milliseconds, that connections are
   * allowed to live in this pool. This value never changes for a pool.
   * @return The time-to-live value for this pool, in milliseconds.
   */
  long getConnectionTimeToLive();
  
  /**
   * Get the name of the class that the configured {@link ContentionHandler}
   * is an instance of.
   * @see Class#getName()
   * @return The class-name of the {@link ContentionHandler} configured for
   * this pool.
   */
  String getContentionHandlerClassName();
  
  /**
   * Get the String representation of the {@link ContentionHandler} that has
   * been configured for this pool. This is the result of calling
   * {@code toString()} on the ContentionHandler.
   * @return The String representation of this pools ContentionHandler.
   */
  String getContentionHandler();
  
  /**
   * Determine whether the pool has been or is shutting down. Note that this
   * method will return true s soon as the shutdown sequence has been
   * initiated, and so will return true for pools where the shutdown sequence
   * has yet to complete.
   * @return True if the pool can no longer be used due to having been shut
   * down.
   */
  boolean isShutDown();
  
  /**
   * Get the {@link Class#getName()} of the concrete
   * {@link ConnectionPoolDataSource} implementation that is used as the source
   * of physical connections in this pool
   * @return A raw-form class name.
   */
  String getSourceConnectionClassName();
  
  /**
   * Get the string representation of the concrete
   * {@link ConnectionPoolDataSource} implementation that is used as the source
   * of physical connections in this pool. This value is obtained by calling
   * {@code toString} on the ConnectionPoolDataSouce.
   * @return The result of calling {@code toString} on this pools
   * ConnectionPoolDataSouce.
   */
  String getSourceConnection();
  
  /**
   * Get the number of physical connections that has been created by this
   * pool since it was created, or since the last reset.
   * @return Zero or a positive integer, subject to overflow.
   */
  int getConnectionsCreated();
  
  /**
   * Get the total number of connections that has been leased by this pool
   * since it was created or since last reset.
   * @return Zero or a positive integer, subject to overflow.
   */
  int getConnectionsLeased();
  
  /**
   * Get the connection reuse rate. This number specifies, as a decimal number,
   * the percentage of connection leases that does not create a new physical
   * connection. This is defined as 1 &minus; created &divide; leased
   * connections. This value is calculated atomically.
   * @return A decimal number between zero and one.
   */
  double getConnectionsReuseRate();
  
  // operations
  /**
   * Shut the pool down. Once this method returns, the pool will have finished
   * its shutdown procedure. This means that it will no longer be possible for
   * other threads to get connections from the pool, however, connections that
   * are already leased will continue to function normally until they are
   * closed.
   */
  String shutDown();
  
  /**
   * Reset the connections created and connections leased counters.
   * Note that the counters may change while the resetting is taking place,
   * so they are not guaranteed to be zero after this method returns.
   * <p>
   * Resetting involves traversal, so the time it takes to perform the reset
   * is O(n) on the size of the pool.
   * <p>
   * This is a blocking method - only one counter reset can take place at a
   * time.
   * <p>
   * This method is <em>not</em> atomic and is <em>not</em> isolated, so other
   * threads that simultaneously tries to read the counters may experience
   * them "flicker" - that is, jumping up and down. This is the consequence of
   * design decisions that prioritizes pool performance over the accuracy of
   * these counters.
   */
  void resetCounters();
  
  /**
   * Produce a stack-dump of all threads that currently holds a connection.
   * This is only intended for debugging purpose and the actual format is
   * unspecified.
   * <p>
   * Note that the dump produced may be out-dated by the time this method
   * returns.
   * @return Stack-dumps for all threads that currently holds a connection.
   * Never null, not even if the pool has been shut down.
   */
  String listConnectionOwningThreadsStackTraces();
  
  /**
   * Produce a stack-dump of all threads that currently holds a connection,
   * and write it to System.err. The format is the same as for
   * {@link NanoPoolManagementMBean#listConnectionOwningThreadsStackTraces()}
   * @see NanoPoolManagementMBean#listConnectionOwningThreadsStackTraces()
   */
  void dumpConnectionOwningThreadsStackTraces();
  
  void resizePool(int newSize);
  
  void interruptConnection(int id);
  
  void killConnection(int id);
}
