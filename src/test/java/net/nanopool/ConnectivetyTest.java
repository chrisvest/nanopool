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
    @Test
    public void mustNormallyConnect() throws SQLException {
        pool = npds();
        Connection con = pool.getConnection();
        try {
            Statement stmt = con.createStatement();
            try {
                ResultSet rs = stmt.executeQuery("select 1");
                assertTrue(rs.next());
                assertEquals(1, rs.getInt(1));
            } finally {
                stmt.close();
            }
        } finally {
            con.close();
        }
    }
}
