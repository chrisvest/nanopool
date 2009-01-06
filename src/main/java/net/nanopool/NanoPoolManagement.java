package net.nanopool;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;

/**
 *
 * @author cvh
 */
public class NanoPoolManagement implements NanoPoolManagementMBean {
    private final NanoPoolDataSource np;

    public NanoPoolManagement(DataSource np) {
        if (np == null) throw new NullPointerException(
                "DataSource parameter must be non-null and of type " +
                "net.nanopool.NanoPoolDataSource.");
        this.np = (NanoPoolDataSource)np;
    }

    public int getCurrentOpenConnectionsCount() {
        return np.fsm.countOpenConnections(np.connectors);
    }

    public int getPoolSize() {
        return np.poolSize;
    }

    public long getConnectionTimeToLive() {
        return np.timeToLive;
    }

    public String getContentionHandlerClassName() {
        try {
            return np.contentionHandler.getClass().getName();
        } catch (NullPointerException npe) {
            return "null";
        }
    }

    public String getContentionHandler() {
        return String.valueOf(np.contentionHandler);
    }

    public boolean isShutDown() {
        for (Connector cn : np.allConnectors) {
            if (cn == FsmMixin.shutdownMarker) return true;
        }
        return false;
    }

    public String shutDown() {
        List<SQLException> faults = np.shutdown();
        if (faults.size() > 0) {
            StringWriter sos = new StringWriter();
            PrintWriter pout = new PrintWriter(sos);
            for (SQLException ex : faults) {
                ex.printStackTrace(pout);
            }
            pout.close();
            return sos.toString();
        }
        return "Shutdown completed successfully.";
    }

    public String getSourceConnectionClassName() {
        try {
            return np.source.getClass().getName();
        } catch (NullPointerException npe) {
            return "null";
        }
    }

    public String getSourceConnection() {
        return String.valueOf(np.source);
    }

    public int getConnectionsCreated() {
        int createdCount = 0;
        for (Connector cn : np.allConnectors) {
            if (cn != null) createdCount += cn.getRealConnectionsCreated();
        }
        return createdCount;
    }

    public int getConnectionsLeased() {
        int leasedCount = 0;
        for (Connector cn : np.allConnectors) {
            if (cn != null) leasedCount += cn.getConnectionsLeased();
        }
        return leasedCount;
    }

    public void resetCounters() {
        for (Connector cn : np.allConnectors) {
            if (cn != null) cn.resetCounters();
        }
    }

    public String listConnectionOwningThreadsStackTraces() {
        StringBuilder sb = new StringBuilder();
        for (Connector cn : np.allConnectors) {
            if (cn == null)
                continue;
            Thread owner = cn.getOwner();
            sb.append(cn);
            if (owner == null) {
                sb.append(" is not currently owned by anyone.\n");
            } else {
                sb.append(" owned by " + owner.getName() + ":\n");
                StackTraceElement[] trace = owner.getStackTrace();
                if (trace.length == 0)
                    sb.append("  No stack trace available.\n");
                for (StackTraceElement frame : trace) {
                    sb.append("  ").append(frame).append("\n");
                }
            }
        }
        return sb.toString();
    }

    public void dumpConnectionOwningThreadsStackTraces() {
        System.err.print(listConnectionOwningThreadsStackTraces());
    }
}
