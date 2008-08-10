package net.nanopool;

import javax.sql.DataSource;

import net.nanopool.cas.CasArray;

public interface DataSourceState {
	DataSource createSource();
	CasArray<Connector> createCasArray(int size);
	ContentionHandler createContentionHandler();
}
