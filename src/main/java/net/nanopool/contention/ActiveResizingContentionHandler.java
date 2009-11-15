package net.nanopool.contention;

import java.sql.SQLException;

import net.nanopool.ActiveResizingAgent;
import net.nanopool.ManagedNanoPool;

// TODO javadoc ActiveResizingContentionHander
public class ActiveResizingContentionHandler implements ContentionHandler {
  private final ActiveResizingAgent agent;
  private final int trigger;
  private final int maxSize;

  // TODO javadoc
  public ActiveResizingContentionHandler(
      ActiveResizingAgent agent, int triggeringContentionLevel, int maxSize) {
    this.agent = agent;
    this.trigger = triggeringContentionLevel;
    this.maxSize = maxSize;
  }

  public void handleContention(int count, ManagedNanoPool mnp)
      throws SQLException {
    if (trigger <= count) {
      agent.enqueue(mnp.getMXBean(), maxSize);
    }
  }
}
