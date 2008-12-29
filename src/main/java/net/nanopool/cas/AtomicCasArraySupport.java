package net.nanopool.cas;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicReferenceArray;


public abstract class AtomicCasArraySupport<T> implements CasArray<T> {
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

    public Iterator<T> iterator() {
        return new Iterator<T>() {
            int cursor = 0;
            public boolean hasNext() {
                return cursor == length();
            }

            public T next() {
                if (!hasNext()) throw new NoSuchElementException();
                T t = get(cursor);
                cursor++;
                return t;
            }

            public void remove() {
                throw new UnsupportedOperationException(
                        "CasArray Iterators do not support remove().");
            }
        };
    }
}
