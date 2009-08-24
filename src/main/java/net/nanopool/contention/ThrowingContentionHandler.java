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
package net.nanopool.contention;

import net.nanopool.ManagedNanoPool;
import net.nanopool.NanoPoolRuntimeException;

/**
 * An implementation of {@link ContentionHandler} that throws a
 * {@link NanoPoolRuntimeException} when there's too much contention on the
 * pool.
 * 
 * @author cvh
 * @since 1.0
 */
public class ThrowingContentionHandler implements ContentionHandler {
  /**
   * The {@link NanoPoolRuntimeException} thrown by this ContentionHandler
   * will have this message.
   */
  public static final String MESSAGE = "Connection pool contention too high.";
  
  public void handleContention(int count, ManagedNanoPool mnp) {
    throw new NanoPoolRuntimeException(MESSAGE);
  }
}
