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

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import net.nanopool.cas.CasArray;
import net.nanopool.cas.StrongResizableAtomicCasArray;
import net.nanopool.contention.ContentionHandler;
import net.nanopool.contention.DefaultContentionHandler;
import net.nanopool.hooks.Hook;
import net.nanopool.loadbalancing.RandomStrategy;
import net.nanopool.loadbalancing.Strategy;

/**
 * Configuration is an atomically mutable state-container that describes to a
 * {@link NanoPoolDataSource} how it should operate. The Configuration instance
 * can be safely mutated after a NanoPoolDataSource has been created using it.
 * @author cvh
 * @since 1.0
 */
public class Configuration {
    private final AtomicReference<State> state = new AtomicReference<State>();

    Configuration(State st) {
        state.set(st);
    }

    public Configuration() {
        this(new State(
                10, 300000, StrongResizableAtomicCasArray.class,
                new DefaultContentionHandler(), null, null, null, null, null,
                RandomStrategy.INSTANCE
        ));
    }

    State getState() {
        return state.get();
    }

    /*
     * Simpler properties.
     */

    public int getPoolSize() {
        return state.get().poolSize;
    }

    public Configuration setPoolSize(int poolSize) {
        if (poolSize < 1) {
            throw new IllegalArgumentException("Pool size must be at least " +
                    "one. " + poolSize + " is too low.");
        }
        State s, n;
        do {
            s = state.get();
            n = new State(poolSize, s.ttl, s.casArrayType, s.contentionHandler,
                    s.preConnectHooks, s.postConnectHooks, s.preReleaseHooks,
                    s.postReleaseHooks, s.connectionInvalidationHooks,
                    s.loadBalancingStrategy);
        } while (state.compareAndSet(s, n));
        return this;
    }

    public long getTimeToLive() {
        return state.get().ttl;
    }

    public Configuration setTimeToLive(long ttl) {
        if (ttl < 0) {
            throw new IllegalArgumentException("Time to live must be at " +
                    "least zero. " + ttl +" is too low.");
        }
        State s, n;
        do {
            s = state.get();
            n = new State(s.poolSize, ttl, s.casArrayType, s.contentionHandler,
                    s.preConnectHooks, s.postConnectHooks, s.preReleaseHooks,
                    s.postReleaseHooks, s.connectionInvalidationHooks,
                    s.loadBalancingStrategy);
        } while (state.compareAndSet(s, n));
        return this;
    }

    public Class<? extends CasArray> getCasArrayType() {
        return state.get().casArrayType;
    }

    public Configuration setCasArrayType(Class<? extends CasArray> type) {
        if (!CasArray.class.isAssignableFrom(type)) {
            throw new IllegalArgumentException(type.getCanonicalName() +
                    " is not a kind of " + CasArray.class.getCanonicalName());
        }
        try {
            type.getConstructor(new Class[]{Integer.TYPE});
        } catch (Exception e) {
            throw new IllegalArgumentException("No public public constructor " +
                    "taking one int as an argument was found for this " +
                    "CasArray implementation: " + type.getCanonicalName(), e);
        }
        State s, n;
        do {
            s = state.get();
            n = new State(s.poolSize, s.ttl, type, s.contentionHandler,
                    s.preConnectHooks, s.postConnectHooks, s.preReleaseHooks,
                    s.postReleaseHooks, s.connectionInvalidationHooks,
                    s.loadBalancingStrategy);
        } while (state.compareAndSet(s, n));
        return this;
    }

    public ContentionHandler getContentionHandler() {
        return state.get().contentionHandler;
    }

    public Configuration setContentionHandler(ContentionHandler contentionHandler) {
        if (contentionHandler == null) {
            throw new IllegalArgumentException(
                    "Contention handler cannot be null.");
        }
        State s, n;
        do {
            s = state.get();
            n = new State(s.poolSize, s.ttl, s.casArrayType, contentionHandler,
                    s.preConnectHooks, s.postConnectHooks, s.preReleaseHooks,
                    s.postReleaseHooks, s.connectionInvalidationHooks,
                    s.loadBalancingStrategy);
        } while (state.compareAndSet(s, n));
        return this;
    }

    public Strategy getLoadBalancingStrategy() {
        return state.get().loadBalancingStrategy;
    }

    public Configuration setLoadBalancingStrategy(Strategy strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException(
                    "Load balancing strategy should not be null.");
        }
        State s, n;
        do {
            s = state.get();
            n = new State(s.poolSize, s.ttl, s.casArrayType, s.contentionHandler,
                    s.preConnectHooks, s.postConnectHooks, s.preReleaseHooks,
                    s.postReleaseHooks, s.connectionInvalidationHooks,
                    strategy);
        } while (state.compareAndSet(s, n));
        return this;
    }

    /*
     * Hooks.
     */

    private <T> Cons<T> recurRemove(T obj, Cons<T> from) {
        if (from == null) return null;
        if (from.first.equals(obj)) return from.rest;
        return new Cons(from.first, recurRemove(obj, from.rest));
    }

    private <T> Cons<T> remove(T obj, Cons<T> from) {
        if (from == null) return null;
        if (!from.contains(obj)) return from;
        return recurRemove(obj, from);
    }

    public List<Hook> getPreConnectHooks() {
        return state.get().preConnectHooks.toList();
    }

    public Configuration addPreConnectHook(Hook hook) {
        State s, n;
        do {
            s = state.get();
            n = new State(s.poolSize, s.ttl, s.casArrayType, s.contentionHandler,
                    new Cons(hook, s.preConnectHooks), s.postConnectHooks,
                    s.preReleaseHooks, s.postReleaseHooks,
                    s.connectionInvalidationHooks, s.loadBalancingStrategy);
        } while (state.compareAndSet(s, n));
        return this;
    }

    public Configuration removePreConnectHook(Hook hook) {
        State s, n;
        do {
            s = state.get();
            n = new State(s.poolSize, s.ttl, s.casArrayType, s.contentionHandler,
                    remove(hook, s.preConnectHooks), s.postConnectHooks,
                    s.preReleaseHooks, s.postReleaseHooks,
                    s.connectionInvalidationHooks, s.loadBalancingStrategy);
        } while (state.compareAndSet(s, n));
        return this;
    }

    public List<Hook> getPostConnectHooks() {
        return state.get().postConnectHooks.toList();
    }

    public Configuration addPostConnectHook(Hook hook) {
        State s, n;
        do {
            s = state.get();
            n = new State(s.poolSize, s.ttl, s.casArrayType, s.contentionHandler,
                    s.preConnectHooks, new Cons(hook, s.postConnectHooks),
                    s.preReleaseHooks, s.postReleaseHooks,
                    s.connectionInvalidationHooks, s.loadBalancingStrategy);
        } while (state.compareAndSet(s, n));
        return this;
    }

    public Configuration removePostConnectHook(Hook hook) {
        State s, n;
        do {
            s = state.get();
            n = new State(s.poolSize, s.ttl, s.casArrayType, s.contentionHandler,
                    s.preConnectHooks, remove(hook, s.postConnectHooks),
                    s.preReleaseHooks, s.postReleaseHooks,
                    s.connectionInvalidationHooks, s.loadBalancingStrategy);
        } while (state.compareAndSet(s, n));
        return this;
    }

    public List<Hook> getPreReleaseHooks() {
        return state.get().preReleaseHooks.toList();
    }

    public Configuration addPreReleaseHook(Hook hook) {
        State s, n;
        do {
            s = state.get();
            n = new State(s.poolSize, s.ttl, s.casArrayType, s.contentionHandler,
                    s.preConnectHooks, s.postConnectHooks,
                    new Cons(hook, s.preReleaseHooks), s.postReleaseHooks,
                    s.connectionInvalidationHooks, s.loadBalancingStrategy);
        } while (state.compareAndSet(s, n));
        return this;
    }

    public Configuration removePreReleaseHook(Hook hook) {
        State s, n;
        do {
            s = state.get();
            n = new State(s.poolSize, s.ttl, s.casArrayType, s.contentionHandler,
                    s.preConnectHooks, s.postConnectHooks,
                    remove(hook, s.preReleaseHooks), s.postReleaseHooks,
                    s.connectionInvalidationHooks, s.loadBalancingStrategy);
        } while (state.compareAndSet(s, n));
        return this;
    }

    public List<Hook> getPostReleaseHooks() {
        return state.get().postReleaseHooks.toList();
    }

    public Configuration addPostReleaseHook(Hook hook) {
        State s, n;
        do {
            s = state.get();
            n = new State(s.poolSize, s.ttl, s.casArrayType, s.contentionHandler,
                    s.preConnectHooks, s.postConnectHooks,
                    s.preReleaseHooks, new Cons(hook, s.postReleaseHooks),
                    s.connectionInvalidationHooks, s.loadBalancingStrategy);
        } while (state.compareAndSet(s, n));
        return this;
    }

    public Configuration removePostReleaseHook(Hook hook) {
        State s, n;
        do {
            s = state.get();
            n = new State(s.poolSize, s.ttl, s.casArrayType, s.contentionHandler,
                    s.preConnectHooks, s.postConnectHooks,
                    s.preReleaseHooks, remove(hook, s.postReleaseHooks),
                    s.connectionInvalidationHooks, s.loadBalancingStrategy);
        } while (state.compareAndSet(s, n));
        return this;
    }

    public List<Hook> getConnectionInvalidationHooks() {
        return state.get().connectionInvalidationHooks.toList();
    }

    public Configuration addConnectionInvalidationHook(Hook hook) {
        State s, n;
        do {
            s = state.get();
            n = new State(s.poolSize, s.ttl, s.casArrayType, s.contentionHandler,
                    s.preConnectHooks, s.postConnectHooks,
                    s.preReleaseHooks, s.postReleaseHooks,
                    new Cons(hook, s.connectionInvalidationHooks),
                    s.loadBalancingStrategy);
        } while (state.compareAndSet(s, n));
        return this;
    }

    public Configuration removeConnectionInvalidationHook(Hook hook) {
        State s, n;
        do {
            s = state.get();
            n = new State(s.poolSize, s.ttl, s.casArrayType, s.contentionHandler,
                    s.preConnectHooks, s.postConnectHooks,
                    s.preReleaseHooks, s.postReleaseHooks,
                    remove(hook, s.connectionInvalidationHooks),
                    s.loadBalancingStrategy);
        } while (state.compareAndSet(s, n));
        return this;
    }
}
