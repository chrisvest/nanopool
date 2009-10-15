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
  
  public void run(EventType type, Connection con, SQLException sqle) {
    if (!logger.isLoggable(logLevel))
      return;
    String msg = type.toString() + ": " + con;
    logger.log(logLevel, msg, sqle);
  }
}
