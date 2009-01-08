/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.nanopool.hooks;

/**
 *
 * @author cvh
 */
public enum EventType {
    preConnect,
    postConnect,
    preRelease,
    postRelease,
    invalidation;

    @Override
    public String toString() {
        switch (this) {
            case preConnect: return "Pre-connect";
            case postConnect: return "Post-connect";
            case preRelease: return "Pre-release";
            case postRelease: return "Post-release";
            case invalidation: return "Invalidation";
        }
        return super.toString();
    }
}
