package net.nanopool.samples;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import net.nanopool2.NanoPoolDataSource;

import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;

public class Simple {
    public static void main(String[] args) throws SQLException {
        MysqlConnectionPoolDataSource source =
            new MysqlConnectionPoolDataSource();
        source.setServerName("localhost");
        source.setPort(3306);
        source.setDatabaseName("test");
        source.setUser("root");
        source.setPassword("");
        
        System.out.println("Creating connection pool");
        DataSource pds = new NanoPoolDataSource(source, 10, 300000);
        
        System.out.println("Getting new connection");
        Connection con = pds.getConnection();
        
        try {
            System.out.println("Creating statement");
            Statement st = con.createStatement();
            
            System.out.println("Executing query");
            ResultSet rs = st.executeQuery("select now()");
            
            if (rs.next()) {
                System.out.println(rs.getString(1));
            }
        } finally {
            System.out.println("Closing connection");
            con.close();
        }
    }
}
