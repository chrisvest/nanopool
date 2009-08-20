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
package net.nanopool.contention;

import java.sql.SQLException;

/**
 * The default {@link ContentionHandler} implementation. The waiting is
 * implemented with {@link Thread#yield()} and by default also prints a warning,
 * though this can be turned off.
 * 
 * @author vest
 * @since 1.0
 */
public final class DefaultContentionHandler implements ContentionHandler {
  private final boolean printWarning;
  private final int throwLimit;
  
  public DefaultContentionHandler() {
    this(true, 1000);
  }
  
  public DefaultContentionHandler(boolean printWarning, int throwLimit) {
    this.printWarning = printWarning;
    this.throwLimit = throwLimit;
  }
  
  public void handleContention(int count) throws SQLException {
    if (0 < throwLimit && throwLimit <= count) {
      throw new SQLException("NanoPoolDataSource too contended. "
          + "Look for connection leaks or increase the pool size.");
    }
    if (printWarning) {
      System.err.println("NanoPoolDataSource: contention warning.");
    }
    Thread.yield();
  }
}
