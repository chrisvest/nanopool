package net.nanopool;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.ConnectionPoolDataSource;

import net.nanopool.cas.CasArray;
import net.nanopool.hooks.Hook;

final class FsmMixin {
    static final Connector reservationMarker = new Connector();
    static final Connector shutdownMarker = new Connector();

    static Connection getConnection(
            final CasArray<Connector> connectors,
            final ConnectionPoolDataSource source,
            final CheapRandom rand,
            final Connector[] allConnectors,
            final State state) throws SQLException {
        runHooks(state.preConnectHooks, source, null, null);
        final int poolSize = connectors.length();
        final long ttl = state.ttl;
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
                        con = new Connector(source, connectors, idx, ttl);
                        allConnectors[idx] = con;
                    }
                    try {
                        Connection connection = con.getConnection(
                                state.preReleaseHooks, state.postReleaseHooks,
                                state.connectionInvalidationHooks);
                        runHooks(state.postConnectHooks,
                                source, connection, null);
                        return connection;
                    } catch (SQLException sqle) {
                        runHooks(state.postConnectHooks,
                                source, null, sqle);
                    }
                }
                con = connectors.get(idx);
            }
            ++idx;
            if (idx == start)
                state.contentionHandler.handleContention();
        }
    }

    static List<SQLException> shutdown(
            final CasArray<Connector> connectors) {
        List<SQLException> caughtExceptions = new ArrayList<SQLException>();
        
        iterate_connectors: for (int i = 0; i < connectors.length(); i++) {
            Connector con = null;
            do { // try snatching it and eagerly mark it as shut down.
                con = connectors.get(i);
                // avoid CAS if already shut down:
                if (con == shutdownMarker) continue iterate_connectors;
            } while(!connectors.cas(i, shutdownMarker, con));
            if (con != reservationMarker && con != null) {
                try {
                    con.invalidate();
                } catch (SQLException e) {
                    caughtExceptions.add(e);
                }
            }
        }
        
        return caughtExceptions;
    }

    static int countOpenConnections(CasArray<Connector> connectors) {
        int openCount = 0;
        for (Connector cn : connectors) {
            if (cn != null
                && cn != reservationMarker
                && cn != shutdownMarker) {
                openCount++;
            }
        }
        return openCount;
    }

    static void runHooks(Cons<Hook> hooks,
            ConnectionPoolDataSource source, Connection con, SQLException sqle) {
        while (hooks != null) {
            hooks.first.run(source, con, sqle);
            hooks = hooks.rest;
        }
    }
}
