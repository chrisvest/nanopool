package net.nanopool;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.sql.ConnectionPoolDataSource;
import net.nanopool.loadbalancing.Strategy;

/**
 *
 * @author cvh
 */
public class LoadBalancedNanoPoolDataSource extends AbstractDataSource
        implements ManagedNanoPool {
    private final CopyOnWriteArrayList<NanoPoolDataSource> pools;
    private final Strategy strategy;

    public LoadBalancedNanoPoolDataSource(ConnectionPoolDataSource[] sources,
            Configuration config) {
        NanoPoolDataSource[] dataSources = new NanoPoolDataSource[sources.length];
        config = new Configuration(config.getState());
        int perPoolSize = Math.max(1, config.getPoolSize() / sources.length);
        config.setPoolSize(perPoolSize);
        for (int i = 0; i < dataSources.length; i++) {
            dataSources[i] = new NanoPoolDataSource(sources[i], config);
        }
        pools = new CopyOnWriteArrayList<NanoPoolDataSource>(dataSources);
        strategy = config.getState().loadBalancingStrategy.buildInstance(pools);
    }

    public Connection getConnection() throws SQLException {
        return strategy.getPool(pools).getConnection();
    }

    public Connection getConnection(String username, String password) throws SQLException {
        throw new UnsupportedOperationException("Not supported.");
    }

    public NanoPoolManagementMBean getMXBean() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
