/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.nanopool;

/**
 *
 * @author cvh
 */
public interface NanoPoolManagerMBean {
    int getCurrentOpenConnectionsCount();
    int getPoolSize();
    long getConnectionTimeToLive();
    String getContentionHandlerClassName();
    String getContentionHandler();
}
