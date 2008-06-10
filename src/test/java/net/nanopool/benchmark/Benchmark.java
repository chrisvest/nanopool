package net.nanopool.benchmark;

import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import net.nanopool.DefaultContentionHandler;
import net.nanopool.NanoPoolDataSource;
import net.nanopool.cas.CasArray;
import net.nanopool.cas.StrongAtomicCasArray;
import net.nanopool.cas.StrongStripedAtomicCasArray;
import net.nanopool.cas.WeakAtomicCasArray;
import net.nanopool.cas.WeakStripedAtomicCasArray;
import org.junit.Test;

public class Benchmark {
    @Test
    public void executeBenchmark() {
        main(null);
    }
    
    public static void main(String[] args) {
        System.out.println("--------------------------------");
        System.out.println("  thr = thread");
        System.out.println("  con = connection");
        System.out.println("  cyc = connect-query-close cycle");
        System.out.println("  tot = total");
        System.out.println("  sec = a second");
        System.out.println("--------------------------------");
        try {
            runTestSet(50, 20);
            runTestSet(2, 1);
        System.out.println("------Warmup's over-------------");
            for (int connections = 1; connections <= 10; connections++) {
                runTestSet(connections, connections);
//                runTestSet(connections * 2, connections);
//                System.out.println(" --+--");
            }
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        System.out.println("--------------------------------");
    }
    
    private static void runTestSet(int threads, int poolSize) throws InterruptedException {
//        benchmark(threads, new StrongAtomicCasArray(poolSize));
//        benchmark(threads, new WeakAtomicCasArray(poolSize));
        benchmark(threads, new StrongStripedAtomicCasArray(poolSize));
//        benchmark(threads, new WeakStripedAtomicCasArray(poolSize));
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

    private static void benchmark(int threads, CasArray casArray) throws InterruptedException {
        ConnectionPoolDataSource cpds = newCpds();
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch startSignal = new CountDownLatch(1);
        int timeToLive = 300000; // five minutes
        NanoPoolDataSource npds = new NanoPoolDataSource(
                cpds, casArray, timeToLive, new DefaultContentionHandler(false));
        Worker[] workers = new Worker[threads];
        for (int i = 0; i < threads; i++) {
            Worker worker = new Worker(npds, startSignal);
            executor.execute(worker);
            workers[i] = worker;
        }
        startSignal.countDown();
        try {
            Thread.sleep(60000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        executor.shutdownNow();
        executor.awaitTermination(60, TimeUnit.SECONDS);
        long sumThroughPut = 0;
        for (Worker worker : workers) {
            sumThroughPut += worker.hits;
        }
        int poolSize = casArray.length();
        String casArrayImplName = casArray.getClass().getSimpleName();
        double totalThroughput = sumThroughPut / 60.0;
        double throughputPerThread = totalThroughput / threads;
        double throughputPerConnection = totalThroughput / poolSize;
        System.out.printf("%s thrs, %s cons: " +
        		"%.2f cyc/sec/tot, %.2f cyc/sec/thr, %.2f cyc/sec/con | %s\n",
                threads, poolSize, totalThroughput,
                throughputPerThread, throughputPerConnection,
                casArrayImplName);
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
                    Statement st = con.createStatement();
                    ResultSet result = st.executeQuery("select now()");
                    closeSafely(con, st, result);
                    count++;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            hits = count;
        }

        private void closeSafely(Connection con, Statement st, ResultSet result) {
            try {
                result.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            try {
                st.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            try {
                con.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
}
