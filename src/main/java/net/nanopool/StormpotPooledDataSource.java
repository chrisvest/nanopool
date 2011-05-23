package net.nanopool;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.sql.ConnectionPoolDataSource;

import stormpot.Config;
import stormpot.LifecycledPool;
import stormpot.qpool.QueuePool;


public class StormpotPooledDataSource extends AbstractDataSource {
  private final LifecycledPool<StormpotConnection> pool;
  
  public StormpotPooledDataSource(
      ConnectionPoolDataSource ds, int size, long ttl) {
    ConnectionAllocator allocator = new ConnectionAllocator(ds);
    Config<StormpotConnection> config = new Config<StormpotConnection>();
    config.setSize(size);
    config.setTTL(ttl, TimeUnit.MILLISECONDS);
    config.setAllocator(allocator);
    pool = new QueuePool<StormpotConnection>(config);
  }

  public List<SQLException> close() {
    
    return new ArrayList();
  }

  public Connection getConnection() throws SQLException {
    try {
      return pool.claim();
    } catch (Exception e) {
      throw new SQLException(e);
    }
  }
}
