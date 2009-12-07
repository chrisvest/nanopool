package net.nanopool.benchmark;

import javax.sql.ConnectionPoolDataSource;

public class ConnectionConfiguration {
  private ConnectionPoolDataSource cpds;
  private String username;
  private String password;
  private String url;
  private String driverClass;

  public String getDriverClass() {
    return driverClass;
  }

  public void setDriverClass(String driverClass) {
    this.driverClass = driverClass;
  }

  public ConnectionPoolDataSource getCpds() {
    return cpds;
  }

  public void setCpds(ConnectionPoolDataSource cpds) {
    this.cpds = cpds;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }
}
