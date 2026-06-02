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
package no.bekk.dbscheduler.uistarter.log;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.kagkarlsson.scheduler.boot.autoconfigure.DbSchedulerAutoConfiguration;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class DbSchedulerUiLogAutoConfigurationCycleTest {

  @Test
  void contextStartsWithoutCycleWhenLogWriterEnabled() {
    new ApplicationContextRunner()
        .withConfiguration(
            AutoConfigurations.of(
                DbSchedulerAutoConfiguration.class, DbSchedulerUiLogAutoConfiguration.class))
        .withBean(DataSource.class, DbSchedulerUiLogAutoConfigurationCycleTest::stubDataSource)
        .withPropertyValues("db-scheduler-ui.log.enabled=true")
        .run(context -> assertThat(context).hasNotFailed());
  }

  private static DataSource stubDataSource() {
    DataSource ds = mock(DataSource.class, RETURNS_DEEP_STUBS);
    try {
      when(ds.getConnection().getMetaData().getDatabaseProductName()).thenReturn("H2");
    } catch (SQLException e) {
      throw new IllegalStateException(e);
    }
    return ds;
  }
}
