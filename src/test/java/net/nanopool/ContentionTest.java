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
public class ContentionTest extends NanoPoolTestBase {
    @Override
    protected Settings buildSettings() {
        return super.buildSettings().setContentionHandler(
                new ThrowingContentionHandler());
    }

    @Test
    public void contentionHandlerMustRun() throws SQLException {
        pool = npds();
        Connection[] cons = new Connection[pool.state.connectors.length];
        for (int i = 0; i < cons.length; i++) {
            cons[i] = pool.getConnection();
        }

        // pool is now empty. Next getConnection must throw.

        final AtomicBoolean finishedMarker = new AtomicBoolean(false);
        killMeLaterCheck(finishedMarker, 10000,
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
}
