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
 * Implementors of this interface are not expected to be thread-safe. So
 * although they may be, users of this interface cannot expect them to be. This
 * is typically not a problem because {@link ManagedNanoPool#getMXBean()} is
 * guaranteed to be thread-safe, so you just fetch a thread-local instance
 * every time you need one.
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
   */
  int getCurrentAvailableConnectionsCount();
  
  int getCurrentLeasedConnectionsCount();
  
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
