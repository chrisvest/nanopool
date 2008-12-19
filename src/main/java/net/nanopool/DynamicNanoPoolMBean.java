package net.nanopool;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.ReflectionException;
import net.nanopool.jmx.NanoPoolMBeanInfo;

/**
 *
 * @author cvh
 */
class DynamicNanoPoolMBean implements DynamicMBean {
    static final String attr_currentOpenConnections = "currentOpenConnections";
    static final String attr_poolSize = "poolSize";
    static final String attr_connectionTimeToLive = "connectionTimeToLive";

    private final NanoPoolDataSource np;

    public DynamicNanoPoolMBean(NanoPoolDataSource np) {
        this.np = np;
    }

    public Object getAttribute(String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException {
        if (attribute == null) {
            return null;
        } else if (attr_currentOpenConnections.equals(attribute)) {
            return np.fsm.countOpenConnections(np.connectors);
        } else if (attr_poolSize.equals(attribute)) {
            return np.poolSize;
        } else if (attr_connectionTimeToLive.equals(attribute)) {
            return np.timeToLive;
        }
        throw new AttributeNotFoundException("No such attribute: " + attribute);
    }

    public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public AttributeList getAttributes(String[] attributes) {
        AttributeList al = new AttributeList();
        for (String attrName : attributes) {
            try {
                Object val = getAttribute(attrName);
                Attribute attr = new Attribute(attrName, val);
                al.add(attr);
            } catch (Exception ex) {
                Attribute attr = new Attribute(attrName, ex);
                al.add(attr);
            }
        }
        return al;
    }

    public AttributeList setAttributes(AttributeList attributes) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Object invoke(String actionName, Object[] params, String[] signature) throws MBeanException, ReflectionException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public MBeanInfo getMBeanInfo() {
        return NanoPoolMBeanInfo.INSTANCE;
    }

}
