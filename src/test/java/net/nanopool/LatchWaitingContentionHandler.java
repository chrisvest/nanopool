/**
 * 
 */
package net.nanopool;

import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;

import net.nanopool.contention.ContentionHandler;

final class LatchWaitingContentionHandler implements
    ContentionHandler {
  private final CountDownLatch startLatch;
  
  LatchWaitingContentionHandler(CountDownLatch startLatch) {
    this.startLatch = startLatch;
  }
  
  public void handleContention(int count, ManagedNanoPool mnp)
      throws SQLException {
    try {
      startLatch.await();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}