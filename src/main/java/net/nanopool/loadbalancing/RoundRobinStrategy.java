package net.nanopool.loadbalancing;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import net.nanopool.NanoPoolDataSource;

/**
 *
 * @author cvh
 */
public class RoundRobinStrategy implements Strategy {
    private final AtomicInteger cursor = new AtomicInteger();

    public Strategy buildInstance(CopyOnWriteArrayList<NanoPoolDataSource> dataSources) {
        return new RoundRobinStrategy();
    }

    public NanoPoolDataSource getPool(CopyOnWriteArrayList<NanoPoolDataSource> dataSources) {
        int idx = cursor.getAndIncrement();
        return dataSources.get(idx % dataSources.size());
    }
}
