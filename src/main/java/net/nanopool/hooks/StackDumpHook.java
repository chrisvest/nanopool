package net.nanopool.hooks;

import java.io.PrintStream;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.ConnectionPoolDataSource;

/**
 *
 * @author cvh
 */
public class StackDumpHook implements Hook {
    private final PrintStream out;
    
    public StackDumpHook() {
        this(System.err);
    }

    public StackDumpHook(PrintStream out) {
        this.out = out;
    }

    public void run(EventType type, ConnectionPoolDataSource source, Connection con, SQLException sqle) {
        new Throwable().printStackTrace(out);
    }
}
