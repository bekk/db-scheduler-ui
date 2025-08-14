package no.bekk.dbscheduler.uistarter.config;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class DbSchedulerUiUtilTest {

  @Test
  void testNormalizeNoPaths() {
    String path = DbSchedulerUiUtil.normalizePaths("", "");
    assertThat(path).isEqualTo("");
  }

  @Test
  void testNormalizeNullPaths() {
    String path = DbSchedulerUiUtil.normalizePaths(null, null);
    assertThat(path).isEqualTo("");
  }

  @Test
  void testNormalizeSinglePath() {
    String path = DbSchedulerUiUtil.normalizePaths(null, "/db-scheduler-ui");
    assertThat(path).isEqualTo("/db-scheduler-ui");
  }

  @Test
  void testNormalizeNoSlash() {
    String path = DbSchedulerUiUtil.normalizePaths(null, "db-scheduler-ui");
    assertThat(path).isEqualTo("/db-scheduler-ui");
  }

  @Test
  void testNormalizePath() {
    String path = DbSchedulerUiUtil.normalizePath("db-scheduler-ui");
    assertThat(path).isEqualTo("/db-scheduler-ui");
  }

  @Test
  void testNormalizePaths() {
    String path = DbSchedulerUiUtil.normalizePaths("/api", "db-scheduler-ui");
    assertThat(path).isEqualTo("/api/db-scheduler-ui");
  }

  @Test
  void testNormalizePathsExtraSlashes() {
    String path = DbSchedulerUiUtil.normalizePaths("/api/", "/db-scheduler-ui");
    assertThat(path).isEqualTo("/api/db-scheduler-ui");
  }

}
