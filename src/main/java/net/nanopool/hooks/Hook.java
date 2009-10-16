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
package net.nanopool.hooks;

import java.sql.Connection;
import java.sql.SQLException;

import net.nanopool.Settings;

/**
 * A Hook is a type of call-back that is invoked when certain events occur
 * within the connection pool. See the {@link EventType} for the types of
 * events that invoke hooks, and the {@link Settings} for how to register hooks
 * with a pool.
 * <p>
 * Hooks are mostly useful for debugging purpose, and for generating
 * performance data and counters.
 * @author cvh
 * @see EventType
 * @see Settings
 */
public interface Hook {
  /**
   * This method is called on a hook when an event it is registered for occurs.
   * <p>
   * This method may throw a RuntimeException to abort a flow inside the pool -
   * this is safe to do without putting the pool in an inconsistent state.
   * <p>
   * <strong>Note on dead-locks:</strong> You are likely to hold locks inside
   * the JDBC driver when hooks run - this is mostly true for the -release
   * hooks, but you should not make assumptions. Therefore, be careful not
   * to create any dead-locks between locks in your own code and locks in the
   * JDBC driver.
   * @param type The type of event that has occurred. Never null.
   * @param con The connection, if any, that this event occurred in relation
   * to. May be null.
   * @param sqle Any SQLException that might have been thrown. This
   * SQLException instance may be logged, but should not be re-thrown - please
   * assume that it is otherwise properly handled. May be null.
   */
  void run(EventType type, Connection con, SQLException sqle);
}
