package net.nanopool2;

import java.io.PrintWriter;
import java.sql.SQLException;

import javax.sql.ConnectionPoolDataSource;

import net.nanopool.DataSourceSupport;
import net.nanopool.Log;
import net.nanopool2.cas.CasArray;
import net.nanopool2.cas.StrongAtomicCasArray;

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
    
    protected void handleContention() throws SQLException {
        log.warn("Contention warning for the %s connection pool.", this);
        Thread.yield();
    }

    protected CasArray<Connector> newCasArray(int poolSize) {
        return new StrongAtomicCasArray<Connector>(poolSize);
    }
}
