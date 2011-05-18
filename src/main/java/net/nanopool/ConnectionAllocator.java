package net.nanopool;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;

import stormpot.Allocator;
import stormpot.Slot;

public class ConnectionAllocator implements Allocator<StormpotConnection> {
  private final ConnectionPoolDataSource ds;

  public ConnectionAllocator(ConnectionPoolDataSource ds) {
    this.ds = ds;
  }

  public StormpotConnection allocate(Slot slot) throws Exception {
    PooledConnection connection = ds.getPooledConnection();
    return new StormpotConnection(slot, connection);
  }

  public void deallocate(StormpotConnection poolable) throws Exception {
    poolable.closePhysical();
  }
}
