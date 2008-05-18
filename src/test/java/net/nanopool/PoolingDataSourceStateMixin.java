package net.nanopool;

import javax.sql.DataSource;

import net.nanopool2.NanoPoolDataSource;

import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;

public class PoolingDataSourceStateMixin implements DataSourceState {
	protected int totalSize;
	protected long timeout;

	public PoolingDataSourceStateMixin(int totalSize, long timeout) {
		this.totalSize = totalSize;
		this.timeout = timeout;
	}
	
	public DataSource createSource() {
		MysqlConnectionPoolDataSource cpds =
			new MysqlConnectionPoolDataSource();
		cpds.setUser("root");
		cpds.setPassword("");
		cpds.setServerName("localhost");
		return new NanoPoolDataSource(cpds, totalSize , timeout);
	}

}
