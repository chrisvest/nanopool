package net.nanopool2;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.ConnectionPoolDataSource;

import net.nanopool2.cas.CasArray;

public class FsmMixin {

    public Connection getConnection(
            Connector ticket,
            CasArray<Connector> connectors,
            ConnectionPoolDataSource source,
            CheapRandom rand,
            int poolSize,
            long timeToLive,
            ContentionHandler contentionHandler) throws SQLException {
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
                contentionHandler.handleContention();
        }
    }
}
