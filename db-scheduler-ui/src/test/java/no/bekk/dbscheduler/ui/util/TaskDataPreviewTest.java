package no.bekk.dbscheduler.ui.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class TaskDataPreviewTest {

  @Test
  void hidden_isNull() {
    assertThat(TaskDataPreview.of(Map.of("a", 1), false)).isNull();
  }

  @Test
  void null_isNull() {
    assertThat(TaskDataPreview.of(null, true)).isNull();
  }

  @Test
  void short_isCompactJson() {
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("orderId", "88121");
    data.put("amount", 12.5);

    assertThat(TaskDataPreview.of(data, true)).isEqualTo("{\"orderId\":\"88121\",\"amount\":12.5}");
  }

  @Test
  void long_isCutTo100WithEllipsis() {
    String preview = TaskDataPreview.of(Map.of("text", "x".repeat(500)), true);

    assertThat(preview).hasSize(101).startsWith("{\"text\":\"xxx").endsWith("…");
  }
}
