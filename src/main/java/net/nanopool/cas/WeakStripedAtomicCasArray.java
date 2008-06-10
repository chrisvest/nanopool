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
public class WeakStripedAtomicCasArray<T> extends StripedAtomicCasArraySupport<T> {
    public WeakStripedAtomicCasArray(int size) {
        super(size);
    }
    
    @Override
    protected boolean doCas(AtomicReference<T> atomic, T newValue, T oldValue) {
        return atomic.weakCompareAndSet(oldValue, newValue);
    }
}
