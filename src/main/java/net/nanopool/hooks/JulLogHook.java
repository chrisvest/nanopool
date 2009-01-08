package net.nanopool.hooks;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.ConnectionPoolDataSource;

/**
 *
 * @author cvh
 */
public class JulLogHook implements Hook {
    private volatile Level logLevel;
    private final Logger logger;

    public JulLogHook(String loggerName) {
        this(loggerName, Level.INFO);
    }

    public JulLogHook(String loggerName, Level level) {
        this(Logger.getLogger(loggerName), level);
    }

    public JulLogHook(Logger logger, Level level) {
        this.logger = logger;
        setLevel(level);
    }

    public Level getLevel() {
        return logLevel;
    }

    public void setLevel(Level level) {
        if (level == null)
            throw new NullPointerException("Log level cannot be null.");
        logLevel = level;
    }

    public void run(EventType type, ConnectionPoolDataSource source,
            Connection con, SQLException sqle) {
        if (!logger.isLoggable(logLevel)) return;
        String msg = type.toString() +
                ": " + source + "/" + con;
        if (sqle != null) {
            logger.log(logLevel, msg, sqle);
        } else {
            logger.log(logLevel, msg, sqle);
        }
    }
}
