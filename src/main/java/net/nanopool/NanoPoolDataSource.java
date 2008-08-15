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
    
    /**
     * Create a new {@link NanoPoolDataSource} based on the specified
     * {@link ConnectionPoolDataSource}, and with the specified pool size and
     * time-to-live.
     * The pool will use a {@link DefaultContentionHandler} and create its own
     * {@link CasArray} that isn't shared with anyone else.
     * @param source the {@link ConnectionPoolDataSource} instance that will
     * provide the raw connections to this pool. You usually get these instances
     * from your JDBC driver. If your driver of choice does not have an
     * implementation for this interface, then you either have to write it
     * yourself or give up and cry in a corner. Thankfully, most modern JDBC
     * drivers support this feature of the JDBC specification.
     * @param poolSize
     * @param timeToLive
     */
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

    /**
     * Lease a new {@link Connection} from the pool.
     * This method will attempt to reserve one of the connections that is
     * available from the pool. If the pool have not been completely saturated
     * with connections, then a new connection will be created at an available
     * slot and immediately leased.
     * If there are no connections available to lease, then we will sit down
     * around the camp fire and wait until one <em>becomes</em> available, and
     * the exact procedure of how this happens is dictated by this pools
     * configured {@link ContentionHandler} (which does a {@link Thread#yield()}
     * by default).
     * @return A new/old {@link Connection} from the pool. This object is
     * guaranteed to only be available to a single thread <em>provided</em>
     * that you yourself do not share it amongst more than one thread (and I
     * will spank you if you do -- hard) <em>and</em> you are not keeping
     * connection objects around after you close them. And be sure that you
     * close your connection when you're done with it - it will not return to
     * the pool if you forget this!
     * @throws SQLException Thrown if we tried to establish a sparkly-new
     * connection with the configured {@link ConnectionPoolDataSource} and it
     * <em>fails!</em>
     */
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
