package net.nanopool;

public class DefaultContentionHandler implements ContentionHandler {
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
