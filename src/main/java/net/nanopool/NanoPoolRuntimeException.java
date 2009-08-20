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
package net.nanopool;

/**
 * Thrown under the same premises as any other {@link RuntimeException}. That
 * is, whenever something unanticipated went wrong.
 * 
 * @author vest
 * @since 1.0
 */
public class NanoPoolRuntimeException extends RuntimeException {
  private static final long serialVersionUID = -2359233167832636507L;
  
  /**
   * @see RuntimeException#RuntimeException()
   * @since 1.0
   */
  public NanoPoolRuntimeException() {
    super();
  }
  
  /**
   * @see RuntimeException#RuntimeException(String, Throwable)
   * @param msg
   * @param th
   * @since 1.0
   */
  public NanoPoolRuntimeException(String msg, Throwable th) {
    super(msg, th);
  }
  
  /**
   * @see RuntimeException#RuntimeException(String)
   * @param msg
   * @since 1.0
   */
  public NanoPoolRuntimeException(String msg) {
    super(msg);
  }
  
  /**
   * @see RuntimeException#RuntimeException(Throwable)
   * @param th
   * @since 1.0
   */
  public NanoPoolRuntimeException(Throwable th) {
    super(th);
  }
}
