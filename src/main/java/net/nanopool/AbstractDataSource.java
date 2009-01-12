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
import javax.sql.DataSource;

/**
 * This is a very simple base class for {@link DataSource} that throws
 * {@link UnsupportedOperationException} on all method calls.
 * @author cvh
 */
public abstract class AbstractDataSource implements DataSource {
    /**
     * Always throws UnsupportedOperationException.
     * @return
     * @throws java.sql.SQLException
     */
    public PrintWriter getLogWriter() throws SQLException {
        throw new UnsupportedOperationException("Not supported.");
    }

    /**
     * Always throws UnsupportedOperationException.
     * @return
     * @throws java.sql.SQLException
     */
    public int getLoginTimeout() throws SQLException {
        throw new UnsupportedOperationException("Not supported.");
    }

    /**
     * Always throws UnsupportedOperationException.
     * @param out
     * @throws java.sql.SQLException
     */
    public void setLogWriter(PrintWriter out) throws SQLException {
        throw new UnsupportedOperationException("Not supported.");
    }

    /**
     * Always throws UnsupportedOperationException.
     * @param seconds
     * @throws java.sql.SQLException
     */
    public void setLoginTimeout(int seconds) throws SQLException {
        throw new UnsupportedOperationException("Not supported.");
    }

    /**
     * Always throws UnsupportedOperationException.
     * @param <T>
     * @param iface
     * @return
     * @throws java.sql.SQLException
     */
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new UnsupportedOperationException("Not supported.");
    }

    /**
     * Always throws UnsupportedOperationException.
     * @param iface
     * @return
     * @throws java.sql.SQLException
     */
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        throw new UnsupportedOperationException("Not supported.");
    }
}
