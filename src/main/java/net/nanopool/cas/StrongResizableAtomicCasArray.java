package net.nanopool.cas;

import java.util.concurrent.atomic.AtomicReferenceArray;

public class StrongResizableAtomicCasArray<T> extends AtomicCasArraySupport<T> {
    public StrongResizableAtomicCasArray(int size) {
        super(size);
    }

    @Override
    protected boolean doCas(AtomicReferenceArray<T> array, int idx, T newValue,
            T oldValue) {
        return array.compareAndSet(idx, oldValue, newValue);
    }
}
