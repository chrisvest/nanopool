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
public class StrongStripedAtomicCasArray extends StripedAtomicCasArraySupport {
    public StrongStripedAtomicCasArray(int size) {
        super(size);
    }
    
    @Override
    protected boolean doCas(AtomicReference<Connector> atomic, Connector newValue, Connector oldValue) {
        return atomic.compareAndSet(oldValue, newValue);
    }
}
