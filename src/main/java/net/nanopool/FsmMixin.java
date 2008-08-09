package net.nanopool;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.ConnectionPoolDataSource;

import net.nanopool.cas.CasArray;

public class FsmMixin {
    protected final Connector reservationTicket = new Connector();;
    protected final Connector shutdownMarker = new Connector();;

    public Connection getConnection(
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
            while (con != reservationTicket) {
                if (con == shutdownMarker)
                    throw new IllegalStateException("Connection pool is shut down.");
                // we might have gotten one
                if (connectors.cas(idx, reservationTicket, con)) { // reserve it
                    if (con == null) {
                        con = new Connector(source, connectors, idx, timeToLive);
                    }
                    return con.getConnection();
                }
                con = connectors.get(idx);
            }
            ++idx;
            if (idx == start)
                contentionHandler.handleContention();
        }
    }
}
