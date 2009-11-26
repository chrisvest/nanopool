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
package net.nanopool;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.junit.Test;

/**
 * 
 * @author cvh
 */
public class ConnectivetyTest extends NanoPoolTestBase {
  @Test public void
  mustNormallyConnect() throws SQLException {
    pool = npds();
    Connection con = pool.getConnection();
    try {
      Statement stmt = con.createStatement();
      try {
        ResultSet rs = stmt.executeQuery(
            "select CURRENT_TIMESTAMP from sysibm.sysdummy1");
        assertTrue(rs.next());
        assertNotNull(rs.getTimestamp(1));
      } finally {
        stmt.close();
      }
    } finally {
      con.close();
    }
  }
}
