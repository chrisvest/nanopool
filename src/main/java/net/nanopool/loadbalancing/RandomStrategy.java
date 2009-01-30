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
