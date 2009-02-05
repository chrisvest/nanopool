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
package net.nanopool.benchmark;

import biz.source_code.miniConnectionPoolManager.MiniConnectionPoolManager;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.c3p0.DataSources;
import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;
import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import net.nanopool.AbstractDataSource;
import net.nanopool.Configuration;
import net.nanopool.NanoPoolDataSource;
import net.nanopool.contention.DefaultContentionHandler;
import org.apache.commons.dbcp.datasources.SharedPoolDataSource;
import org.junit.Test;

public class Benchmark {
    private static PoolFactory poolFactory;

    @Test
    public void executeBenchmark() throws InterruptedException {
        main(null);
    }
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("--------------------------------");
        System.out.println("  thr = thread");
        System.out.println("  con = connection");
        System.out.println("  cyc = connect-query-close cycle");
        System.out.println("  tot = total");
        System.out.println("  sec = a second");
        System.out.println("--------------------------------");

        System.out.println("### Testing NanoPool");
        poolFactory = new PoolFactory() {
            public DataSource buildPool(ConnectionPoolDataSource cpds, int size, long ttl) {
                return new NanoPoolDataSource(cpds, buildConfig(size, ttl));
            }

            public void closePool(DataSource pool) {
                if (pool instanceof NanoPoolDataSource) {
                    List<SQLException> sqles = ((NanoPoolDataSource)pool).shutdown();
                    for (SQLException sqle : sqles) {
                        sqle.printStackTrace();
                    }
                }
            }
        };
        runTestSet();

        System.out.println("### Testing C3P0");
        poolFactory = new PoolFactory() {
            public DataSource buildPool(ConnectionPoolDataSource cpds, int size, long ttl) {
                ComboPooledDataSource cmds = new ComboPooledDataSource();
                try {
                    cmds.setConnectionPoolDataSource(cpds);
                    cmds.setMaxPoolSize(size);
                    return cmds;
                } catch (PropertyVetoException ex) {
                    throw new RuntimeException(ex);
                }
            }

            public void closePool(DataSource pool) {
                try {
                    DataSources.destroy(pool);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        };
        runTestSet();

        System.out.println("### Testing Commons DBCP");
        poolFactory = new PoolFactory() {
            public DataSource buildPool(ConnectionPoolDataSource cpds, int size, long ttl) {
                SharedPoolDataSource spds = new SharedPoolDataSource();
                spds.setConnectionPoolDataSource(cpds);
                spds.setMaxActive(size);
                return spds;
            }

            public void closePool(DataSource pool) {
                SharedPoolDataSource spds = (SharedPoolDataSource)pool;
                try {
                    spds.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
        runTestSet();

        System.out.println("### Testing MiniConnectionPoolManager");
        poolFactory = new PoolFactory() {
            public DataSource buildPool(ConnectionPoolDataSource cpds, int size, long ttl) {
                final MiniConnectionPoolManager mcpm = new MiniConnectionPoolManager(cpds, size);
                return new AbstractDataSource() {
                    public Connection getConnection() throws SQLException {
                        return mcpm.getConnection();
                    }

                    @Override
                    public List<SQLException> shutdown() {
                        List<SQLException> sqles = new ArrayList<SQLException>();
                        try {
                            mcpm.dispose();
                        } catch (SQLException ex) {
                            sqles.add(ex);
                        }
                        return sqles;
                    }
                };
            }

            public void closePool(DataSource pool) {
                AbstractDataSource ads = (AbstractDataSource)pool;
                List<SQLException> sqles = ads.shutdown();
                for (SQLException sqle : sqles) {
                    sqle.printStackTrace();
                }
            }
        };
        runTestSet();
    }
    
    private static void runTestSet() throws InterruptedException {
        try {
            benchmark(50, 20);
            benchmark(2, 1);
        System.out.println("------Warmup's over-------------");
            for (int connections = 1; connections <= 10; connections++) {
                benchmark(connections, connections);
                benchmark(connections * 2, connections);
                benchmark(connections, connections * 2);
            }
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        System.out.println("--------------------------------");
    }

    private static ConnectionPoolDataSource newCpds() {
        MysqlConnectionPoolDataSource myCpds =
                new MysqlConnectionPoolDataSource();
        myCpds.setUser("root");
        myCpds.setPassword("");
        myCpds.setDatabaseName("test");
        myCpds.setPort(3306);
        myCpds.setServerName("localhost");
        return myCpds;
    }

    private static Configuration buildConfig(int poolSize, long ttl) {
        Configuration conf = new Configuration();
        conf.setPoolSize(poolSize)
            .setTimeToLive(ttl)
            .setContentionHandler(new DefaultContentionHandler(false, 0));
        return conf;
    }

    private static DataSource buildPool(ConnectionPoolDataSource cpds, int size) {
        long ttl = 300000; // five minutes
        return poolFactory.buildPool(cpds, size, ttl);
    }

    private static void shutdown(DataSource pool) {
        poolFactory.closePool(pool);
    }

    private static void benchmark(int threads, int poolSize) throws InterruptedException {
        ConnectionPoolDataSource cpds = newCpds();
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch startSignal = new CountDownLatch(1);
        DataSource pool = buildPool(cpds, poolSize);
        int runningTime = 5000;

        Worker[] workers = new Worker[threads];
        for (int i = 0; i < threads; i++) {
            Worker worker = new Worker(pool, startSignal);
            executor.execute(worker);
            workers[i] = worker;
        }

        startSignal.countDown();
        try {
            Thread.sleep(runningTime);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }

        executor.shutdownNow();
        executor.awaitTermination(runningTime, TimeUnit.MILLISECONDS);
        
        long sumThroughPut = 0;
        for (Worker worker : workers) {
            sumThroughPut += worker.hits;
        }
        double totalThroughput = sumThroughPut / 60.0;
        double throughputPerThread = totalThroughput / threads;
        double throughputPerConnection = totalThroughput / poolSize;
        System.out.printf("%s thrs, %s cons: " +
        		"%.2f cyc/sec/tot, %.2f cyc/sec/thr, %.2f cyc/sec/con\n",
                threads, poolSize, totalThroughput,
                throughputPerThread, throughputPerConnection);

        shutdown(pool);
    }
    
    private static class Worker implements Runnable {
        public volatile long hits = 0;
        private final DataSource ds;
        private final CountDownLatch latch;

        public Worker(DataSource ds, CountDownLatch startLatch) {
            this.ds = ds;
            this.latch = startLatch;
        }
        
        public void run() {
            long count = 0;
            try {
                latch.await();
                while (!Thread.interrupted()) {
                    Connection con = ds.getConnection();
                    closeSafely(con);
                    count++;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            hits = count;
        }

        private void closeSafely(Connection con) {
            try {
                con.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
}
