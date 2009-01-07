package net.nanopool.contention;

import net.nanopool.*;

/**
 * A sleepy implementation of {@link ContentionHandler}.
 * Waiting is implemented by {@link Thread#sleep(long)}'ing for one
 * millisecond.
 * @author vest
 * @since 1.0
 */
public class SleepyContentionHandler implements ContentionHandler {
    public void handleContention() {
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
