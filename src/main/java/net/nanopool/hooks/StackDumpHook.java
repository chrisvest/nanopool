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

import java.io.PrintStream;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * The StackDumpHook prints a complete stack trace to a designated
 * {@link PrintStream} every time it run. The stack traces are printed to
 * {@link System#out} by default.
 * <p>
 * This hook is primarily for debugging purpose and you wouldn't want to use
 * it in a production setting.
 * @author cvh
 */
public class StackDumpHook implements Hook {
  private final PrintStream out;
  
  /**
   * Create a StackDumpHook that prints a complete stack trace to System.out
   * when it runs.
   */
  public StackDumpHook() {
    this(System.err);
  }
  
  /**
   * Create a StackDumpHook that prints a complete stack trace to the
   * {@link PrintStream} specified as parameter.
   * @param out The {@link PrintStream} to which the stack traces will be
   * printed.
   */
  public StackDumpHook(PrintStream out) {
    this.out = out;
  }
  
  public void run(EventType type, Connection con, SQLException sqle) {
    new Throwable().printStackTrace(out);
  }
}
