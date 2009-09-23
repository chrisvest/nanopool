package net.nanopool;

import java.sql.Connection;
import java.sql.SQLException;

public class ThreadThat {
  public static Thread getAndCloseOneConnection(final NanoPoolDataSource pool) {
    Runnable runnable = new Runnable() {
      public void run() {
        Connection con = null;
        try {
          con = pool.getConnection();
        } catch (SQLException e) {
          e.printStackTrace();
        } finally {
          if (con != null) {
            try {
              con.close();
            } catch (SQLException e) {
              e.printStackTrace();
            }
          }
        }
      }
    };
    return new Thread(runnable);
  }
}
