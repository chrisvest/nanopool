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

import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.sql.ConnectionPoolDataSource;
import org.junit.After;

/**
 * Abstract base class for NanoPoolDataSource test cases.
 * @author cvh
 */
public abstract class NanoPoolTestBase {
    protected NanoPoolDataSource pool;

    protected NanoPoolDataSource npds() throws SQLException {
        ConnectionPoolDataSource source = buildCpds();
        Configuration config = buildConfig();
        return buildNpds(source, config);
    }

    protected ConnectionPoolDataSource buildCpds() throws SQLException {
        MysqlConnectionPoolDataSource source =
            new MysqlConnectionPoolDataSource();
        source.setServerName("localhost");
        source.setPort(3306);
        source.setDatabaseName("test");
        source.setUser("root");
        source.setPassword("");
        
        // timeouts:
        source.setLoginTimeout(5 /*seconds*/);
        source.setConnectTimeout(5000 /*milliseconds*/);
        source.setSocketTimeout(5000 /*milliseconds*/);

        return source;
    }

    protected Configuration buildConfig() {
        return new Configuration().setPoolSize(10).setTimeToLive(300000);
    }

    protected NanoPoolDataSource buildNpds(ConnectionPoolDataSource source,
            Configuration config) {
        return new NanoPoolDataSource(source, config);
    }

    @After
    public void closePool() throws SQLException {
        if (pool != null) {
            List<SQLException> sqles = pool.close();
            if (!sqles.isEmpty()) {
                for (SQLException sqle : sqles) {
                    sqle.printStackTrace();
                }
                throw sqles.get(0);
            }
        }
    }

    static Thread killMeLaterCheck(
            final AtomicBoolean finishedMarker,
            final long sleepTime,
            final Exception exception) {
        final Thread thisThread = Thread.currentThread();
        Thread killer = new Thread(new Runnable() {
            @SuppressWarnings("deprecation")
            public void run() {
                try {
                    Thread.sleep(sleepTime);
                    if (!finishedMarker.get()) {
                        exception.setStackTrace(thisThread.getStackTrace());
                        thisThread.stop(exception);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }, thisThread.getName() + "-killer");
        killer.setDaemon(true);
        killer.start();
        return killer;
    }
}
