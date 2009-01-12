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
package net.nanopool.samples;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import javax.sql.DataSource;

import net.nanopool.NanoPoolDataSource;

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
        
        // timeouts:
        source.setLoginTimeout(5 /*seconds*/);
        source.setConnectTimeout(5000 /*milliseconds*/);
        source.setSocketTimeout(5000 /*milliseconds*/);
        
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
        
        System.out.println("Shutting down pool");
        List<SQLException> exceptions = ((NanoPoolDataSource)pds).shutdown();
        
        if (!exceptions.isEmpty()) {
            System.out.println("Caught these SQLExceptions in shutdown:");
            for (SQLException ex : exceptions)
                ex.printStackTrace(System.out);
        }
        
        System.out.println("All done.");
    }
}
