package no.bekk.dbscheduler.uistarter.autoconfigure;

import static org.assertj.core.api.Assertions.assertThatCode;
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
  void throwsWhenLegacyEnabledIsFalse() {
    MockEnvironment env = new MockEnvironment().withProperty("db-scheduler-log.enabled", "false");

    assertThatThrownBy(() -> processor.postProcessEnvironment(env, application))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("db-scheduler-ui.log.enabled=false");
  }

  @Test
  void throwsWhenOnlyLegacyTableNameSet() {
    MockEnvironment env =
        new MockEnvironment().withProperty("db-scheduler-log.table-name", "my_logs");

    assertThatThrownBy(() -> processor.postProcessEnvironment(env, application))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("db-scheduler-ui.log.table-name=my_logs");
  }

  @Test
  void includesBothMigrationHintsWhenBothSet() {
    MockEnvironment env =
        new MockEnvironment()
            .withProperty("db-scheduler-log.enabled", "true")
            .withProperty("db-scheduler-log.table-name", "my_logs");

    assertThatThrownBy(() -> processor.postProcessEnvironment(env, application))
        .hasMessageContaining("db-scheduler-ui.log.enabled=true")
        .hasMessageContaining("db-scheduler-ui.log.table-name=my_logs");
  }

  @Test
  void doesNotThrowWhenNoLegacyPropertiesPresent() {
    MockEnvironment env = new MockEnvironment().withProperty("db-scheduler-ui.log.enabled", "true");

    assertThatCode(() -> processor.postProcessEnvironment(env, application))
        .doesNotThrowAnyException();
  }
}
