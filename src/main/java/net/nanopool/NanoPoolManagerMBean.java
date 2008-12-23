package net.nanopool;

/**
 *
 * @author cvh
 */
public interface NanoPoolManagerMBean {
    // attributes
    int getCurrentOpenConnectionsCount();
    int getPoolSize();
    long getConnectionTimeToLive();
    String getContentionHandlerClassName();
    String getContentionHandler();
    boolean isShutDown();
    String getSourceConnectionClassName();
    String getSourceConnection();

    // operations
    String shutDown();
}
