package net.nanopool;

import javax.sql.DataSource;

/**
 *
 * @author cvh
 */
public class NanoPoolManager implements NanoPoolManagerMBean {
    private final NanoPoolDataSource np;

    public NanoPoolManager(DataSource np) {
        this.np = (NanoPoolDataSource)np;
    }

    public int getCurrentOpenConnectionsCount() {
        return np.fsm.countOpenConnections(np.connectors);
    }

    public int getPoolSize() {
        return np.poolSize;
    }

    public long getConnectionTimeToLive() {
        return np.timeToLive;
    }

    public String getContentionHandlerClassName() {
        return np.contentionHandler.getClass().getName();
    }

    public String getContentionHandler() {
        return np.contentionHandler.toString();
    }
}
