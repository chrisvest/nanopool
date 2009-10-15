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
import javax.sql.ConnectionPoolDataSource;

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
 */
public interface Hook {
  void run(EventType type, ConnectionPoolDataSource source, Connection con,
      SQLException sqle);
}
