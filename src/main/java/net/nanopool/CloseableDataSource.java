/*
   Copyright 2008-2009 Christian Vest Hansen

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package net.nanopool;

import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;

/**
 * A CloseableDataSource may contain physical database connections and other
 * resources that should be properly closed when the DataSource is no longer in
 * use. A closed data source may throw InvalidStateException on all methods,
 * except close.
 * 
 * @author vest
 */
public interface CloseableDataSource extends DataSource {
  /**
   * Fully close this DataSource. This method is idempotent, so closing and
   * already closed DataSource is guaranteed to not do any harm, such as
   * throwing an exception.
   * 
   * @return A List of the SQLExceptions generated while closing the physical
   *         database connections, or an empty list if the data source is
   *         already closed.
   */
  public abstract List<SQLException> close();
}
