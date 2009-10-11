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
import java.util.concurrent.atomic.AtomicInteger;
import javax.sql.ConnectionPoolDataSource;

/**
 * This hook decrements an {@link AtomicInteger} by one every time it is
 * called.
 * @author cvh
 */
public class CountDownHook implements Hook {
  public final AtomicInteger counter;
  
  public CountDownHook(AtomicInteger counter) {
    this.counter = counter;
  }
  
  public void run(EventType type, ConnectionPoolDataSource source,
      Connection con, SQLException sqle) {
    counter.decrementAndGet();
  }
}
