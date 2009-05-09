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
import net.nanopool.contention.ContentionHandler;
import net.nanopool.contention.DefaultContentionHandler;
import net.nanopool.hooks.Hook;
import net.nanopool.loadbalancing.RandomStrategy;
import net.nanopool.loadbalancing.Strategy;

/**
 * Settings is an atomically mutable container of attributes that describes to a
 * {@link NanoPoolDataSource} how it should operate. The Settings instance
 * can be safely mutated after a NanoPoolDataSource has been created using it.
 * @author cvh
 * @since 1.0
 */
public class Settings {
    private final AtomicReference<Config> config = new AtomicReference<Config>();

    Settings(Config cfg) {
        this.config.set(cfg);
    }

    public Settings() {
        this(new Config(
                10, 300000,
                new DefaultContentionHandler(), null, null, null, null, null,
                RandomStrategy.INSTANCE, MilliTime.INSTANCE
        ));
    }

    Config getConfig() {
        return config.get();
    }

    /*
     * Simpler properties.
     */

    public int getPoolSize() {
        return config.get().poolSize;
    }

    public Settings setPoolSize(int poolSize) {
        if (poolSize < 1) {
            throw new IllegalArgumentException("Pool size must be at least " +
                    "one. " + poolSize + " is too low.");
        }
        Config s, n;
        do {
            s = config.get();
            n = new Config(poolSize, s.ttl, s.contentionHandler,
                    s.preConnectHooks, s.postConnectHooks, s.preReleaseHooks,
                    s.postReleaseHooks, s.connectionInvalidationHooks,
                    s.loadBalancingStrategy, s.time);
        } while (!config.compareAndSet(s, n));
        return this;
    }

    public long getTimeToLive() {
        return config.get().ttl;
    }

    public Settings setTimeToLive(long ttl) {
        if (ttl < 0) {
            throw new IllegalArgumentException("Time to live must be at " +
                    "least zero. " + ttl +" is too low.");
        }
        Config s, n;
        do {
            s = config.get();
            n = new Config(s.poolSize, ttl, s.contentionHandler,
                    s.preConnectHooks, s.postConnectHooks, s.preReleaseHooks,
                    s.postReleaseHooks, s.connectionInvalidationHooks,
                    s.loadBalancingStrategy, s.time);
        } while (!config.compareAndSet(s, n));
        return this;
    }

    public ContentionHandler getContentionHandler() {
        return config.get().contentionHandler;
    }

    public Settings setContentionHandler(ContentionHandler contentionHandler) {
        if (contentionHandler == null) {
            throw new IllegalArgumentException(
                    "Contention handler cannot be null.");
        }
        Config s, n;
        do {
            s = config.get();
            n = new Config(s.poolSize, s.ttl, contentionHandler,
                    s.preConnectHooks, s.postConnectHooks, s.preReleaseHooks,
                    s.postReleaseHooks, s.connectionInvalidationHooks,
                    s.loadBalancingStrategy, s.time);
        } while (!config.compareAndSet(s, n));
        return this;
    }

    public Strategy getLoadBalancingStrategy() {
        return config.get().loadBalancingStrategy;
    }

    public Settings setLoadBalancingStrategy(Strategy strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException(
                    "Load balancing strategy should not be null.");
        }
        Config s, n;
        do {
            s = config.get();
            n = new Config(s.poolSize, s.ttl, s.contentionHandler,
                    s.preConnectHooks, s.postConnectHooks, s.preReleaseHooks,
                    s.postReleaseHooks, s.connectionInvalidationHooks,
                    strategy, s.time);
        } while (!config.compareAndSet(s, n));
        return this;
    }

    TimeSource getTimeSource() {
        return config.get().time;
    }

    Settings setTimeSource(TimeSource time) {
        if (time == null) {
            throw new IllegalArgumentException(
                    "Time Source should not be null.");
        }
        Config s, n;
        do {
            s = config.get();
            n = new Config(s.poolSize, s.ttl, s.contentionHandler,
                    s.preConnectHooks, s.postConnectHooks, s.preReleaseHooks,
                    s.postReleaseHooks, s.connectionInvalidationHooks,
                    s.loadBalancingStrategy, time);
        } while (!config.compareAndSet(s, n));
        return this;
    }

    /*
     * Hooks.
     */

    private <T> Cons<T> recurRemove(T obj, Cons<T> from) {
        if (from == null) return null;
        if (from.first.equals(obj)) return from.rest;
        return new Cons<T>(from.first, recurRemove(obj, from.rest));
    }

    private <T> Cons<T> remove(T obj, Cons<T> from) {
        if (from == null) return null;
        if (!from.contains(obj)) return from;
        return recurRemove(obj, from);
    }

    public List<Hook> getPreConnectHooks() {
        return config.get().preConnectHooks.toList();
    }

    public Settings addPreConnectHook(Hook hook) {
        Config s, n;
        do {
            s = config.get();
            n = new Config(s.poolSize, s.ttl, s.contentionHandler,
                    new Cons<Hook>(hook, s.preConnectHooks), s.postConnectHooks,
                    s.preReleaseHooks, s.postReleaseHooks,
                    s.connectionInvalidationHooks, s.loadBalancingStrategy,
                    s.time);
        } while (!config.compareAndSet(s, n));
        return this;
    }

    public Settings removePreConnectHook(Hook hook) {
        Config s, n;
        do {
            s = config.get();
            n = new Config(s.poolSize, s.ttl, s.contentionHandler,
                    remove(hook, s.preConnectHooks), s.postConnectHooks,
                    s.preReleaseHooks, s.postReleaseHooks,
                    s.connectionInvalidationHooks, s.loadBalancingStrategy,
                    s.time);
        } while (!config.compareAndSet(s, n));
        return this;
    }

    public List<Hook> getPostConnectHooks() {
        return config.get().postConnectHooks.toList();
    }

    public Settings addPostConnectHook(Hook hook) {
        Config s, n;
        do {
            s = config.get();
            n = new Config(s.poolSize, s.ttl, s.contentionHandler,
                    s.preConnectHooks, new Cons<Hook>(hook, s.postConnectHooks),
                    s.preReleaseHooks, s.postReleaseHooks,
                    s.connectionInvalidationHooks, s.loadBalancingStrategy,
                    s.time);
        } while (!config.compareAndSet(s, n));
        return this;
    }

    public Settings removePostConnectHook(Hook hook) {
        Config s, n;
        do {
            s = config.get();
            n = new Config(s.poolSize, s.ttl, s.contentionHandler,
                    s.preConnectHooks, remove(hook, s.postConnectHooks),
                    s.preReleaseHooks, s.postReleaseHooks,
                    s.connectionInvalidationHooks, s.loadBalancingStrategy,
                    s.time);
        } while (!config.compareAndSet(s, n));
        return this;
    }

    public List<Hook> getPreReleaseHooks() {
        return config.get().preReleaseHooks.toList();
    }

    public Settings addPreReleaseHook(Hook hook) {
        Config s, n;
        do {
            s = config.get();
            n = new Config(s.poolSize, s.ttl, s.contentionHandler,
                    s.preConnectHooks, s.postConnectHooks,
                    new Cons<Hook>(hook, s.preReleaseHooks), s.postReleaseHooks,
                    s.connectionInvalidationHooks, s.loadBalancingStrategy,
                    s.time);
        } while (!config.compareAndSet(s, n));
        return this;
    }

    public Settings removePreReleaseHook(Hook hook) {
        Config s, n;
        do {
            s = config.get();
            n = new Config(s.poolSize, s.ttl, s.contentionHandler,
                    s.preConnectHooks, s.postConnectHooks,
                    remove(hook, s.preReleaseHooks), s.postReleaseHooks,
                    s.connectionInvalidationHooks, s.loadBalancingStrategy,
                    s.time);
        } while (!config.compareAndSet(s, n));
        return this;
    }

    public List<Hook> getPostReleaseHooks() {
        return config.get().postReleaseHooks.toList();
    }

    public Settings addPostReleaseHook(Hook hook) {
        Config s, n;
        do {
            s = config.get();
            n = new Config(s.poolSize, s.ttl, s.contentionHandler,
                    s.preConnectHooks, s.postConnectHooks,
                    s.preReleaseHooks, new Cons<Hook>(hook, s.postReleaseHooks),
                    s.connectionInvalidationHooks, s.loadBalancingStrategy,
                    s.time);
        } while (!config.compareAndSet(s, n));
        return this;
    }

    public Settings removePostReleaseHook(Hook hook) {
        Config s, n;
        do {
            s = config.get();
            n = new Config(s.poolSize, s.ttl, s.contentionHandler,
                    s.preConnectHooks, s.postConnectHooks,
                    s.preReleaseHooks, remove(hook, s.postReleaseHooks),
                    s.connectionInvalidationHooks, s.loadBalancingStrategy,
                    s.time);
        } while (!config.compareAndSet(s, n));
        return this;
    }

    public List<Hook> getConnectionInvalidationHooks() {
        return config.get().connectionInvalidationHooks.toList();
    }

    public Settings addConnectionInvalidationHook(Hook hook) {
        Config s, n;
        do {
            s = config.get();
            n = new Config(s.poolSize, s.ttl, s.contentionHandler,
                    s.preConnectHooks, s.postConnectHooks,
                    s.preReleaseHooks, s.postReleaseHooks,
                    new Cons<Hook>(hook, s.connectionInvalidationHooks),
                    s.loadBalancingStrategy, s.time);
        } while (!config.compareAndSet(s, n));
        return this;
    }

    public Settings removeConnectionInvalidationHook(Hook hook) {
        Config s, n;
        do {
            s = config.get();
            n = new Config(s.poolSize, s.ttl, s.contentionHandler,
                    s.preConnectHooks, s.postConnectHooks,
                    s.preReleaseHooks, s.postReleaseHooks,
                    remove(hook, s.connectionInvalidationHooks),
                    s.loadBalancingStrategy, s.time);
        } while (!config.compareAndSet(s, n));
        return this;
    }
}
