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

import net.nanopool.contention.ContentionHandler;
import javax.sql.DataSource;

import net.nanopool.cas.CasArray;

import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;

public abstract class PoolingDataSourceStateMixin implements DataSourceState {
    protected final Factory<ContentionHandler> contentionHandlerFactory;
    protected final Factory<CasArray<Connector>> casArrayFactory;
    protected final long timeout;
    
    public PoolingDataSourceStateMixin(
            Factory<ContentionHandler> contentionHandlerFactory,
            Factory<CasArray<Connector>> casArrayFactory,
            long timeout) {
        this.contentionHandlerFactory = contentionHandlerFactory;
        this.casArrayFactory = casArrayFactory;
        this.timeout = timeout;
    }
    
    public DataSource createSource() {
        MysqlConnectionPoolDataSource cpds = new MysqlConnectionPoolDataSource();
        cpds.setUser("root");
        cpds.setPassword("");
        cpds.setServerName("localhost");
        Configuration config = new Configuration();
        config.setContentionHandler(contentionHandlerFactory.create())
              .setPoolSize(10)
              .setTimeToLive(timeout);
        return new NanoPoolDataSource(cpds, config);
    }
}
