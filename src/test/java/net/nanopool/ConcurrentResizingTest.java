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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Test;

/**
 * 
 * @author cvh
 */
public class ConcurrentResizingTest extends NanoPoolTestBase {
  @Test public void
  mustBePossibleToGetConnectionWhilePoolIsBeingResized() throws Exception {
    final int threadCount = 5;
    pool = npds();
    final NanoPoolDataSource fpool = pool;
    final long stopTime = System.currentTimeMillis() + 200;
    final AtomicReference<SQLException> sqleRef = new AtomicReference<SQLException>();
    final CountDownLatch stopLatch = new CountDownLatch(threadCount);
    
    Thread[] ths = new Thread[threadCount];
    for (int i = 0; i < ths.length; i++) {
      ths[i] = new Thread(new Runnable() {
        public void run() {
          Connection con = null;
          while (System.currentTimeMillis() < stopTime) {
            try {
              con = fpool.getConnection();
            } catch (SQLException sqle) {
              sqle.printStackTrace();
              sqleRef.set(sqle);
            } finally {
              if (con != null) {
                try {
                  con.close();
                } catch (SQLException ex) {
                  ex.printStackTrace();
                }
              }
            }
          }
          stopLatch.countDown();
        }
      }, "TesterThread-" + (i + 1));
    }
    
    for (Thread th : ths) {
      th.start();
    }
    
    CheapRandom rnd = new CheapRandom();
    while (System.currentTimeMillis() < stopTime) {
      fpool.resizePool(rnd.nextAbs(threadCount, threadCount * 3));
    }
    
    stopLatch.await();
    
    SQLException sqle = sqleRef.get();
    if (sqle != null) {
      throw sqle;
    }
  }
}
