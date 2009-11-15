package net.nanopool.contention;

import java.sql.SQLException;

import net.nanopool.ManagedNanoPool;

// TODO javadoc ActiveResizingContentionHander
public class ActiveResizingContentionHandler implements ContentionHandler {
  private final ActiveResizingAgent agent;
  private final int trigger;

  // TODO javadoc
  public ActiveResizingContentionHandler(
      ActiveResizingAgent agent, int triggeringContentionLevel) {
    this.agent = agent;
    this.trigger = triggeringContentionLevel;
  }

  public void handleContention(int count, ManagedNanoPool mnp)
      throws SQLException {
    if (trigger <= count) {
      agent.enqueue(new PoolResize());
    }
  }

}
