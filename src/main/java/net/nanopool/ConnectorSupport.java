package net.nanopool;

import java.util.Queue;
import java.util.concurrent.Semaphore;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;

public class ConnectorSupport {
    
    protected final ConnectionPoolDataSource source;
    protected final long timeout;
    protected final Queue<Connector> queue;
    protected final Semaphore semaphore;
    protected final Log log;
    
    protected long connectedTime;
    protected PooledConnection connection;

    public ConnectorSupport(ConnectionPoolDataSource source, long timeout,
            Queue<Connector> queue, Semaphore semaphore, Log log) {
        this.source = source;
        this.timeout = timeout;
        this.queue = queue;
        this.semaphore = semaphore;
        this.log = log.subLoggerFor("Connector");
    }
}
