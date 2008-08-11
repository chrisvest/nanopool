package net.nanopool;

import java.sql.SQLException;

import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;

final class ConnectionListener implements ConnectionEventListener {
    private final Connector connector;
    
    public ConnectionListener(Connector connector) {
        this.connector = connector;
    }
    
    public void connectionClosed(ConnectionEvent event) {
        try {
            connector.returnToPool();
        } catch (SQLException e) {
            throw new NanoPoolRuntimeException(
                    "Failed at returning the connection to the pool", e);
        }
    }
    
    public void connectionErrorOccurred(ConnectionEvent event) {
        try {
            connector.invalidate();
        } catch (SQLException e) {
            throw new NanoPoolRuntimeException(
                    "Failed at invalidating the connection", e);
        }
        try {
            connector.returnToPool();
        } catch (SQLException e) {
            throw new NanoPoolRuntimeException(
                    "Failed at returning the connection to the pool", e);
        }
    }
}
