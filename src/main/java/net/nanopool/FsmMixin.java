package net.nanopool;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.ConnectionPoolDataSource;

import net.nanopool.cas.CasArray;

public class FsmMixin {

    public Connection getConnection(
            final Connector ticket,
            final CasArray<Connector> connectors,
            final ConnectionPoolDataSource source,
            final CheapRandom rand,
            final int poolSize,
            final long timeToLive,
            final ContentionHandler contentionHandler) throws SQLException {
        final int start = StrictMath.abs(rand.nextInt()) % poolSize;
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
                contentionHandler.handleContention();
        }
    }
}
