package net.nanopool.hooks;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;

public class CountDownHookTest {
  AtomicInteger counter;
  
  @Before public void
  setUp() {
    counter = new AtomicInteger();
  }
  
  @Test(expected = NullPointerException.class) public void
  mustThrowNullPointerExceptionWhenParsedNullToConstructor() {
    new CountDownHook(null);
  }
  
  @Test public void
  mustReuseParsedAtomicIntegerInstance() {
    assertTrue(new CountDownHook(counter).counter == counter);
  }
  
  @Test public void
  creatingWithoutSpecifiedCounterMustStartAtZero() {
    assertThat(new CountDownHook().counter.get(), is(0));
  }
  
  @Test public void
  mustAlwaysCountDownWhenRun() {
    new CountDownHook(counter).run(null, null, null, null);
    assertThat(counter.get(), is(-1));
  }
}
