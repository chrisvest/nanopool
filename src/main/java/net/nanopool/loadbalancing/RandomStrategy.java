package net.nanopool.loadbalancing;

import java.util.concurrent.CopyOnWriteArrayList;
import net.nanopool.CheapRandom;
import net.nanopool.NanoPoolDataSource;

/**
 *
 * @author cvh
 */
public class RandomStrategy implements Strategy {
    private static final CheapRandom rand = new CheapRandom();
    public static final RandomStrategy INSTANCE = new RandomStrategy();

    public Strategy buildInstance(CopyOnWriteArrayList<NanoPoolDataSource> dataSources) {
        return INSTANCE;
    }

    public NanoPoolDataSource getPool(CopyOnWriteArrayList<NanoPoolDataSource> dataSources) {
        return dataSources.get(rand.nextAbs(0, dataSources.size()));
    }

}
