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
  
  long getConnectionTimeToLive();
  
  String getContentionHandlerClassName();
  
  String getContentionHandler();
  
  boolean isShutDown();
  
  String getSourceConnectionClassName();
  
  String getSourceConnection();
  
  int getConnectionsCreated();
  
  int getConnectionsLeased();
  
  // operations
  String shutDown();
  
  void resetCounters();
  
  String listConnectionOwningThreadsStackTraces();
  
  void dumpConnectionOwningThreadsStackTraces();
  
  void resizePool(int newSize);
  
  void interruptConnection(int id);
  
  void killConnection(int id);
}
