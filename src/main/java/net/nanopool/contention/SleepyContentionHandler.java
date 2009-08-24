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

import net.nanopool.ManagedNanoPool;

/**
 * A sleepy implementation of {@link ContentionHandler}. Waiting is implemented
 * by {@link Thread#sleep(long)}'ing the thread for a while.
 * 
 * @author vest
 * @since 1.0
 */
public class SleepyContentionHandler implements ContentionHandler {
  private final long sleepTime;
  
  /**
   * Create a sleepy contention handler that will put the thread to sleep for
   * 10 milliseconds when the pool gets contended.
   */
  public SleepyContentionHandler() {
    this(10);
  }
  
  /**
   * Create a SleepyContentionHandler that, upon contention, will sleep the
   * thread for the specified number of milliseconds. This assumes that the
   * given sleepTime parameter is a positive number, that is, greater than
   * zero.
   * <p>
   * The thread may sleep for a shorter time period if it is interrupted.
   * If the thread is interrupted while sleeping in this ContentionHandler,
   * it will be resumed and the interruption status will be preserved so the
   * thread is still interrupted.
   * <p>
   * The SleepyContentionHandler can exhibit an alternative behavior, if the
   * given sleepTime is less than or equal to zero. In this case, the actual
   * sleep time will be the contention level multiplied by 10, in milliseconds.
   * The contention level is the number of times the entire pool has been
   * searched for an available connection. Operating this way, the
   * SleepyContentionHandler will give you a progressive back-off from very
   * high contention situations.
   * <p>
   * Progressive back-off can serve as inspiration for other custom
   * ContentionHandlers, but you usually do not want to use the
   * SleepyContentionHandler this way as it may severely affect your latency
   * times. Before you get the idea that you might need this, please make sure
   * that your pools aren't too small, and that you are using them correctly.
   * @param sleepTime
   */
  public SleepyContentionHandler(long sleepTime) {
    this.sleepTime = sleepTime;
  }
  
  public void handleContention(int count, ManagedNanoPool mnp) {
    try {
      Thread.sleep(sleepTime < 1 ? ((long) count) * 10 : sleepTime);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
