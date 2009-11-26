package net.nanopool;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;


public class ActiveResizingAgent {
  
  private final Executor executor;
  private final ConcurrentMap<NanoPoolManagementMBean, Long> mbeanCache =
    new ConcurrentHashMap<NanoPoolManagementMBean, Long>();
  
  /**
   * Deliberately under-synchronized.
   * A single-value cache hoping to reduce the memory-bus load from the real
   * ConcurrentHashMap-based MBean cache.
   */
  private NanoPoolManagementMBean lastEnqueueAttemptCache;

  public ActiveResizingAgent(Executor executor, TimeSource time) {
    this.executor = executor;
  }

  public void eventuallyResize(
      NanoPoolManagementMBean mbean, double factor, int inc, int maxSize) {
    if (lastEnqueueAttemptCache != mbean && cache(mbean)) {
      lastEnqueueAttemptCache = mbean;
      executor.execute(resizeTask(mbean, maxSize));
    }
  }

  private boolean cache(NanoPoolManagementMBean mbean) {
    return mbeanCache.putIfAbsent(mbean, System.currentTimeMillis()) == null;
  }

  private Runnable resizeTask(NanoPoolManagementMBean mbean, int maxSize) {
    return new Runnable() {
      public void run() {
      }
    };
  }
  
}
