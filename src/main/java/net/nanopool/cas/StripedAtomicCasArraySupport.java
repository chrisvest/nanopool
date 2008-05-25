/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.nanopool.cas;

import java.util.concurrent.atomic.AtomicReference;
import net.nanopool.Connector;

/**
 *
 * @author vest
 */
public abstract class StripedAtomicCasArraySupport implements CasArray<Connector> {
    private final AtomicReference<Connector>[] array;
    
    public StripedAtomicCasArraySupport(int size) {
        array = new AtomicReference[size];
        for (int i = 0; i < size; i++) {
            array[i] = new AtomicReference<Connector>();
        }
    }

    public final boolean cas(int idx, Connector newValue, Connector oldValue) {
        return doCas(array[idx], newValue, oldValue);
    }

    public final Connector get(int idx) {
        return array[idx].get();
    }

    public final int length() {
        return array.length;
    }

    protected abstract boolean doCas(AtomicReference<Connector> atomic,
            Connector newValue, Connector oldValue);
}
