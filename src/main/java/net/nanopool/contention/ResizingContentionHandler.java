package net.nanopool.contention;

import net.nanopool.ManagedNanoPool;
import net.nanopool.NanoPoolManagementMBean;

/**
 * The ResizingContentionHandler will, upon contention, try to increase the
 * size of the pool. The ResizingContentionHandler never shrinks the pool.
 * <p>
 * The ResizingContentionHandler will yield the thread if it encounters
 * contention yet decides not to increase the pool, for any reason.
 * @author vest
 *
 */
public class ResizingContentionHandler implements ContentionHandler {
  private final int triggeringContentionLevel;
  private final double factor;
  private final int increment;
  private final int max;

  /**
   * Create a new ResizingContentionHandler that operates under the specified
   * (immutable) parameters.
   * <p>
   * When it is decided that the pool is contended and needs to be made bigger,
   * the new pool size is derived from the current using the following formula:
   * new-size = old-size &sdot; factor + increment. This allows the
   * ResizingContentionHandler to be configured for either exponential or
   * linear pool growth.
   * <p>
   * If the increment is zero, and the current size multiplied by the factor,
   * when converted to an integer, is equal to the current size, then the pool
   * is increased by one.
   * @param triggeringContentionLevel
   *          This number specifies how eager we should be to resize the pool.
   *          Contention level is defined as how many times the whole pool has
   *          been traversed in search of an available connection. A good
   *          number is probably somewhere between 10 and 100.
   * @param factor
   *          This number specifies an exponential growth rate. If this number
   *          is 1.0, the pool will grow linearly according to the increment.
   *          If this number is 2.0 and the increment is 0, then the pool will
   *          double in size every time it is resized. This number cannot be
   *          less than 1.0, and if the increment is 0 then this number must be
   *          greater than 1.0. A good value is probably somewhere around 1.1
   *          or 1.2, that is to say; slightly larger than 1.0.
   * @param increment
   *          The increment is a constant that is added to the result of
   *          multiplying the current pool size by the factor, to form a new
   *          pool size. This value cannot be negative, and must furthermore
   *          be greater than 0 if the factor is 1.0.
   * @param maxSize
   *          This number specifies the upper limit of the pool growth. That
   *          is, the pool will never be resized to a value larger than this.
   *          If the size of the pool is already equal to or larger than this
   *          value, then the ResizingContentionHandler will yield the thread.
   *          The ResizingContentionHandler never shrinks the pool, not even if
   *          it is larger than this value. If the ResizingContentionHandler
   *          finds that the pool needs to be made larger, but the new size, as
   *          specified by the formula given above, ends up larger than this
   *          maximum, then the pool is simply resized to this maximum possible
   *          value.
   */
  public ResizingContentionHandler(
      int triggeringContentionLevel, double factor, int increment, int maxSize) {
    checkValuesAreLegal(triggeringContentionLevel, factor, increment, maxSize);
    this.triggeringContentionLevel = triggeringContentionLevel;
    this.factor = factor;
    this.increment = increment;
    this.max = maxSize;
  }

  // TODO javadoc checkValuesAreLegal
  public static void checkValuesAreLegal(int triggeringContentionLevel,
      double factor, int increment, int maxSize) {
    if (triggeringContentionLevel < 1) {
      throw new IllegalArgumentException(
          "triggeringContentionLevel cannot be less than one.");
    }
    if (factor < 1.0) {
      throw new IllegalArgumentException("factor cannot be less than one.");
    }
    if (increment == 0 && factor < 1.1) {
      throw new IllegalArgumentException(
          "factor must be greater than one when increment is zero.");
    }
    if (increment < 0) {
      throw new IllegalArgumentException(
          "increment must be greater than zero.");
    }
    if (maxSize < 1) {
      throw new IllegalArgumentException("maxSize cannot be less than one.");
    }
  }

  public void handleContention(int count, ManagedNanoPool mnp) {
    if (count < triggeringContentionLevel) {
      Thread.yield();
      return;
    }
    NanoPoolManagementMBean mbean = mnp.getMXBean();
    if (!resizePool(mbean, factor, increment, max)) {
      Thread.yield();
    }
  }

  // TODO javadoc resizePool
  public static boolean resizePool(
      NanoPoolManagementMBean mbean, double factor, int increment, int max) {
    int oldSize = mbean.getPoolSize();
    int newSize = (int) (oldSize * factor + increment);
    if (newSize == oldSize) {
      newSize++;
    }
    newSize = Math.min(newSize, max);
    if (oldSize < newSize) {
      mbean.resizePool(newSize);
      return true;
    }
    return false;
  }
}
