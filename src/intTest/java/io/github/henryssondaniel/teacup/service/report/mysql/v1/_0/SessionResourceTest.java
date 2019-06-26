package io.github.henryssondaniel.teacup.service.report.mysql.v1._0;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SessionResourceTest {
  @Test
  void summary() {
    assertThat(new SessionResource().summary("5")).isNotNull();
  }
}
