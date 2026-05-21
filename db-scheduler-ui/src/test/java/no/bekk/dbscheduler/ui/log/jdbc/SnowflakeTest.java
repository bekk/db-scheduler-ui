package no.bekk.dbscheduler.ui.log.jdbc;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class SnowflakeTest {

  @Test
  void generatesUniqueIds() {
    Snowflake snowflake = new Snowflake(1);
    Set<Long> ids = new HashSet<>();
    for (int i = 0; i < 10_000; i++) {
      ids.add(snowflake.nextId());
    }
    assertThat(ids).hasSize(10_000);
  }

  @Test
  void parseRoundTripsNodeIdAndSequence() {
    Snowflake snowflake = new Snowflake(42);
    long id = snowflake.nextId();
    long[] parts = snowflake.parse(id);

    assertThat(parts[1]).isEqualTo(42L);
    assertThat(parts[2]).isGreaterThanOrEqualTo(0L);
  }

  @Test
  void rejectsNodeIdOutOfRange() {
    assertThat(catchThrowable(() -> new Snowflake(-1)))
        .isInstanceOf(IllegalArgumentException.class);
    assertThat(catchThrowable(() -> new Snowflake(Snowflake.maxNodeId + 1)))
        .isInstanceOf(IllegalArgumentException.class);
  }

  private static Throwable catchThrowable(Runnable r) {
    try {
      r.run();
      return null;
    } catch (Throwable t) {
      return t;
    }
  }
}
