package net.nanopool;

import javax.sql.DataSource;

public abstract class DataSourceSupport implements DataSource {
    protected final Log log;
    
    public DataSourceSupport(Log log) {
        String className = getClass().getSimpleName();
        this.log = log.subLoggerFor(className);
    }
}
