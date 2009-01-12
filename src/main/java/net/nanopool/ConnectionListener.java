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

import java.sql.SQLException;

import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;

final class ConnectionListener implements ConnectionEventListener {
    private final Connector connector;
    
    public ConnectionListener(Connector connector) {
        this.connector = connector;
    }
    
    public void connectionClosed(ConnectionEvent event) {
        try {
            connector.returnToPool();
        } catch (SQLException e) {
            throw new NanoPoolRuntimeException(
                    "Failed at returning the connection to the pool", e);
        }
    }
    
    public void connectionErrorOccurred(ConnectionEvent event) {
        try {
            connector.invalidate();
        } catch (SQLException e) {
            throw new NanoPoolRuntimeException(
                    "Failed at invalidating the connection", e);
        }
        try {
            connector.returnToPool();
        } catch (SQLException e) {
            throw new NanoPoolRuntimeException(
                    "Failed at returning the connection to the pool", e);
        }
    }
}
