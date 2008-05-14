package net.nanopool;

import javax.sql.DataSource;

public interface DataSourceState {
	DataSource createSource();
}
