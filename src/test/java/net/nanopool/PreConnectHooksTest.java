package net.nanopool;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import net.nanopool.contention.ContentionHandler;
import net.nanopool.hooks.CountUpHook;
import net.nanopool.hooks.Hook;

import org.junit.Before;
import org.junit.Test;

public class PreConnectHooksTest extends NanoPoolTestBase {
  private ContentionHandler contentionHandler;
  private Hook preConnectHook;
  private AtomicInteger counter;
  private CountDownLatch startLatch;
  private int poolSize;
  
  @Override
  protected Settings buildSettings() {
    return super.buildSettings()
      .setPoolSize(poolSize)
      .setContentionHandler(contentionHandler)
      .addPreConnectHook(preConnectHook);
  }
  
  @Before public void
  setUp() {
    counter = new AtomicInteger();
    startLatch = new CountDownLatch(1);
    contentionHandler = new LatchWaitingContentionHandler(startLatch);
    preConnectHook = new CountUpHook(counter);
  }

  @Test public void
  resizingDownMustNotCausePreConnectHooksToRunTwice() throws Exception {
    verifyPreConnectHookRuns(2, 1, 1);
  }

  private void verifyPreConnectHookRuns(int startSize, int endSize,
      int preConnectHookRuns) throws Exception {
    poolSize = startSize;
    pool = npds();
    Connection[] cons = new Connection[startSize];
    Thread thread = ThreadThat.getAndCloseOneConnection(pool);
    try {
      for (int i = 0; i < startSize; i++) {
        cons[i] = pool.getConnection();
      }
      counter.set(0);
      thread.start();
      thread.join(25);
      pool.resizePool(endSize);
    } finally {
      for (Connection con : cons) {
        con.close();
      }
    }
    startLatch.countDown();
    thread.join(1000);
    assertThat(counter.get(), is(preConnectHookRuns));
  }
  
  @Test public void
  resizingUpMustNotCausePreConnectHooksToRunTwice() throws Exception {
    verifyPreConnectHookRuns(1, 2, 1);
  }
}
