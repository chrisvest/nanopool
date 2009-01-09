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
