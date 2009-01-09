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
    public PrintWriter getLogWriter() throws SQLException {
        throw new UnsupportedOperationException("Not supported.");
    }

    public int getLoginTimeout() throws SQLException {
        throw new UnsupportedOperationException("Not supported.");
    }

    public void setLogWriter(PrintWriter out) throws SQLException {
        throw new UnsupportedOperationException("Not supported.");
    }

    public void setLoginTimeout(int seconds) throws SQLException {
        throw new UnsupportedOperationException("Not supported.");
    }

    /**
     * This method will always throw UnsupportedOperationException.
     * @param <T>
     * @param iface
     * @return
     * @throws java.sql.SQLException
     */
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new UnsupportedOperationException("Not supported.");
    }

    /**
     * This method will always throw UnsupportedOperationException.
     * @param iface
     * @return
     * @throws java.sql.SQLException
     */
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        throw new UnsupportedOperationException("Not supported.");
    }
}
