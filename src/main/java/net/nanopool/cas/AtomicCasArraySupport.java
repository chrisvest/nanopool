package net.nanopool.cas;

import java.util.concurrent.atomic.AtomicReferenceArray;


public abstract class AtomicCasArraySupport<T>
        extends CasArraySupport<T> implements CasArray<T> {
    private final AtomicReferenceArray<T> array;
    
    public AtomicCasArraySupport(int size) {
        array = new AtomicReferenceArray<T>(size);
    }
    
    public final boolean cas(int idx, T newValue, T oldValue) {
        return doCas(array, idx, newValue, oldValue);
    }

    public final T get(int idx) {
        return array.get(idx);
    }
    
    public final int length() {
        return array.length();
    }
    
    protected abstract boolean doCas(AtomicReferenceArray<T> array, int idx,
            T newValue, T oldValue);
}
