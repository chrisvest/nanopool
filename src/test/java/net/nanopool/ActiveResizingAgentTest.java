package net.nanopool;

import static org.mockito.Mockito.*;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;

import net.nanopool.ActiveResizingAgent;

import org.junit.Before;

public class ActiveResizingAgentTest {
  ActiveResizingAgent agent;
  BlockingQueue queue;
  TimeSource time;
  NanoPoolManagementMBean mbean;
  Executor executor;
  
  @Before public void
  setUp() {
    queue = mock(BlockingQueue.class);
    time = mock(TimeSource.class);
    mbean = mock(NanoPoolManagementMBean.class);
    executor = mock(Executor.class);
    agent = new ActiveResizingAgent(queue, time, executor);
  }
  
//  @Test public void
//  mustMark
}
