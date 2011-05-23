/*
   Copyright 2008-2009 Christian Vest Hansen

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package net.nanopool.benchmark;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;

import net.nanopool.AbstractDataSource;
import net.nanopool.NanoPoolDataSource;
import net.nanopool.Settings;
import net.nanopool.StormpotPooledDataSource;
import net.nanopool.contention.DefaultContentionHandler;

import org.apache.commons.dbcp.datasources.SharedPoolDataSource;

import biz.source_code.miniConnectionPoolManager.MiniConnectionPoolManager;

import com.jolbox.bonecp.BoneCPConfig;
import com.jolbox.bonecp.BoneCPDataSource;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.c3p0.DataSources;

public class PoolFactories {

  static final class NanoPoolFactory implements PoolFactory {
    public DataSource buildPool(ConnectionConfiguration config, int size,
        long ttl) {
      ConnectionPoolDataSource cpds = config.getCpds();
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
  
  static final class DbcpPoolFactory implements PoolFactory {
    public DataSource buildPool(ConnectionConfiguration config, int size,
        long ttl) {
      ConnectionPoolDataSource cpds = config.getCpds();
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
    public DataSource buildPool(ConnectionConfiguration config, int size,
        long ttl) {
      ConnectionPoolDataSource cpds = config.getCpds();
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
  
  static final class BoneCpPoolFactory implements PoolFactory {
    public DataSource buildPool(ConnectionConfiguration config, int size,
        long ttl) {
      BoneCPConfig boneConf = new BoneCPConfig();
      boneConf.setJdbcUrl(config.getUrl());
      boneConf.setUsername(config.getUsername());
      boneConf.setPassword(config.getPassword());
      boneConf.setIdleMaxAge(ttl);
      BoneCPDataSource ds = new BoneCPDataSource(boneConf);
      ds.setDriverClass(config.getDriverClass());
      return ds;
    }

    public void closePool(DataSource pool) {
      BoneCPDataSource ds = (BoneCPDataSource) pool;
      ds.close();
    }
  }
  
  static final class C3p0PoolFactory implements PoolFactory {
    public DataSource buildPool(ConnectionConfiguration config, int size,
        long ttl) {
      ComboPooledDataSource cpds = new ComboPooledDataSource();
      try {
        cpds.setDriverClass(config.getDriverClass());
      } catch (PropertyVetoException e) {
        throw new IllegalStateException("Could not load driver", e);
      }            
      cpds.setJdbcUrl(config.getUrl());
      cpds.setUser(config.getUsername());
      cpds.setPassword(config.getPassword());
      cpds.setMaxPoolSize(size);
      cpds.setMaxIdleTime((int) ttl);
      return cpds;
    }

    public void closePool(DataSource pool) {
      try {
        DataSources.destroy(pool);
      } catch (SQLException e) {
        throw new IllegalStateException("Can not close this pool", e);
      }
    }
  }
  
  static final class StormpotPoolFactory implements PoolFactory {
    public DataSource buildPool(ConnectionConfiguration config, int size,
        long ttl) {
      return new StormpotPooledDataSource(config.getCpds(), size, ttl);
    }

    public void closePool(DataSource pool) {
      ((StormpotPooledDataSource) pool).close();
    }
  }
}
