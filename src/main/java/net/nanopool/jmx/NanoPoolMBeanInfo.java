package net.nanopool.jmx;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;

/**
 *
 * @author cvh
 */
public class NanoPoolMBeanInfo {
    public static final MBeanInfo INSTANCE;

    static {
        String name = "NanoPoolDataSource";
        String description = "";
        MBeanAttributeInfo[] attributes = null;
        MBeanConstructorInfo[] constructors = null;
        MBeanOperationInfo[] operations = null;
        MBeanNotificationInfo[] notifications = null;
        
        INSTANCE = new MBeanInfo(name, description, attributes, constructors,
                operations, notifications);
    }
}
