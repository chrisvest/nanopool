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
 * A ManagedNanoPool is something that can produce an object that implements
 * the {@link NanoPoolManagementMBean} interface.
 * <p>
 * The primary reason for defining this ability in its own interface, is
 * testability.
 * @author cvh
 */
public interface ManagedNanoPool {
  /**
   * Produce an object that implements the {@link NanoPoolManagementMBean}
   * interface. There is no guarantee that this method will always return the
   * same exact instance. It is perfectly within specification for any
   * implementor to return a new instance on every call. However, implementors
   * must guarantee the thread-safety of this method.
   * @return An object that implements {@link NanoPoolManagementMBean}.
   */
  NanoPoolManagementMBean getMXBean();
}
