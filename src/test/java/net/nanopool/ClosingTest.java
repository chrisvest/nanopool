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
public class ClosingTest extends NanoPoolTestBase {
  @Test
  public void doubleClosingConnectionsMustBeNoOp() throws SQLException {
    pool = npds();
    Connection con1 = pool.getConnection();
    con1.close();
    Connection con2 = pool.getConnection();
    // this must not throw
    con1.close();
    // this mustn't throw either
    con2.close();
    con2.close();
  }
  
  @Test
  public void doubleClosingConnectionsMustNotSideEffectReusedConnectors()
      throws SQLException {
    pool = npds();
    Connection con1 = pool.getConnection();
    con1.close();
    Connection con2 = pool.getConnection();
    assertWorking(con2);
    con1.close();
    assertWorking(con2);
    con2.close();
  }
  
  @Override
  protected Settings buildSettings() {
    return super.buildSettings().setPoolSize(1);
  }
  
  private void assertWorking(Connection con) throws SQLException {
    Statement stmt = con.createStatement();
    try {
      ResultSet rs = stmt.executeQuery("select now()");
      assertTrue(rs.next());
    } finally {
      stmt.close();
    }
  }
}
