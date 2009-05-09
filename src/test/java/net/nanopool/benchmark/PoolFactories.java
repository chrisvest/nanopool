package net.nanopool.benchmark;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;

import org.apache.commons.dbcp.datasources.SharedPoolDataSource;

import biz.source_code.miniConnectionPoolManager.MiniConnectionPoolManager;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.c3p0.DataSources;

import net.nanopool.AbstractDataSource;
import net.nanopool.NanoPoolDataSource;
import net.nanopool.Settings;
import net.nanopool.contention.DefaultContentionHandler;

public class PoolFactories {
  
  static final class NanoPoolFactory implements PoolFactory {
    public DataSource buildPool(ConnectionPoolDataSource cpds, int size,
        long ttl) {
      return new NanoPoolDataSource(cpds, buildSettings(size, ttl));
    }
    
    public void closePool(DataSource pool) {
      if (pool instanceof NanoPoolDataSource) {
        List<SQLException> sqles = ((NanoPoolDataSource) pool).close();
        for (SQLException sqle : sqles) {
          sqle.printStackTrace();
        }
      }
    }
    
    private static Settings buildSettings(int poolSize, long ttl) {
      Settings settings = new Settings();
      settings.setPoolSize(poolSize).setTimeToLive(ttl).setContentionHandler(
          new DefaultContentionHandler(false, 0));
      return settings;
    }
  }
  
  static final class C3P0PoolFactory implements PoolFactory {
    // TODO I think this one's broken...
    public DataSource buildPool(
        ConnectionPoolDataSource cpds, int size, long ttl) {
      ComboPooledDataSource cmds = new ComboPooledDataSource();
      try {
        //WrapperConnectionPoolDataSource wcpds = new WrapperConnectionPoolDataSource();
        cmds.setConnectionPoolDataSource(cpds);
        cmds.setMaxPoolSize(size);
        return cmds;
      } catch (PropertyVetoException ex) {
        throw new RuntimeException(ex);
      }
    }
    
    public void closePool(DataSource pool) {
      try {
        DataSources.destroy(pool);
      } catch (SQLException ex) {
        ex.printStackTrace();
      }
    }
  }
  
  static final class DbcpPoolFactory implements PoolFactory {
    public DataSource buildPool(ConnectionPoolDataSource cpds, int size,
        long ttl) {
      SharedPoolDataSource spds = new SharedPoolDataSource();
      spds.setConnectionPoolDataSource(cpds);
      spds.setMaxActive(size);
      return spds;
    }
    
    public void closePool(DataSource pool) {
      SharedPoolDataSource spds = (SharedPoolDataSource) pool;
      try {
        spds.close();
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  }
  
  static final class McpmPoolFactory implements PoolFactory {
    public DataSource buildPool(ConnectionPoolDataSource cpds, int size,
        long ttl) {
      final MiniConnectionPoolManager mcpm = new MiniConnectionPoolManager(
          cpds, size);
      return new AbstractDataSource() {
        public Connection getConnection() throws SQLException {
          return mcpm.getConnection();
        }
        
        public List<SQLException> close() {
          List<SQLException> sqles = new ArrayList<SQLException>();
          try {
            mcpm.dispose();
          } catch (SQLException ex) {
            sqles.add(ex);
          }
          return sqles;
        }
      };
    }
    
    public void closePool(DataSource pool) {
      AbstractDataSource ads = (AbstractDataSource) pool;
      List<SQLException> sqles = ads.close();
      for (SQLException sqle : sqles) {
        sqle.printStackTrace();
      }
    }
  }
  
}
