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
package net.nanopool.hooks;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The JulLogHook uses a {@link Logger} to log every time it is invoked.
 * <p>
 * The log level can be modified at run-time but the Logger is final.
 * @author cvh
 */
public class JulLogHook implements Hook {
  private volatile Level logLevel;
  private final Logger logger;
  
  /**
   * Create a JulLogHook that uses the "net.nanopool" logger, and log
   * {@link Level#INFO}.
   */
  public JulLogHook() {
    this("net.nanopool");
  }
  
  /**
   * Create a JulLogHook that uses the logger with the specified name, and log
   * {@link Level#INFO}.
   * @param loggerName The name of the logger to use.
   */
  public JulLogHook(String loggerName) {
    this(loggerName, Level.INFO);
  }
  
  /**
   * Create a JulLogHook that uses the logger with the specified name and
   * log {@link Level}.
   * @param loggerName The name of the {@link Logger} to use.
   * @param level The {@link Level} to log with.
   */
  public JulLogHook(String loggerName, Level level) {
    this(Logger.getLogger(loggerName), level);
  }
  
  /**
   * Create a JulLogHook with the specified {@link Logger} and {@link Level}.
   * @param logger The {@link Logger} that this hook should log to.
   * @param level The {@link Level} the messages should be logged as.
   */
  public JulLogHook(Logger logger, Level level) {
    this.logger = logger;
    setLevel(level);
  }
  
  /**
   * Get the {@link Level} that is currently configured for this JulLogHook.
   * @return A {@link Level} object - never null.
   */
  public Level getLevel() {
    return logLevel;
  }
  
  /**
   * Set the {@link Level} that this JulLogHook should log messages at.
   * @param level A non-null {@link Level} object.
   */
  public void setLevel(Level level) {
    if (level == null) {
      throw new NullPointerException("Log level cannot be null.");
    }
    logLevel = level;
  }
  
  public void run(EventType type, Connection con, SQLException sqle) {
    if (!logger.isLoggable(logLevel)) {
      return;
    }
    String msg = type.toString() + ": " + con;
    logger.log(logLevel, msg, sqle);
  }
}
