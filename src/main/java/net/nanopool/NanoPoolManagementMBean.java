package net.nanopool;

/**
 *
 * @author cvh
 */
public interface NanoPoolManagementMBean {
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
