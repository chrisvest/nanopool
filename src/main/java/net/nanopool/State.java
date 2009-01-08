package net.nanopool;

import java.lang.reflect.Constructor;
import net.nanopool.cas.CasArray;
import net.nanopool.contention.ContentionHandler;
import net.nanopool.hooks.Hook;

final class State {
    private static final Class[] casArrayCtorSign =
            new Class[] {Integer.TYPE};

    /**
     * Note on poolSize: It should _only_ be used for building the CasArray,
     * because in future versions we might allow resizing of the pool, and
     * therefor this value might grow stale.
     */
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
        return buildCasArray(poolSize);
    }

    <T> CasArray<T> buildCasArray(int size) {
        try {
            Constructor ctor = casArrayType.getConstructor(casArrayCtorSign);
            return (CasArray<T>) ctor.newInstance(size);
        } catch (Exception ex) {
            throw new NanoPoolRuntimeException(
                    "Could not build CasArray instance.", ex);
        }
    }
}
