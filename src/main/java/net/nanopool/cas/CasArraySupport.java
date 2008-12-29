package net.nanopool.cas;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 *
 * @author cvh
 */
public abstract class CasArraySupport<T> implements CasArray<T>  {
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
