/*
 * Copyright (C) Bekk
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package no.bekk.dbscheduler.ui.testsupport;

import java.util.concurrent.atomic.AtomicInteger;
import javax.sql.DataSource;
import org.h2.jdbcx.JdbcDataSource;

public final class TestDatabase {

  private static final AtomicInteger DB_COUNTER = new AtomicInteger();

  private TestDatabase() {}

  public static DataSource newDataSourceWithTables() {
    DataSource ds = newDataSource();
    LogsTable.createSchema(ds);
    return ds;
  }

  private static DataSource newDataSource() {
    JdbcDataSource ds = new JdbcDataSource();
    ds.setURL(
        "jdbc:h2:mem:db-scheduler-ui-test-"
            + DB_COUNTER.incrementAndGet()
            + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
    ds.setUser("sa");
    return ds;
  }
}
