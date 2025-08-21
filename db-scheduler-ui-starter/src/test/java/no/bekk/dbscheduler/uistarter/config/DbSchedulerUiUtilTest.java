package no.bekk.dbscheduler.uistarter.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

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

  @Test
  void testNormalize3Paths() {
    String path = DbSchedulerUiUtil.normalizePaths("/api/", "/v1/", "/db-scheduler-ui");
    assertThat(path).isEqualTo("/api/v1/db-scheduler-ui");
  }

  @Test
  void testNormalize3PathsNulls() {
    String path = DbSchedulerUiUtil.normalizePaths(null, "/v1/", "/db-scheduler-ui");
    assertThat(path).isEqualTo("/v1/db-scheduler-ui");

    path = DbSchedulerUiUtil.normalizePaths("/v2/", "", "/db-scheduler-ui");
    assertThat(path).isEqualTo("/v2/db-scheduler-ui");

    path = DbSchedulerUiUtil.normalizePaths("/v3/", null, "/db-scheduler-ui");
    assertThat(path).isEqualTo("/v3/db-scheduler-ui");
  }
}
