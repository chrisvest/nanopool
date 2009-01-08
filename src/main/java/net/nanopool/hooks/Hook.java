/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.nanopool.hooks;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.ConnectionPoolDataSource;

/**
 *
 * @author cvh
 */
public interface Hook {
    void run(EventType type, ConnectionPoolDataSource source,
            Connection con, SQLException sqle);
}
