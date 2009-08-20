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
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import javax.sql.ConnectionPoolDataSource;

/**
 * 
 * @author cvh
 */
public class TimingHook implements Hook {
  private final ReadLock readLock;
  private final WriteLock writeLock;
  private final ThreadLocal<Long> currentStartTime = new ThreadLocal<Long>();
  private final EventType startType;
  private final EventType endType;
  
  private long totalTimeMs = 0;
  private int totalConnects = 0;
  
  public TimingHook(EventType timerStartType, EventType timerEndType) {
    this.startType = timerStartType;
    this.endType = timerEndType;
    // it is important that this RW-lock is fair, because otherwise readers
    // may starve out writers, and that is bad because writers are in the
    // process of working with a connection.
    ReentrantReadWriteLock rwlock = new ReentrantReadWriteLock(true);
    readLock = rwlock.readLock();
    writeLock = rwlock.writeLock();
  }
  
  public final void run(EventType type, ConnectionPoolDataSource source,
      Connection con, SQLException sqle) {
    if (currentStartTime.get() == null && type == startType) {
      currentStartTime.set(System.nanoTime());
    } else if (type == endType) {
      long end = System.nanoTime();
      long start = currentStartTime.get();
      long spanMs = (end - start) / 1000000L;
      currentStartTime.set(null);
      writeLock.lock();
      try {
        totalConnects++;
        recordTimeMillis(spanMs);
      } finally {
        writeLock.unlock();
      }
    }
  }
  
  public final double avgMs() {
    double ms = 0;
    double connects = 0;
    readLock.lock();
    try {
      ms = totalTimeMs;
      connects = totalConnects;
    } finally {
      readLock.unlock();
    }
    return ms / connects;
  }
  
  public final long totalMs() {
    readLock.lock();
    try {
      return totalTimeMs;
    } finally {
      readLock.unlock();
    }
  }
  
  public final int totalConnects() {
    readLock.lock();
    try {
      return totalConnects;
    } finally {
      readLock.unlock();
    }
  }
  
  public final void reset() {
    writeLock.lock();
    try {
      totalConnects = 0;
      totalTimeMs = 0;
    } finally {
      writeLock.unlock();
    }
  }
  
  protected void recordTimeMillis(long spanMs) {
    totalTimeMs += spanMs;
  }
}
