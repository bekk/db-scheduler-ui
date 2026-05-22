/*
 * Copyright (C) Marten Prieß
 * Originally part of https://github.com/rocketbase-io/db-scheduler-log
 * Copyright (C) Bekk (modifications)
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

import com.github.kagkarlsson.scheduler.boot.config.DbSchedulerCustomizer;
import com.github.kagkarlsson.scheduler.exceptions.SerializationException;
import com.github.kagkarlsson.scheduler.serializer.Serializer;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import javax.sql.DataSource;
import no.bekk.dbscheduler.ui.log.LogRepository;
import no.bekk.dbscheduler.ui.log.jdbc.IdProvider;
import no.bekk.dbscheduler.ui.log.jdbc.JdbcLogRepository;
import no.bekk.dbscheduler.ui.log.jdbc.Snowflake;
import no.bekk.dbscheduler.ui.log.listener.LogSchedulerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.ConfigurableObjectInputStream;

@AutoConfiguration
@EnableConfigurationProperties(DbSchedulerUiLogProperties.class)
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
@ConditionalOnBean(DataSource.class)
@ConditionalOnProperty(value = "db-scheduler-ui.log.enabled", havingValue = "true")
public class DbSchedulerUiLogAutoConfiguration {

  private static final Logger log =
      LoggerFactory.getLogger(DbSchedulerUiLogAutoConfiguration.class);

  private final DbSchedulerUiLogProperties config;
  private final DataSource existingDataSource;

  public DbSchedulerUiLogAutoConfiguration(
      DbSchedulerUiLogProperties config, DataSource dataSource) {
    this.config = config;
    this.existingDataSource = dataSource;
  }

  @Bean
  @ConditionalOnMissingBean(IdProvider.class)
  IdProvider idProvider() {
    log.debug("No IdProvider bean found, creating a Snowflake instance");
    return new Snowflake();
  }

  @Bean
  @ConditionalOnMissingBean(LogRepository.class)
  LogRepository logRepository(IdProvider idProvider, @Lazy DbSchedulerCustomizer customizer) {
    log.debug("No LogRepository bean found, creating a JdbcLogRepository");
    return new JdbcLogRepository(
        existingDataSource,
        () -> customizer.serializer().orElse(SPRING_JAVA_SERIALIZER),
        config.tableName(),
        idProvider);
  }

  @Bean(destroyMethod = "close")
  @ConditionalOnMissingBean(LogSchedulerListener.class)
  LogSchedulerListener logSchedulerListener(LogRepository logRepository) {
    log.debug("Registering LogSchedulerListener for execution logging");
    return new LogSchedulerListener(logRepository);
  }

  /**
   * {@link Serializer} compatible with Spring Boot Devtools' restart classloader.
   *
   * <p>See <a
   * href="https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#using-boot-devtools-known-restart-limitations">Devtools
   * known limitations</a>.
   */
  private static final Serializer SPRING_JAVA_SERIALIZER =
      new Serializer() {

        @Override
        public byte[] serialize(Object data) {
          if (data == null) return null;
          try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
              ObjectOutput out = new ObjectOutputStream(bos)) {
            out.writeObject(data);
            return bos.toByteArray();
          } catch (Exception e) {
            throw new SerializationException("Failed to serialize object", e);
          }
        }

        @Override
        public <T> T deserialize(Class<T> clazz, byte[] serializedData) {
          if (serializedData == null) return null;
          try (ByteArrayInputStream bis = new ByteArrayInputStream(serializedData);
              ObjectInput in =
                  new ConfigurableObjectInputStream(
                      bis, Thread.currentThread().getContextClassLoader())) {
            return clazz.cast(in.readObject());
          } catch (Exception e) {
            throw new SerializationException("Failed to deserialize object", e);
          }
        }
      };
}
