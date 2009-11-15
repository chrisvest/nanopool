package net.nanopool.contention;

import static org.mockito.Mockito.*;

import java.sql.SQLException;

import net.nanopool.ActiveResizingAgent;
import net.nanopool.ManagedNanoPool;
import net.nanopool.NanoPoolManagementMBean;

import org.junit.Before;
import org.junit.Test;

public class ActiveResizingContentionHandlerTest {
  int triggeringContentionLevel = 2;
  int maxSize = 100;
  ContentionHandler handler;
  ActiveResizingAgent agent;
  ManagedNanoPool mnp;
  NanoPoolManagementMBean mbean;
  
  @Before public void
  setUp() {
    mbean = mock(NanoPoolManagementMBean.class);
    mnp = mock(ManagedNanoPool.class);
    when(mnp.getMXBean()).thenReturn(mbean);
    agent = mock(ActiveResizingAgent.class);
    handler = new ActiveResizingContentionHandler(
        agent, triggeringContentionLevel, maxSize);
  }
  
  @Test public void
  mustNotEnqueueResizeWhenContentionLevelIsLessThanTrigger() throws SQLException {
    handler.handleContention(triggeringContentionLevel - 1, mnp);
    verify(agent, never()).eventuallyResize(mbean, maxSize);
  }
  
  @Test public void
  mustEnqueueResizeWhenContentionIsAtTrigger() throws SQLException {
    handler.handleContention(triggeringContentionLevel, mnp);
    verify(agent).eventuallyResize(mbean, maxSize);
  }
  
  @Test public void
  mustEnqueueResizeWhenContentionIsGreaterThanTrigger() throws SQLException {
    handler.handleContention(triggeringContentionLevel + 1, mnp);
    verify(agent).eventuallyResize(mbean, maxSize);
  }
}



