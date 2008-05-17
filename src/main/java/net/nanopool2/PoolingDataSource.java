package net.nanopool2;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.ConnectionPoolDataSource;

import net.nanopool.Log;
import net.nanopool2.cas.CasArray;

public class PoolingDataSource extends PoolingDataSourceSupport {
    private final Connector ticket;
    private final CasArray<Connector> connectors;
    private final CheapRandom rand;
    
    public PoolingDataSource(ConnectionPoolDataSource source, int poolSize,
            long timeToLive, Log log) {
        super(source, poolSize, timeToLive, log);
        ticket = new Connector();
        connectors = newCasArray(poolSize);
        rand = new CheapRandom();
    }

    public Connection getConnection() throws SQLException {
        int start = StrictMath.abs(rand.nextInt()) % poolSize;
        int idx = start;
        while (true) {
            if (idx == poolSize)
                idx = 0;
            Connector con = connectors.get(idx);
            if (con != ticket) {
                // we might have gotten one
                if (connectors.cas(idx, ticket, con)) { // reserve it
                    if (con == null) {
                        con = new Connector(source, connectors, idx, timeToLive);
                    }
                    return con.getConnection();
                }
            }
            ++idx;
            if (idx == start)
                handleContention();
        }
    }

    public Connection getConnection(String username, String password)
            throws SQLException {
        throw new UnsupportedOperationException();
    }

}
