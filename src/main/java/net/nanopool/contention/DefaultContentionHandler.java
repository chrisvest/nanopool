package net.nanopool.contention;

import net.nanopool.contention.ContentionHandler;

/**
 * The default {@link ContentionHandler} implementation.
 * The waiting is implemented with {@link Thread#yield()} and by default
 * also prints a warning, though this can be turned off.
 * @author vest
 * @since 1.0
 */
public final class DefaultContentionHandler implements ContentionHandler {
    private final boolean printWarning;
    
    public DefaultContentionHandler() {
        this(true);
    }
    
    public DefaultContentionHandler(boolean printWarning) {
        this.printWarning = printWarning;
    }
    
    public void handleContention() {
        if (printWarning)
            System.err.println("PoolingDataSource: contention warning.");
        Thread.yield();
    }
}
