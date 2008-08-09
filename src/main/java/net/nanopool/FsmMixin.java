package net.nanopool;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.ConnectionPoolDataSource;

import net.nanopool.cas.CasArray;

public final class FsmMixin {
    static final Connector reservationMarker = new Connector();
    static final Connector shutdownMarker = new Connector();

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
            while (con != reservationMarker) {
                if (con == shutdownMarker)
                    throw new IllegalStateException("Connection pool is shut down.");
                // we might have gotten one
                if (connectors.cas(idx, reservationMarker, con)) { // reserve it
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

    public List<SQLException> shutdown(
            final CasArray<Connector> connectors,
            final int poolSize) {
        List<SQLException> caughtExceptions = new ArrayList<SQLException>();
        
        iterate_connectors: for (int i = 0; i < poolSize; i++) {
            Connector con = null;
            do { // try snatching it and eagerly mark it as shut down.
                con = connectors.get(i);
                // avoid CAS if already shut down:
                if (con == shutdownMarker) continue iterate_connectors;
            } while(!connectors.cas(i, shutdownMarker, con));
            if (con != reservationMarker) {
                try {
                    con.invalidate();
                } catch (SQLException e) {
                    caughtExceptions.add(e);
                }
            }
        }
        
        return caughtExceptions;
    }
}
