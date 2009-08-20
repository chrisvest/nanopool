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
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;
import net.nanopool.contention.ThrowingContentionHandler;
import org.junit.Test;

/**
 * 
 * @author cvh
 */
public class ResizingTest extends NanoPoolTestBase {
  @Test
  public void poolMustWorkAfterGrowing() throws SQLException {
    pool = npds();
    assertWorking(pool);
    pool.resizePool(15);
    assertWorking(pool);
    pool.close();
  }
  
  @Test
  public void poolMustWorkAfterShrinking() throws SQLException {
    pool = npds();
    assertWorking(pool);
    pool.resizePool(5);
    assertWorking(pool);
    pool.close();
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void cannotResizeToLessThanOne() throws SQLException {
    pool = npds();
    pool.resizePool(0);
  }
  
  private void assertWorking(NanoPoolDataSource pool) throws SQLException {
    int len = pool.state.connectors.length;
    Connection[] cons = new Connection[len];
    
    for (int i = 0; i < len; i++) {
      cons[i] = pool.getConnection();
    }
    
    final AtomicBoolean finishedMarker = new AtomicBoolean(false);
    killMeLaterCheck(finishedMarker, 1000,
        new SQLException("Die you gravy sucking pig-dog."));
    
    try {
      pool.getConnection();
      fail("Expected getConnection to throw.");
    } catch (NanoPoolRuntimeException npre) {
      assertEquals(ThrowingContentionHandler.MESSAGE, npre.getMessage());
    }
    finishedMarker.set(true);
    
    for (int i = 0; i < cons.length; i++) {
      cons[i].close();
    }
  }
  
  @Override
  protected Settings buildSettings() {
    return super.buildSettings().setContentionHandler(
        new ThrowingContentionHandler());
  }
}
