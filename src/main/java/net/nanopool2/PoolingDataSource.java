package net.nanopool2;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.ConnectionPoolDataSource;

import net.nanopool.Log;

public class PoolingDataSource extends PoolingDataSourceSupport {
    private final Connector tombstone;
    private final Connector[] connectors;
    
    public PoolingDataSource(ConnectionPoolDataSource source, int poolSize,
            long timeToLive, Log log) {
        super(source, poolSize, timeToLive, log);
        tombstone = new Connector();
        connectors = new Connector[poolSize];
    }

    public Connection getConnection() throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    public Connection getConnection(String username, String password)
            throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

}
