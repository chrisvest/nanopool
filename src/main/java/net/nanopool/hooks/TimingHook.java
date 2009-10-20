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

/**
 * The timing hook records the sum of elapsed time between two event types.
 * <p>
 * The internal state of the TimingHook is protected by a read/write-lock, so
 * take note that this is a possible point of contention.
 * <p>
 * The TimingHook is designed with extensibility in mind, so it can be safely
 * extended to override the time-logging functionality.
 * @author cvh
 */
public class TimingHook implements Hook {
  private final ReadLock readLock;
  private final WriteLock writeLock;
  private final ThreadLocal<Long> currentStartTime = new ThreadLocal<Long>();
  private final EventType startType;
  private final EventType endType;
  
  private long totalTimeMs = 0;
  private int totalCounts = 0;
  
  /**
   * Create a TimingHook that records the elapsed time in milliseconds,
   * between the two specified types of events.
   * <p>
   * The start event type and the end event type are allowed to be the same
   * event type. If they indeed are the same type, then the timer start on the
   * first instance of the event, and stop on the second, start again on the
   * third and so on.
   * @param timerStartType The event type that starts the time recording.
   * @param timerEndType The event type that stops the timer and records
   * the elapsed time.
   */
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
  
  public final void run(EventType type, Connection con, SQLException sqle) {
    if (currentStartTime.get() == null && type == startType) {
      currentStartTime.set(System.nanoTime());
    } else if (type == endType) {
      long end = System.nanoTime();
      long start = currentStartTime.get();
      long spanMs = (end - start) / 1000000L;
      currentStartTime.set(null);
      writeLock.lock();
      try {
        totalCounts++;
        recordTimeMillis(spanMs);
      } finally {
        writeLock.unlock();
      }
    }
  }
  
  /**
   * Calculate the average of the timings, in milliseconds.
   * @return The average time in milliseconds recorded by this TimingHook,
   * as a double.
   */
  public final double avgMs() {
    double ms = 0;
    double connects = 0;
    readLock.lock();
    try {
      ms = totalTimeMs;
      connects = totalCounts;
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
  
  public final int totalCounts() {
    readLock.lock();
    try {
      return totalCounts;
    } finally {
      readLock.unlock();
    }
  }
  
  public final void reset() {
    writeLock.lock();
    try {
      totalCounts = 0;
      totalTimeMs = 0;
    } finally {
      writeLock.unlock();
    }
  }
  
  protected void recordTimeMillis(long spanMs) {
    totalTimeMs += spanMs;
  }
}
