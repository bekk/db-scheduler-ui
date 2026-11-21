package no.bekk.dbscheduler.uistarter.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.kagkarlsson.scheduler.boot.config.DbSchedulerCustomizer;
import com.github.kagkarlsson.scheduler.boot.config.DbSchedulerOverrides;
import com.github.kagkarlsson.scheduler.serializer.Serializer;
import java.util.Optional;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

class SchedulerSettingsTest {

  private final DataSource dataSource = new SimpleDriverDataSource();
  private final Serializer serializer = Serializer.DEFAULT_JAVA_SERIALIZER;

  @Test
  void noBeans_bothEmpty() {
    SchedulerSettings settings = SchedulerSettings.resolve(null, null);

    assertThat(settings.dataSource()).isEmpty();
    assertThat(settings.serializer()).isEmpty();
  }

  @Test
  void overridesOnly_usesOverrides() {
    DbSchedulerOverrides overrides =
        DbSchedulerOverrides.builder().dataSource(dataSource).serializer(serializer).build();

    SchedulerSettings settings = SchedulerSettings.resolve(overrides, null);

    assertThat(settings.dataSource()).containsSame(dataSource);
    assertThat(settings.serializer()).containsSame(serializer);
  }

  @Test
  void customizerOnly_usesCustomizer() {
    SchedulerSettings settings = SchedulerSettings.resolve(null, customizer());

    assertThat(settings.dataSource()).containsSame(dataSource);
    assertThat(settings.serializer()).containsSame(serializer);
  }

  @Test
  void both_overridesWinWhole() {
    DbSchedulerOverrides overrides = DbSchedulerOverrides.builder().dataSource(dataSource).build();

    SchedulerSettings settings = SchedulerSettings.resolve(overrides, customizer());

    assertThat(settings.dataSource()).containsSame(dataSource);
    assertThat(settings.serializer()).isEmpty();
  }

  @SuppressWarnings("removal")
  private DbSchedulerCustomizer customizer() {
    return new DbSchedulerCustomizer() {
      @Override
      public Optional<DataSource> dataSource() {
        return Optional.of(dataSource);
      }

      @Override
      public Optional<Serializer> serializer() {
        return Optional.of(serializer);
      }
    };
  }
}
