package net.nanopool;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;
import net.nanopool.contention.ResizingContentionHandler;

import org.junit.Before;
import org.junit.Test;

public class ResizingContentionHandlerTest {
  int trigger = 10;
  ResizingContentionHandler rch;
  ManagedNanoPool mnp;
  NanoPoolManagementMBean mbean;
  
  @Before public void
  setUp() {
    mnp = mock(ManagedNanoPool.class);
    mbean = mock(NanoPoolManagementMBean.class);
    when(mnp.getMXBean()).thenReturn(mbean);
  }
  
  @Test public void
  mustEnlargeLinearlyWhenFactorIsOne() {
    rch = new ResizingContentionHandler(trigger, 1.0, 1, 2);
    verifyEnlargemenFromTo(1, 2);
  }

  private void verifyEnlargemenFromTo(int oldSize, int newSize) {
    handleContentionWithCurrentSize(oldSize);
    verify(mbean).resizePool(newSize);
  }

  private void handleContentionWithCurrentSize(int currentSize) {
    currentSizeIs(currentSize);
    rch.handleContention(trigger, mnp);
  }

  private void currentSizeIs(int currentSize) {
    when(mbean.getPoolSize()).thenReturn(currentSize);
  }
  
  @Test public void
  mustEnlargePoolExponentiallyWhenFactorIsGreaterThanOne() {
    rch = new ResizingContentionHandler(trigger, 2.0, 0, 4);
    verifyEnlargemenFromTo(2, 4);
  }
  
  @Test public void
  mustNotGrowBeyondMax() {
    rch = new ResizingContentionHandler(trigger, 4.0, 4, 2);
    verifyEnlargemenFromTo(1, 2);
  }
  
  @Test public void
  mustNotShrinkPoolIfLargerThanMax() {
    rch = new ResizingContentionHandler(trigger, 2.0, 2, 2);
    handleContentionWithCurrentSize(4);
    assertNoResize();
  }

  private void assertNoResize() {
    verify(mbean, never()).resizePool(anyInt());
  }
  
  @Test public void
  mustNotResizeIfCurrentSizeIsEqualToMax() {
    rch = new ResizingContentionHandler(trigger, 1.0, 1, 2);
    handleContentionWithCurrentSize(2);
    assertNoResize();
  }
  
  @Test public void
  mustNotResizeBeforeContentionLevelReachesTrigger() {
    rch = new ResizingContentionHandler(trigger + 1, 1.0, 1, 2);
    handleContentionWithCurrentSize(1);
    assertNoResize();
  }
  
  @Test public void
  mustResizeEvenIfTheCountHasSkippedPassedTrigger() {
    rch = new ResizingContentionHandler(trigger - 1, 1.0, 1, 2);
    verifyEnlargemenFromTo(1, 2);
  }
  
  @Test public void
  mustEnlargeByAtLeastOneWhenIncrementIsZero() {
    rch = new ResizingContentionHandler(trigger, 1.1, 0, 2);
    verifyEnlargemenFromTo(1, 2);
  }
  
  @Test(expected = IllegalArgumentException.class) public void
  triggerCannotBeLessThanOne() {
    new ResizingContentionHandler(0, 1.0, 1, 2);
  }
  
  @Test(expected = IllegalArgumentException.class) public void
  factorCannotBeLessThanOne() {
    new ResizingContentionHandler(trigger, 0.9, 1, 2);
  }
  
  @Test(expected = IllegalArgumentException.class) public void
  factorMustBeGreaterThanOneWhenIncrementIsZero() {
    new ResizingContentionHandler(trigger, 1.0, 0, 2);
  }
  
  @Test(expected = IllegalArgumentException.class) public void
  incrementCannotBeLessThanZero() {
    new ResizingContentionHandler(trigger, 1.0, -1, 2);
  }
  
  @Test(expected = IllegalArgumentException.class) public void
  maxCannotBeLessThanOne() {
    new ResizingContentionHandler(trigger, 1.0, 1, 0);
  }
}
