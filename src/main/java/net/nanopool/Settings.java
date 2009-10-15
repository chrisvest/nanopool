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
import net.nanopool.contention.ContentionHandler;
import net.nanopool.contention.DefaultContentionHandler;
import net.nanopool.hooks.Hook;

/**
 * Settings is an atomically mutable container of attributes that describes to a
 * {@link NanoPoolDataSource} how it should operate. The Settings instance can
 * be safely mutated after a NanoPoolDataSource has been created using it.
 * 
 * @author cvh
 * @since 1.0
 */
public class Settings {
  //guarded by this
  private Config config;
  
  Settings(Config cfg) {
    config = cfg;
  }
  
  /**
   * Create a new Settings objected, starting out with default values.
   */
  public Settings() {
    this(new Config(10, 300000, new DefaultContentionHandler(), null, null,
        null, null, null, MilliTime.INSTANCE));
  }
  
  synchronized Config getConfig() {
    return config;
  }
  
  /*
   * Simpler properties.
   */

  /**
   * Get the pool size defined by this Settings object.
   * @return A positive integer.
   */
  public synchronized int getPoolSize() {
    return config.poolSize;
  }
  
  /**
   * Set the pool size for this Settings object. This number specifies how many
   * connections the pool should try to keep ready for use at all times.
   * @param poolSize The pool size, a number greater than or equal to one.
   * @return This Settings object.
   */
  public synchronized Settings setPoolSize(int poolSize) {
    if (poolSize < 1) {
      throw new IllegalArgumentException("Pool size must be at least "
          + "one. " + poolSize + " is too low.");
    }
    config = new Config(poolSize, config.ttl, config.contentionHandler,
        config.preConnectHooks, config.postConnectHooks,
        config.preReleaseHooks, config.postReleaseHooks,
        config.connectionInvalidationHooks, config.time);
    return this;
  }
  
  /**
   * Get the time-to-live defined by this Settings object.
   * @return A long greater than or equal to zero.
   */
  public synchronized long getTimeToLive() {
    return config.ttl;
  }

  /**
   * Set the time-to-live for this Settings object. This number specifies how
   * old, in milliseconds, connections in this pool are allowed to get before
   * they are closed and recreated.
   * @param ttl The time-to-live, a number greater than or equal to zero.
   * @return This Settings object.
   */
  public synchronized Settings setTimeToLive(long ttl) {
    if (ttl < 0) {
      throw new IllegalArgumentException("Time to live must be at "
          + "least zero. " + ttl + " is too low.");
    }
    config = new Config(config.poolSize, ttl, config.contentionHandler,
        config.preConnectHooks, config.postConnectHooks,
        config.preReleaseHooks, config.postReleaseHooks,
        config.connectionInvalidationHooks, config.time);
    return this;
  }
  
  /**
   * Get the {@link ContentionHandler} specified by this Settings object.
   * @return A ContentionHandler instance, never null.
   */
  public synchronized ContentionHandler getContentionHandler() {
    return config.contentionHandler;
  }

  /**
   * Set the {@link ContentionHandler} instance for this Settings object. This
   * object is used as a call-back for when the pool experiences high
   * contention - that is, the demand for connections exceeds the number of
   * connections in the pool.
   * @param contentionHandler The {@link ContentionHandler} to be used.
   * @return This Settings object.
   */
  public synchronized Settings setContentionHandler(ContentionHandler contentionHandler) {
    if (contentionHandler == null) {
      throw new IllegalArgumentException("Contention handler cannot be null.");
    }
      config = new Config(config.poolSize, config.ttl, contentionHandler,
          config.preConnectHooks, config.postConnectHooks,
          config.preReleaseHooks, config.postReleaseHooks,
          config.connectionInvalidationHooks, config.time);
    return this;
  }
  
  synchronized TimeSource getTimeSource() {
    return config.time;
  }
  
  synchronized Settings setTimeSource(TimeSource time) {
    if (time == null) {
      throw new IllegalArgumentException("Time Source should not be null.");
    }
    config = new Config(config.poolSize, config.ttl,
        config.contentionHandler, config.preConnectHooks,
        config.postConnectHooks, config.preReleaseHooks,
        config.postReleaseHooks, config.connectionInvalidationHooks, time);
    return this;
  }
  
  /*
   * Hooks.
   */

  private <T> Cons<T> recurRemove(T obj, Cons<T> from) {
    if (from == null)
      return null;
    if (from.first.equals(obj))
      return from.rest;
    return new Cons<T>(from.first, recurRemove(obj, from.rest));
  }
  
  private <T> Cons<T> remove(T obj, Cons<T> from) {
    if (from == null)
      return null;
    if (!from.contains(obj))
      return from;
    return recurRemove(obj, from);
  }
  
  /**
   * Get a list of the pre-connect {@link Hook} instances associated with this
   * Settings object.
   * @return The possibly empty list of pre-connect hooks, never null nor are
   * the elements null.
   */
  public synchronized List<Hook> getPreConnectHooks() {
    return config.preConnectHooks.toList();
  }
  
  /**
   * Add the given pre-connect {@link Hook} instance to this Settings object.
   * The pre-connect hooks are run once before any connection lease attempt.
   * @param hook The hook to be added.
   * @return This Settings object.
   * @throws NullPointerException if the hook parameter is null.
   */
  public synchronized Settings addPreConnectHook(Hook hook) {
    config = new Config(config.poolSize, config.ttl, config.contentionHandler,
        new Cons<Hook>(hook, config.preConnectHooks), config.postConnectHooks,
        config.preReleaseHooks, config.postReleaseHooks,
        config.connectionInvalidationHooks, config.time);
    return this;
  }
  
  /**
   * Remove the given pre-connect {@link Hook} instance from this Settings
   * object.
   * @param hook The hook to be removed.
   * @return This Settings object.
   */
  public synchronized Settings removePreConnectHook(Hook hook) {
    config = new Config(config.poolSize, config.ttl, config.contentionHandler,
        remove(hook, config.preConnectHooks), config.postConnectHooks,
        config.preReleaseHooks, config.postReleaseHooks,
        config.connectionInvalidationHooks, config.time);
    return this;
  }
  
  /**
   * Get a list of post-connect hooks associated with this Settings object.
   * @return The possible empty list of post-connect hooks, never null nor are
   * the elements null.
   */
  public synchronized List<Hook> getPostConnectHooks() {
    return config.postConnectHooks.toList();
  }
  
  /**
   * Add the given post-connect {@link Hook} instance to this Settings object.
   * <p>
   * The post-connect hooks are run once after a connection lease attempt.
   * As parameters, they either get the connection object itself if the lease
   * was successful, or any SQLException that might have prevented the lease
   * from succeeding.
   * @param hook
   * @return This Settings object.
   * @throws NullPointerException if the hook parameter is null.
   */
  public synchronized Settings addPostConnectHook(Hook hook) {
    config = new Config(config.poolSize, config.ttl, config.contentionHandler,
        config.preConnectHooks, new Cons<Hook>(hook, config.postConnectHooks),
        config.preReleaseHooks, config.postReleaseHooks,
        config.connectionInvalidationHooks, config.time);
    return this;
  }
  
  /**
   * Remove the given post-connect {@link Hook} instance from this Settings
   * object.
   * @param hook The hook to be removed.
   * @return This Settings object.
   */
  public synchronized Settings removePostConnectHook(Hook hook) {
    config = new Config(config.poolSize, config.ttl, config.contentionHandler,
        config.preConnectHooks, remove(hook, config.postConnectHooks),
        config.preReleaseHooks, config.postReleaseHooks,
        config.connectionInvalidationHooks, config.time);
    return this;
  }
  
  /**
   * Get a list of pre-release hooks associated with this Settings object.
   * @return The possibly empty list of pre-release hooks, never null nor are
   * the elements null.
   */
  public synchronized List<Hook> getPreReleaseHooks() {
    return config.preReleaseHooks.toList();
  }
  
  /**
   * Add the given pre-release {@link Hook} instance to this Settings object.
   * <p>
   * The pre-release hooks are run once for a connection being closed, right
   * before it is returned to the pool. As parameters, they get the connection
   * object itself.
   * <p>
   * This happens before any connection age checks, so the
   * connection object will be potentially valid. The connection can still be
   * closed on the server end or otherwise be considered unusable by the JDBC
   * driver.
   * @param hook
   * @return This Settings object.
   * @throws NullPointerException if the hook parameter is null.
   */
  public synchronized Settings addPreReleaseHook(Hook hook) {
    config = new Config(config.poolSize, config.ttl, config.contentionHandler,
        config.preConnectHooks, config.postConnectHooks,
        new Cons<Hook>(hook, config.preReleaseHooks), config.postReleaseHooks,
        config.connectionInvalidationHooks, config.time);
    return this;
  }
  
  /**
   * Remove the given pre-release {@link Hook} instance from this Settings
   * object.
   * @param hook The hook to be removed.
   * @return This Settings object.
   */
  public synchronized Settings removePreReleaseHook(Hook hook) {
    config = new Config(config.poolSize, config.ttl, config.contentionHandler,
        config.preConnectHooks, config.postConnectHooks,
        remove(hook, config.preReleaseHooks), config.postReleaseHooks,
        config.connectionInvalidationHooks, config.time);
    return this;
  }
  
  /**
   * Get a list of post-release hooks associated with this Settings object.
   * @return The possibly empty list of post-release hooks, never null nor are
   * the elements null.
   */
  public synchronized List<Hook> getPostReleaseHooks() {
    return config.postReleaseHooks.toList();
  }
  
  /**
   * Add the given post-release {@link Hook} instance to this Settings object.
   * <p>
   * The post-release hooks are run once for a connection being closed, right
   * after it is returned to the pool. As parameters, they may get the
   * connection object itself.
   * <p>
   * This happens after any connection age checks, and the connection parameter
   * will be null if the connection was closed due to old age. If the
   * connection parameter is not null, then the connection can still be closed
   * on the server end or otherwise be considered unusable by the JDBC driver.
   * If that is the case, then you have probably configured an unreasonably
   * long time-to-live for your connection pool.
   * <p>
   * Since these hooks run <em>after</em> the connection might have return to
   * the pool, you should also consider the possibility that the connection
   * might have been re-leased before or during the execution of the
   * post-release hooks.
   * @param hook
   * @return This Settings object.
   * @throws NullPointerException if the hook parameter is null.
   */
  public synchronized Settings addPostReleaseHook(Hook hook) {
    config = new Config(config.poolSize, config.ttl, config.contentionHandler,
        config.preConnectHooks, config.postConnectHooks,
        config.preReleaseHooks, new Cons<Hook>(hook, config.postReleaseHooks),
        config.connectionInvalidationHooks, config.time);
    return this;
  }
  
  /**
   * Remove the given post-release {@link Hook} instance from this Settings
   * object.
   * @param hook The hook to be removed.
   * @return This Settings object.
   */
  public synchronized Settings removePostReleaseHook(Hook hook) {
    config = new Config(config.poolSize, config.ttl, config.contentionHandler,
        config.preConnectHooks, config.postConnectHooks,
        config.preReleaseHooks, remove(hook, config.postReleaseHooks),
        config.connectionInvalidationHooks, config.time);
    return this;
  }
  
  /**
   * Get a list of connection-invalidation hooks associated with this Settings
   * object.
   * @return The possibly empty list of connection-invalidation hooks, never
   * null nor are the elements null.
   */
  public synchronized List<Hook> getConnectionInvalidationHooks() {
    return config.connectionInvalidationHooks.toList();
  }
  
  /**
   * Add the given connection-invalidation {@link Hook} instance to this
   * Settings object.
   * <p>
   * The connection-invalidation hooks are run once for a connection being
   * invalidated. That is, every time a physical connection is closed. As
   * parameters they get any SQLException instance that might have been thrown
   * in the attempt to close the physical connection.
   * <p>
   * The most common reasons for connection invalidation is old age, and the
   * pool being shut down. But connection-invalidation can also happen if
   * NanoPool tries to recover from unexpected failures when trying to use a
   * connection.
   * @param hook
   * @return This Settings object.
   * @throws NullPointerException if the hook parameter is null.
   */
  public synchronized Settings addConnectionInvalidationHook(Hook hook) {
    config = new Config(config.poolSize, config.ttl, config.contentionHandler,
        config.preConnectHooks, config.postConnectHooks,
        config.preReleaseHooks, config.postReleaseHooks,
        new Cons<Hook>(hook, config.connectionInvalidationHooks), config.time);
    return this;
  }
  
  /**
   * Remove the given connection-invalidation {@link Hook} instance from this
   * Settings object.
   * @param hook The hook to be removed.
   * @return This Settings object.
   */
  public synchronized Settings removeConnectionInvalidationHook(Hook hook) {
    config = new Config(config.poolSize, config.ttl, config.contentionHandler,
        config.preConnectHooks, config.postConnectHooks,
        config.preReleaseHooks, config.postReleaseHooks,
        remove(hook, config.connectionInvalidationHooks), config.time);
    return this;
  }
}
