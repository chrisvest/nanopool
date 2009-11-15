package net.nanopool.contention;

import static org.mockito.Mockito.*;

import java.sql.SQLException;

import net.nanopool.ManagedNanoPool;

import org.junit.Before;
import org.junit.Test;

public class ActiveResizingContentionHandlerTest {
  int triggeringContentionLevel = 2;
  ContentionHandler handler;
  ActiveResizingAgent agent;
  ManagedNanoPool mnp;
  
  @Before public void
  setUp() {
    agent = mock(ActiveResizingAgent.class);
    handler = new ActiveResizingContentionHandler(
        agent, triggeringContentionLevel);
  }
  
  @Test public void
  mustNotEnqueueResizeWhenContentionLevelIsLessThanTrigger() throws SQLException {
    handler.handleContention(triggeringContentionLevel - 1, mnp);
    verify(agent, never()).enqueue(any(PoolResize.class));
  }
  
  @Test public void
  mustEnqueueResizeWhenContentionIsAtTrigger() throws SQLException {
    handler.handleContention(triggeringContentionLevel, mnp);
    verify(agent).enqueue(any(PoolResize.class));
  }
  
  @Test public void
  mustEnqueueResizeWhenContentionIsGreaterThanTrigger() throws SQLException {
    handler.handleContention(triggeringContentionLevel + 1, mnp);
    verify(agent).enqueue(any(PoolResize.class));
  }
}



