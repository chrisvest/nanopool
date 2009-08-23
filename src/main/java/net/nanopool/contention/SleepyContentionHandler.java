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
  
  public SleepyContentionHandler() {
    this(10);
  }
  
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
