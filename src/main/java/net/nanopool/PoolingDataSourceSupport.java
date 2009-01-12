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

import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.concurrent.locks.ReentrantLock;
import javax.sql.ConnectionPoolDataSource;

import net.nanopool.cas.CasArray;

public abstract class PoolingDataSourceSupport extends AbstractDataSource {
    final ConnectionPoolDataSource source;
    final ReentrantLock resizingLock = new ReentrantLock();
    final State state;
    volatile CasArray<Connector> connectors;
    volatile Connector[] allConnectors;

    PoolingDataSourceSupport(ConnectionPoolDataSource source,
            Configuration config) {
        this.source = source;
        this.state = config.getState();
        this.connectors = state.buildCasArray();
        this.allConnectors = new Connector[connectors.length()];
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return source.getLogWriter();
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return source.getLoginTimeout();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        source.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        source.setLoginTimeout(seconds);
    }
}
