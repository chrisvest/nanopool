package net.nanopool;

import java.io.PrintWriter;
import java.sql.SQLException;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;

import net.nanopool.cas.CasArray;

public abstract class PoolingDataSourceSupport implements DataSource {
    protected final ConnectionPoolDataSource source;
    protected final int poolSize;
    protected final long timeToLive;
    protected final CasArray<Connector> connectors;
    protected final ContentionHandler contentionHandler;

    public PoolingDataSourceSupport(ConnectionPoolDataSource source,
            CasArray<Connector> connectors, long timeToLive, 
            ContentionHandler contentionHandler) {
        this.source = source;
        this.poolSize = connectors.length();
        this.timeToLive = timeToLive;
        this.connectors = connectors;
        this.contentionHandler = contentionHandler;
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
