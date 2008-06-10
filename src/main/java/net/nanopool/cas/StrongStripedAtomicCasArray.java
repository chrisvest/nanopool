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
public class StrongStripedAtomicCasArray<T> extends StripedAtomicCasArraySupport<T> {
    public StrongStripedAtomicCasArray(int size) {
        super(size);
    }
    
    @Override
    protected boolean doCas(AtomicReference<T> atomic, T newValue, T oldValue) {
        return atomic.compareAndSet(oldValue, newValue);
    }
}
