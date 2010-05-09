package net.nanopool;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.ScheduledExecutorService;

import org.junit.Before;
import org.junit.Test;

public class ActiveResizingAgentQueueingTest {
  double factor = 1.0;
  int increment = 1;
  int maxSize = 10;
  ActiveResizingAgent agent;
  TimeSource time;
  NanoPoolManagementMBean mbean;
  ScheduledExecutorService executor;
  
  @Before public void
  setUp() {
    time = mock(TimeSource.class);
    mbean = mock(NanoPoolManagementMBean.class);
    executor = mock(ScheduledExecutorService.class);
    agent = new ActiveResizingAgent(executor, time);
  }
  
  @Test public void
  mustNotHaveSameMBeanInQueueMultipleTimes() {
    givenEnqueues(mbean, mbean);
    verify(executor, times(1)).execute(any(Runnable.class));
  }
  
  @Test public void
  mustNotEnqueueSameMBeanEvenIfInterleavedWithOthers() {
    givenEnqueues(mbean, mock(NanoPoolManagementMBean.class), mbean);
    verify(executor, times(2)).execute(any(Runnable.class));
  }

  private void givenEnqueues(NanoPoolManagementMBean... mbeans) {
    for (NanoPoolManagementMBean mbean : mbeans) {
      agent.eventuallyResize(mbean, factor, increment, maxSize);
    }
  }
}
