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

import net.nanopool.contention.ContentionHandler;
import net.nanopool.hooks.Hook;

final class Config {
    final int poolSize;
    final long ttl;
    final ContentionHandler contentionHandler;
    final Cons<Hook> preConnectHooks;
    final Cons<Hook> postConnectHooks;
    final Cons<Hook> preReleaseHooks;
    final Cons<Hook> postReleaseHooks;
    final Cons<Hook> connectionInvalidationHooks;
    final TimeSource time;

    public Config(int poolSize, long ttl,
            ContentionHandler contentionHandler, Cons<Hook> preConnectHooks,
            Cons<Hook> postConnectHooks, Cons<Hook> preReleaseHook,
            Cons<Hook> postReleaseHook, Cons<Hook> connectionInvalidationHook,
            TimeSource time) {
        super();
        this.poolSize = poolSize;
        this.ttl = ttl;
        this.contentionHandler = contentionHandler;
        this.preConnectHooks = preConnectHooks;
        this.postConnectHooks = postConnectHooks;
        this.preReleaseHooks = preReleaseHook;
        this.postReleaseHooks = postReleaseHook;
        this.connectionInvalidationHooks = connectionInvalidationHook;
        this.time = time;
    }
}
