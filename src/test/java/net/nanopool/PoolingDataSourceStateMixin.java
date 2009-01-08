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
