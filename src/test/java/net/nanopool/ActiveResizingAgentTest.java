package net.nanopool;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class ActiveResizingAgentTest {

  @SuppressWarnings("unchecked")
  public class RunOrCallNow implements Answer {
    public Object answer(InvocationOnMock invocation) throws Throwable {
      Object[] arguments = invocation.getArguments();
      for (Object possibleTaskObject : arguments) {
        if (possibleTaskObject instanceof Runnable) {
          ((Runnable) possibleTaskObject).run();
        } else if (possibleTaskObject instanceof Callable) {
          ((Callable) possibleTaskObject).call();
        }
      }
      return null;
    }
  }

  ActiveResizingAgent agent;
  TimeSource time;
  NanoPoolManagementMBean mbean;
  ScheduledExecutorService executor;
  
  @Before public void
  setUp() {
    time = mock(TimeSource.class);
    mbean = mock(NanoPoolManagementMBean.class);
    executor = newImmediateExecutor();
    agent = new ActiveResizingAgent(executor, time);
  }
  
  private ScheduledExecutorService newImmediateExecutor() {
    ScheduledExecutorService executor = mock(ScheduledExecutorService.class);
    doAnswer(new RunOrCallNow()).when(executor).execute(any(Runnable.class));
    return executor;
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
  
  // TODO must periodically check if pool should be shrunk
  // TODO must eventually shrink a pool that has grown too large
}











