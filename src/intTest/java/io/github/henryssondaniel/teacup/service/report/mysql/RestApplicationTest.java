package io.github.henryssondaniel.teacup.service.report.mysql;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.henryssondaniel.teacup.service.report.mysql.v1._0.SessionResource;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class RestApplicationTest {
  @Test
  void getClasses() {
    assertThat(new RestApplication().getClasses())
        .containsOnlyElementsOf(Collections.singletonList(SessionResource.class));
  }
}
