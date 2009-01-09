package net.nanopool.loadbalancing;

import java.util.concurrent.CopyOnWriteArrayList;
import net.nanopool.NanoPoolDataSource;

/**
 *
 * @author cvh
 */
public interface Strategy {
    Strategy buildInstance(CopyOnWriteArrayList<NanoPoolDataSource> dataSources);
    NanoPoolDataSource getPool(CopyOnWriteArrayList<NanoPoolDataSource> dataSources);
}
