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
