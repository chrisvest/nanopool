/*
   Copyright 2008-2009 Christian Vest Hansen

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package net.nanopool;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.sql.ConnectionPoolDataSource;
import net.nanopool.loadbalancing.Strategy;

/**
 * The LoadBalancedNanoPoolDataSource is like a proxy for a collection of
 * {@link NanoPoolDataSource}s, and distributes the getConnection requests
 * among these pools in accord to some {@link Strategy}.
 * @author cvh
 * @since 1.0
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

    public Connection getConnection(String username, String password)
            throws SQLException {
        throw new UnsupportedOperationException("Not supported.");
    }

    public NanoPoolManagementMBean getMXBean() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
