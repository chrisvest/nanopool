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
  
  @Override
  protected Settings buildSettings() {
    return super.buildSettings()
      .setPoolSize(1)
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
  resizingMustNotCausePreConnectHooksToRunTwice() throws SQLException, InterruptedException {
    pool = npds();
    Connection con = pool.getConnection();
    try {
      Thread thread = ThreadThat.getAndCloseOneConnection(pool);
      thread.start();
      pool.resizePool(2);
      startLatch.countDown();
      thread.join(1000);
      assertThat(counter.get(), is(1));
    } finally {
      con.close();
    }
  }
}
