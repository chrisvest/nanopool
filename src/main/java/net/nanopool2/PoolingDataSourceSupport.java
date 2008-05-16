package net.nanopool2;

import java.io.PrintWriter;
import java.sql.SQLException;

import javax.sql.ConnectionPoolDataSource;

import net.nanopool.DataSourceSupport;
import net.nanopool.Log;

public abstract class PoolingDataSourceSupport extends DataSourceSupport {
    protected final ConnectionPoolDataSource source;
    protected final int poolSize;
    protected final long timeToLive;

    public PoolingDataSourceSupport(ConnectionPoolDataSource source,
            int poolSize, long timeToLive, Log log) {
        super(log);
        this.source = source;
        this.poolSize = poolSize;
        this.timeToLive = timeToLive;
    }

    public PrintWriter getLogWriter() throws SQLException {
        return source.getLogWriter();
    }

    public int getLoginTimeout() throws SQLException {
        return source.getLoginTimeout();
    }

    public void setLogWriter(PrintWriter out) throws SQLException {
        source.setLogWriter(out);
    }

    public void setLoginTimeout(int seconds) throws SQLException {
        source.setLoginTimeout(seconds);
    }
}
