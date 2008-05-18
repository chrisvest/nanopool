package net.nanopool;

import javax.sql.DataSource;

import edu.umd.cs.mtc.MultithreadedTest;

public class PoolTestSupport extends MultithreadedTest {
    protected DataSource source;
    protected int totalSize;
    protected long timeout;
    
    @Override
    public void initialize() {
        super.initialize();
        
    }
}
