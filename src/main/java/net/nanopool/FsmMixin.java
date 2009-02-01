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

import net.nanopool.hooks.EventType;
import net.nanopool.hooks.Hook;

final class FsmMixin {
    static final CheapRandom rand = new CheapRandom();
    static final String MSG_SHUT_DOWN = "Connection pool is shut down.";
    static final String MSG_RESIZING = "The CasArray in use does not support resizing.";

    static Connection getConnection(
            final PoolingDataSourceSupport pds) throws SQLException {
        runHooks(pds.state.preConnectHooks, EventType.preConnect,
                pds.source, null, null);
        final Connector[] connectors = pds.connectors;
        if (connectors == null)
            throw new IllegalStateException(MSG_SHUT_DOWN);
        final int poolSize = connectors.length;
        final State state = pds.state;
        final long ttl = pds.state.ttl;
        final int start = StrictMath.abs(rand.nextInt()) % poolSize;
        int idx = start;
        int contentionCounter = 0;
        while (true) {
            if (idx == poolSize)
                idx = 0;
            Connector con = connectors[idx];
            int st = con.state.get();
            while (st != Connector.RESERVED) {
                if (st == Connector.OUTDATED)
                    throw CasArrayOutdatedException.INSTANCE;
                if (st == Connector.SHUTDOWN)
                    throw new IllegalStateException(MSG_SHUT_DOWN);
                // we might have gotten one
                if (con.state.compareAndSet(Connector.AVAILABLE, Connector.RESERVED)) { // reserve it
                    try {
                        Connection connection = con.getConnection(
                                state.preReleaseHooks, state.postReleaseHooks,
                                state.connectionInvalidationHooks);
                        runHooks(state.postConnectHooks, EventType.postConnect,
                                pds.source, connection, null);
                        return connection;
                    } catch (SQLException sqle) {
                        try {
                            con.invalidate();
                        } finally {
                            con.state.set(Connector.AVAILABLE);
                            runHooks(state.postConnectHooks, EventType.postConnect,
                                    pds.source, null, sqle);
                        }
                        throw sqle;
                    }
                }
                st = con.state.get();
            }
            ++idx;
            if (idx == start)
                state.contentionHandler.handleContention(++contentionCounter);
        }
    }

    static List<SQLException> shutdown(Connector[] connectors) {
        List<SQLException> caughtExceptions = new ArrayList<SQLException>();
        if (connectors == null) return caughtExceptions;

        for (Connector con : connectors) {
            int st = con.state.get();
            con.state.set(Connector.SHUTDOWN);
            if (st == Connector.OUTDATED)
                throw CasArrayOutdatedException.INSTANCE;
            if (st != Connector.RESERVED) {
                try {
                    con.invalidate();
                } catch (SQLException ex) {
                    caughtExceptions.add(ex);
                }
            }
        }
        
        return caughtExceptions;
    }

    static void resizePool(PoolingDataSourceSupport pds, int newSize) {/*
        if (pds.connectors.get(0) == shutdownMarker)
            throw new IllegalStateException(MSG_SHUT_DOWN);
        if (!(pds.connectors instanceof ResizableCasArray)) {
            throw new IllegalStateException(MSG_RESIZING);
        }

        pds.resizingLock.lock();
        try {
            ResizableCasArray<Connector> rca = (ResizableCasArray)pds.connectors;
            int len = rca.length();
            if (len == newSize) return;
            CasArray newCA = pds.state.buildCasArray(newSize);
            for (int i = 0, n = Math.min(len, newSize); i < n; i++) {
                newCA.cas(i, reservationMarker, null);
            }

            if (len < newSize) {
                // pool growth
                Connector[] newAllArray = new Connector[newSize];
                System.arraycopy(pds.allConnectors, 0, newAllArray, 0, len);
                pds.allConnectors = newAllArray;
                pds.connectors = newCA;
                rca.setDelegate(newCA);
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
            for (int i = 0; i < len; i++) {
                rca.setThis(i, outdatedMarker);
            }
        } finally {
            pds.resizingLock.unlock();
        }*/
    }

    static int countOpenConnections(Connector[] connectors) {
        int openCount = 0;
        for (Connector cn : connectors) {
            int state = cn.state.get();
            if (state == Connector.OUTDATED) throw CasArrayOutdatedException.INSTANCE;
            if (state == Connector.AVAILABLE) {
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
