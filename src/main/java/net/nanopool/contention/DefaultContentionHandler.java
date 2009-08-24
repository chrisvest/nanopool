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

import net.nanopool.ManagedNanoPool;

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
  
  /**
   * Create a DefaultContentionHandler that will print contention warnings,
   * and throw SQLExceptions if the contention level goes over 1000.
   * <p>
   * Contention level is how many times over the entire pool has been searched
   * for an available connection.
   */
  public DefaultContentionHandler() {
    this(true, 1000);
  }
  
  /**
   * Create a DefaultContentionHandler with the specified parameters.
   * @param printWarning
   *          If true, the DefaultContentionHandler will print a warning
   *          message to System.err when the pool is contended.
   * @param throwLimit
   *          The contention level limit that, if passed, will cause the
   *          DefaultContentionHandler to throw an SQLException. If this value
   *          is zero, then an SQLException will always be thrown. If this
   *          value is negative, then the SQLException will never be thrown. A
   *          negative throwLimit turns this feature off.
   */
  public DefaultContentionHandler(boolean printWarning, int throwLimit) {
    this.printWarning = printWarning;
    this.throwLimit = throwLimit;
  }
  
  public void handleContention(int count, ManagedNanoPool mnp)
      throws SQLException {
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
