package no.bekk.dbscheduler.uistarter.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.mock.env.MockEnvironment;

class LegacyLogPropertyEnvironmentPostProcessorTest {

  private final LegacyLogPropertyEnvironmentPostProcessor processor =
      new LegacyLogPropertyEnvironmentPostProcessor();
  private final SpringApplication application = new SpringApplication();

  @Test
  void throwsWhenLegacyEnabledIsTrue() {
    MockEnvironment env = new MockEnvironment().withProperty("db-scheduler-log.enabled", "true");

    assertThatThrownBy(() -> processor.postProcessEnvironment(env, application))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("db-scheduler-ui.log")
        .hasMessageContaining("no longer supported");
  }

  @Test
  void includesTableNameMigrationHintWhenSet() {
    MockEnvironment env =
        new MockEnvironment()
            .withProperty("db-scheduler-log.enabled", "true")
            .withProperty("db-scheduler-log.table-name", "my_logs");

    assertThatThrownBy(() -> processor.postProcessEnvironment(env, application))
        .hasMessageContaining("my_logs")
        .hasMessageContaining("db-scheduler-ui.log.table-name=my_logs");
  }

  @Test
  void doesNotThrowWhenLegacyEnabledIsFalse() {
    MockEnvironment env = new MockEnvironment().withProperty("db-scheduler-log.enabled", "false");
    processor.postProcessEnvironment(env, application);
  }

  @Test
  void doesNotThrowWhenLegacyEnabledAbsent() {
    MockEnvironment env =
        new MockEnvironment().withProperty("db-scheduler-log.table-name", "leftover");
    processor.postProcessEnvironment(env, application);
    assertThat(env.getProperty("db-scheduler-log.table-name")).isEqualTo("leftover");
  }
}
