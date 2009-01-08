/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.nanopool.cas;

/**
 *
 * @author cvh
 */
public interface ResizableCasArray<T> extends CasArray<T> {
    void setCasDelegate(CasArray<T> delegate);
}
