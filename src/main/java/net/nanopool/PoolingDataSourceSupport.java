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
    protected final CasArray connectors;
    protected final ContentionHandler contentionHandler;

    PoolingDataSourceSupport(ConnectionPoolDataSource source,
            CasArray connectors, long timeToLive, 
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

    /**
     * This method will always throw UnsupportedOperationException.
     * @param <T>
     * @param iface
     * @return
     * @throws java.sql.SQLException
     */
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new UnsupportedOperationException("Not supported.");
    }

    /**
     * This method will always throw UnsupportedOperationException.
     * @param iface
     * @return
     * @throws java.sql.SQLException
     */
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        throw new UnsupportedOperationException("Not supported.");
    }
}
