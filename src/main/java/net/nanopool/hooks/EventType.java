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

/**
 * An enum for the different types of events that can cause a hook to run.
 * @author cvh
 */
public enum EventType {
  /**
   * The pre-connect event happens before a connection is leased.
   * @see net.nanopool.Settings#addPreConnectHook(Hook)
   */
  preConnect,
  /**
   * The post-connect event happens after a connection has been leased or the
   * lease attempt has decidedly failed with an SQLException, but before that
   * connection is returned, or the exception thrown, to the caller of
   * {@link net.nanopool.NanoPoolDataSource#getConnection()}.
   * @see net.nanopool.Settings#addPostConnectHook(Hook)
   */
  postConnect,
  /**
   * The pre-release event happens before a leased connection returns to the
   * pool. Note that throwing from a pre-release hook does not abrupt or abort
   * any flow inside of NanoPool - it is a safe thing to do from the
   * perspective of NanoPool itself.
   * @see net.nanopool.Settings#addPreReleaseHook(Hook)
   */
  preRelease,
  /**
   * The post-release event happens after a connection lease has returned to
   * the pool. Be mindful of race conditions here, because the connection might
   * have been re-leased at this point, or the physical connection might have
   * been closed.
   * @see net.nanopool.Settings#addPostReleaseHook(Hook)
   */
  postRelease,
  /**
   * The invalidation event happens after a physical connection has been closed
   * but before any post-release event. If an SQLException is thrown by the
   * JDBC driver while closing the physical connection, then that exception
   * will be passed to the hooks as a parameter.
   * @see net.nanopool.Settings#addConnectionInvalidationHook(Hook)
   */
  invalidation;
  
  @Override
  public String toString() {
    switch (this) {
    case preConnect: return "Pre-connect";
    case postConnect: return "Post-connect";
    case preRelease: return "Pre-release";
    case postRelease: return "Post-release";
    case invalidation: return "Invalidation";
    }
    return super.toString();
  }
}
