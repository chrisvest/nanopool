/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.nanopool.cas;

import java.util.concurrent.atomic.AtomicReference;

/**
 *
 * @author vest
 */
public abstract class StripedAtomicCasArraySupport<T>
        extends CasArraySupport<T> implements CasArray<T> {
    private final AtomicReference<T>[] array;
    
    public StripedAtomicCasArraySupport(int size) {
        array = new AtomicReference[size];
        for (int i = 0; i < size; i++) {
            array[i] = new AtomicReference<T>();
        }
    }

    public final boolean cas(int idx, T newValue, T oldValue) {
        return doCas(array[idx], newValue, oldValue);
    }

    public final T get(int idx) {
        return array[idx].get();
    }

    public final int length() {
        return array.length;
    }

    protected abstract boolean doCas(AtomicReference<T> atomic,
            T newValue, T oldValue);
}
