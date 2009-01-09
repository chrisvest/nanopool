package net.nanopool;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.concurrent.locks.ReentrantLock;
import javax.sql.ConnectionPoolDataSource;

import net.nanopool.cas.CasArray;

public abstract class PoolingDataSourceSupport extends AbstractDataSource {
    final ConnectionPoolDataSource source;
    final ReentrantLock resizingLock = new ReentrantLock();
    final State state;
    volatile CasArray<Connector> connectors;
    volatile Connector[] allConnectors;

    PoolingDataSourceSupport(ConnectionPoolDataSource source,
            Configuration config) {
        this.source = source;
        this.state = config.getState();
        this.connectors = state.buildCasArray();
        this.allConnectors = new Connector[connectors.length()];
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return source.getLogWriter();
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return source.getLoginTimeout();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        source.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        source.setLoginTimeout(seconds);
    }
}
