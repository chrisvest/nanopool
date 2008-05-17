package net.nanopool2.cas;

import java.util.concurrent.atomic.AtomicReferenceArray;

public class StrongAtomicCasArray<T> extends AtomicCasArraySupport<T> {
    public StrongAtomicCasArray(int size) {
        super(size);
    }

    @Override
    protected boolean doCas(AtomicReferenceArray<T> array, int idx, T newValue,
            T oldValue) {
        return array.compareAndSet(idx, oldValue, newValue);
    }
    
}
