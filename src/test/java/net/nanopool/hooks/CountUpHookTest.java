package net.nanopool.hooks;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;

public class CountUpHookTest {
  AtomicInteger counter;
  
  @Before public void
  setUp() {
    counter = new AtomicInteger();
  }
  
  @Test(expected = NullPointerException.class) public void
  mustThrowNullPointerExceptionWhenParsedNullToConstructor() {
    new CountUpHook(null);
  }
  
  @Test public void
  mustReuseParsedAtomicIntegerInstance() {
    assertTrue(new CountUpHook(counter).counter == counter);
  }
  
  @Test public void
  creatingWithoutSpecifiedCounterMustStartAtZero() {
    assertThat(new CountUpHook().counter.get(), is(0));
  }
  
  @Test public void
  mustAlwaysCountDownWhenRun() {
    new CountUpHook(counter).run(null, null, null, null);
    assertThat(counter.get(), is(1));
  }
}
