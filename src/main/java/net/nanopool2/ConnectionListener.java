package net.nanopool2;

import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;

public class ConnectionListener implements ConnectionEventListener {
    private final Connector connector;
    
    public ConnectionListener(Connector connector) {
        this.connector = connector;
    }
    
    public void connectionClosed(ConnectionEvent event) {
        connector.returnToPool();
    }
    
    public void connectionErrorOccurred(ConnectionEvent event) {
        connector.invalidate();
        connector.returnToPool();
    }
    
}
