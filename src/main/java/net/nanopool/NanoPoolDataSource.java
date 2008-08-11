package net.nanopool;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.sql.ConnectionPoolDataSource;

import net.nanopool.cas.CasArray;
import net.nanopool.cas.StrongAtomicCasArray;

public final class NanoPoolDataSource extends PoolingDataSourceSupport {
    private final CheapRandom rand;
    private final FsmMixin fsm;
    
    public NanoPoolDataSource(ConnectionPoolDataSource source, int poolSize,
            long timeToLive) {
        this(source, new StrongAtomicCasArray<Connector>(poolSize), timeToLive,
                new DefaultContentionHandler());
    }
    
    public NanoPoolDataSource(ConnectionPoolDataSource source,
            CasArray<Connector> connectors, long timeToLive,
            ContentionHandler contentionHandler) {
        super(source, connectors, timeToLive, contentionHandler);
        rand = new CheapRandom();
        fsm = new FsmMixin();
    }

    public Connection getConnection() throws SQLException {
        return fsm.getConnection(connectors, source, rand,
                poolSize, timeToLive, contentionHandler);
    }
    
    /**
     * Initiate a shutdown sequence on the pool.
     * This method will return before the pool has completely shutdown, however
     * the pool <strong>will</strong> be unable to grant any new connections.
     * Calling {@link NanoPoolDataSource#getConnection()} on a shut down pool
     * will result in an {@link IllegalStateException}.
     * Calling {@link NanoPoolDataSource#shutdown()} on a pool that has already
     * been shut down, has no effect.
     * Connections that are active and in use when the pool is shut down will
     * <strong>not</strong> be forcibly killed. Instead, all active connections
     * will be allowed to operate for as long as they please, until they are
     * closed normally or closed forcibly by the database or similar external
     * factors. Any connections that are not in use, will be marked unavailable
     * and closed. This behavior means that it is (relatively) safe to shut
     * down a pool that is in use.
     * Any {@link SQLException}s encountered during the shut down procedure
     * are aggregated in a {@link List}, and that list is returned as the
     * result of this method, for perusal by client code.
     * @return A {@link List} of all {@link SQLException}s caught when shutting
     * the pool down.
     * @since 1.0
     */
    public List<SQLException> shutdown() {
        return fsm.shutdown(connectors, poolSize);
    }
    
    /**
     * This method will always throw UnsupportedOperationException.
     * @param username not used
     * @param password not used
     * @return never returns
     * @throws SQLException never throws SQLException
     * @throws UnsupportedOperationException always.
     * @since 1.0
     */
    public Connection getConnection(String username, String password)
            throws SQLException {
        throw new UnsupportedOperationException();
    }
}
