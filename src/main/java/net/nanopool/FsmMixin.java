/*
   Copyright 2008-2009 Christian Vest Hansen

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package net.nanopool;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.ConnectionPoolDataSource;

import net.nanopool.cas.CasArray;
import net.nanopool.cas.ResizableCasArray;
import net.nanopool.hooks.EventType;
import net.nanopool.hooks.Hook;

final class FsmMixin {
    static final Connector reservationMarker = new Connector();
    static final Connector shutdownMarker = new Connector();
    static final Connector outdatedMarker = new Connector();

    static Connection getConnection(
            final CasArray<Connector> connectors,
            final ConnectionPoolDataSource source,
            final CheapRandom rand,
            final Connector[] allConnectors,
            final State state) throws SQLException {
        runHooks(state.preConnectHooks, EventType.preConnect,
                source, null, null);
        final int poolSize = connectors.length();
        final long ttl = state.ttl;
        final int start = StrictMath.abs(rand.nextInt()) % poolSize;
        int idx = start;
        int contentionCounter = 0;
        while (true) {
            if (idx == poolSize)
                idx = 0;
            Connector con = connectors.get(idx);
            if (con == outdatedMarker) throw CasArrayOutdatedException.INSTANCE;
            while (con != reservationMarker) {
                if (con == shutdownMarker)
                    throw new IllegalStateException("Connection pool is shut down.");
                // we might have gotten one
                if (connectors.cas(idx, reservationMarker, con)) { // reserve it
                    if (con == null) {
                        con = new Connector(source, connectors, idx, ttl);
                        // this is safe bc. con is properly constructed:
                        allConnectors[idx] = con;
                    }
                    try {
                        Connection connection = con.getConnection(
                                state.preReleaseHooks, state.postReleaseHooks,
                                state.connectionInvalidationHooks);
                        runHooks(state.postConnectHooks, EventType.postConnect,
                                source, connection, null);
                        return connection;
                    } catch (SQLException sqle) {
                        allConnectors[idx] = null;
                        connectors.cas(idx, null, reservationMarker);
                        runHooks(state.postConnectHooks, EventType.postConnect,
                                source, null, sqle);
                        throw sqle;
                    }
                }
                con = connectors.get(idx);
            }
            ++idx;
            if (idx == start)
                state.contentionHandler.handleContention(++contentionCounter);
        }
    }

    static List<SQLException> shutdown(
            final CasArray<Connector> connectors) {
        List<SQLException> caughtExceptions = new ArrayList<SQLException>();
        
        iterate_connectors: for (int i = 0; i < connectors.length(); i++) {
            Connector con = null;
            do { // try snatching it and eagerly mark it as shut down.
                con = connectors.get(i);
                if (con == outdatedMarker)
                    throw CasArrayOutdatedException.INSTANCE;
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

    static void resizePool(PoolingDataSourceSupport pds, int newSize) {
        if (pds.connectors.get(0) == shutdownMarker)
            throw new IllegalStateException("Connection pool is shut down.");
        if (!(pds.connectors instanceof ResizableCasArray)) {
            throw new IllegalStateException(
                    "The CasArray in use does not support resizing.");
        }

        pds.resizingLock.lock();
        try {
            ResizableCasArray<Connector> rca = (ResizableCasArray)pds.connectors;
            int len = rca.length();
            if (len == newSize) return;
            CasArray newCA = pds.state.buildCasArray(newSize);
            for (int i = 0; i < len; i++) {
                newCA.cas(i, reservationMarker, null);
            }

            if (len < newSize) {
                // pool growth
                Connector[] newAllArray = new Connector[newSize];
                System.arraycopy(pds.allConnectors, 0, newAllArray, 0, len);
                pds.allConnectors = newAllArray;
                pds.connectors = newCA;
                rca.setCasDelegate(newCA);
                for (int i = 0; i < len; i++) {
                    newCA.cas(i, rca.get(i), reservationMarker);
                }
            } else {
                // pool shrink
                Connector[] newAllArray = new Connector[newSize];
                ArrayList<Integer> nilIndices = new ArrayList<Integer>();
                pds.connectors = newCA;
                for (int i = 0; i < newSize; i++) {
                    Connector cn = rca.get(i);
                    if (cn == null) {
                        nilIndices.add(i);
                    } else {
                        newCA.cas(i, cn, reservationMarker);
                    }
                }
                for (int i = newSize; i < len; i++) {
                    rca.cas(i, rca.get(i), outdatedMarker);
                }
                System.arraycopy(pds.allConnectors, 0, newAllArray, 0, newSize);
                pds.allConnectors = newAllArray;
                for (int i : nilIndices) {
                    newCA.cas(i, null, reservationMarker);
                }
            }
        } finally {
            pds.resizingLock.unlock();
        }
    }

    static int countOpenConnections(CasArray<Connector> connectors) {
        int openCount = 0;
        for (Connector cn : connectors) {
            if (cn == outdatedMarker) throw CasArrayOutdatedException.INSTANCE;
            if (cn != null
                && cn != reservationMarker
                && cn != shutdownMarker) {
                openCount++;
            }
        }
        return openCount;
    }

    static void runHooks(Cons<Hook> hooks, EventType type,
            ConnectionPoolDataSource source, Connection con, SQLException sqle) {
        while (hooks != null) {
            hooks.first.run(type, source, con, sqle);
            hooks = hooks.rest;
        }
    }
}
