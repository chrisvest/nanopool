package net.nanopool;

import static org.mockito.Mockito.*;

import java.util.concurrent.Executor;

import org.junit.Before;
import org.junit.Test;

public class ActiveResizingAgentTest {
  class ImmediateExecutor implements Executor {
    public void execute(Runnable command) {
      command.run();
    }
  }

  ActiveResizingAgent agent;
  TimeSource time;
  NanoPoolManagementMBean mbean;
  Executor executor;
  
  @Before public void
  setUp() {
    time = mock(TimeSource.class);
    mbean = mock(NanoPoolManagementMBean.class);
    executor = new ImmediateExecutor();
    agent = new ActiveResizingAgent(executor, time);
  }
  
  @Test public void
  mustEnlargeLinearlyWhenFactorIsOne() {
    assertResize(1.0, 1, 2, 1, 2);
  }

  private void assertResize(double factor, int inc, int max, int from, int to) {
    when(mbean.getPoolSize()).thenReturn(from);
    agent.eventuallyResize(mbean, factor, inc, max);
    if (from != to) {
      verify(mbean).resizePool(to);
    } else {
      verify(mbean, never()).resizePool(to);
    }
  }
  
  @Test public void
  mustEnlargeExponentiallyWhenFactorIsGreaterThanOne() {
    assertResize(2.0, 0, 10, 2, 4);
  }
  
  @Test public void
  mustNotGrowBeyondMax() {
    assertResize(2.0, 0, 2, 2, 2);
  }
  
  @Test public void
  mustNotShrinkPoolIfLargerThanMax() {
    assertResize(2.0, 2, 2, 4, 4);
  }
  
  @Test public void
  mustNotResizeIfCurrentSizeIsEqualToMax() {
    assertResize(1.0, 1, 2, 2, 2);
  }
  
  @Test public void
  mustEnlargeAtLeastByOneIfIncrementIsZero() {
    assertResize(1.1, 0, 10, 1, 2);
  }
  
}











