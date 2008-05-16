package net.nanopool2;

import java.util.concurrent.atomic.AtomicReferenceArray;

public class AtomicCasArray<T> implements CasArray<T> {
    private final AtomicReferenceArray<T> array;
    
    public AtomicCasArray(int size) {
        array = new AtomicReferenceArray<T>(size);
    }
    
    public boolean cas(int idx, T newValue, T oldValue) {
        return array.compareAndSet(idx, oldValue, newValue);
    }

    public T get(int idx) {
        return array.get(idx);
    }
}
