package net.nanopool.contention;

import net.nanopool.ActiveResizingAgent;

import org.junit.Test;
import org.mockito.Mockito;

public class ActiveResizingContentionHandlerConstructionTest {
  int trigger = 1;
  double factor = 1.0;
  int increment = 1;
  int maxSize = 1;
  ActiveResizingAgent agent = Mockito.mock(ActiveResizingAgent.class);

  private static void ctor(ActiveResizingAgent agent, int trigger, double factor,
      int increment, int maxSize) {
    new ActiveResizingContentionHandler(
        agent, trigger, factor, increment, maxSize);
  }
  
  @Test(expected = IllegalArgumentException.class) public void
  agentMustNotBeNull() {
    ctor(null, trigger, factor, increment, maxSize);
  }
  
  @Test(expected = IllegalArgumentException.class) public void
  triggerCannotBeLessThanOne() {
    ctor(agent, 0, factor, increment, maxSize);
  }
  
  @Test(expected = IllegalArgumentException.class) public void
  factorCannotBeLessThanOne() {
    ctor(agent, trigger, 0.9, increment, maxSize);
  }
  
  @Test(expected = IllegalArgumentException.class) public void
  factorMustBeGreaterThanOneWhenIncrementIsZero() {
    ctor(agent, trigger, 1.0, 0, maxSize);
  }
  
  @Test(expected = IllegalArgumentException.class) public void
  incrementCannotBeLessThanZero() {
    ctor(agent, trigger, factor, -1, maxSize);
  }
  
  @Test(expected = IllegalArgumentException.class) public void
  maxSizeCannotBeLessThanOne() {
    ctor(agent, trigger, factor, increment, 0);
  }
}
