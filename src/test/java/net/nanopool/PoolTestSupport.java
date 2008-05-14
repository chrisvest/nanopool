package net.nanopool;

import javax.sql.DataSource;

import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;

import edu.umd.cs.mtc.MultithreadedTest;

public class PoolTestSupport extends MultithreadedTest {
	private static final Log log = null;
	protected DataSource source;
	protected int totalSize;
	protected long timeout;

	@Override
	public void initialize() {
		super.initialize();
		
	}
	
	
}
