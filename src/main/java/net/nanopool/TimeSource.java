/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.nanopool;

/**
 *
 * @author vest
 */
public interface TimeSource {
    long millisecondsToUnit(long millis);
    long now();
}
