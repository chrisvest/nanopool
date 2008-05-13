package net.nanopool;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

import javax.sql.ConnectionPoolDataSource;

public abstract class PoolingDataSourceSupport extends DataSourceSupport {
    protected final ConnectionPoolDataSource cpSource;
    protected final Queue<Connector> queue;
    protected final Semaphore semaphore;
    
    public PoolingDataSourceSupport(ConnectionPoolDataSource source,
            int size, long timeout, Log log) {
        super(log);
        cpSource = source;
        semaphore = new Semaphore(size);
        queue = new ConcurrentLinkedQueue<Connector>();
        for (int i = 0; i < size; i++) {
            queue.add(new Connector(source, timeout, queue, semaphore, log));
        }
    }

    public int getLoginTimeout() throws SQLException {
        return cpSource.getLoginTimeout();
    }

    public PrintWriter getLogWriter() throws SQLException {
        return cpSource.getLogWriter();
    }

    public void setLoginTimeout(int seconds) throws SQLException {
        cpSource.setLoginTimeout(seconds);
    }

    public void setLogWriter(PrintWriter out) throws SQLException {
        cpSource.setLogWriter(out);
    }
}
