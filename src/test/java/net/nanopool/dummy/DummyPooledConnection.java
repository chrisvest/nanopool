package net.nanopool.dummy;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.ConnectionEventListener;
import javax.sql.PooledConnection;
import javax.sql.StatementEventListener;

/**
 * A dummy that throws UnsupportedOperationException on every method call.
 * @author cvh
 */
public class DummyPooledConnection implements PooledConnection {

    public Connection getConnection() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void close() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void addConnectionEventListener(ConnectionEventListener arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeConnectionEventListener(ConnectionEventListener arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void addStatementEventListener(StatementEventListener listener) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeStatementEventListener(StatementEventListener listener) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
