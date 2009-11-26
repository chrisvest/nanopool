package net.nanopool.contention;

import java.sql.SQLException;

import net.nanopool.ActiveResizingAgent;
import net.nanopool.ManagedNanoPool;

// TODO javadoc ActiveResizingContentionHander
public class ActiveResizingContentionHandler implements ContentionHandler {
  private final ActiveResizingAgent agent;
  private final int trigger;
  private final int maxSize;
  private final double factor;
  private final int inc;

  // TODO javadoc
  public ActiveResizingContentionHandler(
      ActiveResizingAgent agent, int triggeringContentionLevel, double factor,
      int increment, int maxSize) {
    if (agent == null) {
      throw new IllegalArgumentException("agent cannot be null.");
    }
    ResizingContentionHandler.checkValuesAreLegal(
        triggeringContentionLevel, factor, increment, maxSize);
    this.agent = agent;
    this.trigger = triggeringContentionLevel;
    this.maxSize = maxSize;
    this.factor = factor;
    this.inc = increment;
  }

  public void handleContention(int count, ManagedNanoPool mnp)
      throws SQLException {
    if (trigger <= count) {
      agent.eventuallyResize(mnp.getMXBean(), factor, inc, maxSize);
    }
  }
}
