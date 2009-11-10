package net.nanopool.contention;

import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;

public class ActiveResizingContentionHandlerTest {
  ActiveResizingContentionHandler handler;
  ActiveResizingContentionHandler.Actor actor;
  
  @Before public void
  setUp() {
    actor = mock(ActiveResizingContentionHandler.Actor.class);
    handler = new ActiveResizingContentionHandler(actor);
  }
  
  @Test public void
  startMethodMustDelegateToActor() {
    handler.start();
    verify(actor).start();
  }
  
  @Test public void
  stopMethodMustDelegateToActorShutdown() {
    handler.stop();
    verify(actor).shutdown();
  }
}
