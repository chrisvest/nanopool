package net.nanopool.cas;

import java.util.concurrent.atomic.AtomicReferenceArray;

public class WeakAtomicCasArray<T> extends AtomicCasArraySupport<T> {
    public WeakAtomicCasArray(int size) {
        super(size);
    }

    @Override
    protected boolean doCas(AtomicReferenceArray<T> array, int idx, T newValue,
            T oldValue) {
        return array.weakCompareAndSet(idx, oldValue, newValue);
    }
}
