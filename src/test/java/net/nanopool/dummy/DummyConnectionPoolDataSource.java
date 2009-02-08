package net.nanopool.dummy;

import java.io.PrintWriter;
import java.sql.SQLException;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;

/**
 * A dummy that throws UnsupportedOperationException on every method call.
 * @author cvh
 */
public class DummyConnectionPoolDataSource implements ConnectionPoolDataSource {

    public PooledConnection getPooledConnection() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public PooledConnection getPooledConnection(String arg0, String arg1) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public PrintWriter getLogWriter() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setLogWriter(PrintWriter arg0) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setLoginTimeout(int arg0) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getLoginTimeout() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
