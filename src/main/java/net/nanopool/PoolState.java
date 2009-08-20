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

import java.util.concurrent.locks.ReentrantLock;
import javax.sql.ConnectionPoolDataSource;

/**
 * 
 * @author cvh
 */
class PoolState {
  final ConnectionPoolDataSource source;
  final ReentrantLock resizingLock;
  final Config config;
  volatile Connector[] connectors;
  
  PoolState(ConnectionPoolDataSource source, Config config,
      Connector[] connectors) {
    this.source = source;
    this.config = config;
    this.connectors = connectors;
    resizingLock = new ReentrantLock();
  }
}
