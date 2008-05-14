package net.nanopool;

import javax.sql.DataSource;

import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;

public class PoolingDataSourceStateMixin implements DataSourceState {
	protected int totalSize;
	protected long timeout;
	protected Log log;

	public PoolingDataSourceStateMixin(int totalSize, long timeout, Log log) {
		this.totalSize = totalSize;
		this.timeout = timeout;
		this.log = log;
	}
	
	public DataSource createSource() {
		MysqlConnectionPoolDataSource cpds =
			new MysqlConnectionPoolDataSource();
		cpds.setUser("root");
		cpds.setPassword("");
		cpds.setServerName("localhost");
		return new PoolingDataSource(cpds, totalSize , timeout, log);
	}

}
