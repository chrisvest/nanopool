package net.nanopool.contention;

import static org.mockito.Mockito.*;

import java.util.concurrent.BlockingQueue;

import org.junit.Before;

public class ActiveResizingAgentTest {
  ActiveResizingAgent agent;
  BlockingQueue queue;
  
  @Before public void
  setUp() {
    queue = mock(BlockingQueue.class);
    agent = new ActiveResizingAgent(queue);
  }
}
