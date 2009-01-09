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
    int getConnectionsCreated();
    int getConnectionsLeased();
    boolean isReziable();

    // operations
    String shutDown();
    void resetCounters();
    String listConnectionOwningThreadsStackTraces();
    void dumpConnectionOwningThreadsStackTraces();
    void resizePool(int newSize);
    void interruptConnection(int id);
    void killConnection(int id);
}
