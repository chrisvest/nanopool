package net.nanopool;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.nanopool.cas.CasArray;
import net.nanopool.contention.ContentionHandler;
import net.nanopool.hooks.Hook;

final class State {
    private static final Class[] casArrayCtorSign =
            new Class[] {Integer.TYPE};

    final int poolSize;
    final long ttl;
    final Class<? extends CasArray> casArrayType;
    final ContentionHandler contentionHandler;
    final Cons<Hook> preConnectHooks;
    final Cons<Hook> postConnectHooks;
    final Cons<Hook> preReleaseHooks;
    final Cons<Hook> postReleaseHooks;
    final Cons<Hook> connectionInvalidationHooks;

    public State(int poolSize, long ttl, Class<? extends CasArray> casArrayType, ContentionHandler contentionHandler, Cons<Hook> preConnectHooks, Cons<Hook> postConnectHooks, Cons<Hook> preReleaseHook, Cons<Hook> postReleaseHook, Cons<Hook> connectionInvalidationHook) {
        super();
        this.poolSize = poolSize;
        this.ttl = ttl;
        this.casArrayType = casArrayType;
        this.contentionHandler = contentionHandler;
        this.preConnectHooks = preConnectHooks;
        this.postConnectHooks = postConnectHooks;
        this.preReleaseHooks = preReleaseHook;
        this.postReleaseHooks = postReleaseHook;
        this.connectionInvalidationHooks = connectionInvalidationHook;
    }

    <T> CasArray<T> buildCasArray() {
        try {
            Constructor ctor = casArrayType.getConstructor(casArrayCtorSign);
            return (CasArray<T>) ctor.newInstance(poolSize);
        } catch (Exception ex) {
            throw new NanoPoolRuntimeException(
                    "Could not build CasArray instance.", ex);
        }
    }
}
