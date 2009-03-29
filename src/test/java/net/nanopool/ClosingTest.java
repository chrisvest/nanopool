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
    public void doubleClosingConnectionsMustNotSideEffectReusedConnectors() throws SQLException {
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
