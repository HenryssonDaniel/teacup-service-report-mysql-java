package io.github.henryssondaniel.teacup.service.report.mysql.v1._0;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.junit.jupiter.api.Test;

class SessionResourceTest {
  private final DataSource dataSource = mock(DataSource.class);

  @Test
  void sessionResource() {
    assertThat(new SessionResource()).isNotNull();
  }

  @Test
  void summary() throws SQLException {
    var resultSet = mock(ResultSet.class);
    when(resultSet.next()).thenReturn(true, false);

    var preparedStatement = mock(PreparedStatement.class);

    try (var set = preparedStatement.executeQuery()) {
      when(set).thenReturn(resultSet);
    }

    var connection = mock(Connection.class);
    try (var statement = connection.prepareStatement(anyString())) {
      when(statement).thenReturn(preparedStatement);
    }

    try (var conn = dataSource.getConnection()) {
      when(conn).thenReturn(connection);
    }

    var response = createSessionResource();
    assertThat(response.getEntity())
        .isEqualTo(
            "{\"sessions\": [{\"aborted\": 0, \"executions\": 0, \"failed\": 0, \"initialized\": "
                + "\"null\", \"running\": 0, \"skipped\": 0, \"successful\": 0}]}");
    assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
  }

  @Test
  void summaryWhenConnectionError() throws SQLException {
    try (var connection = dataSource.getConnection()) {
      when(connection).thenThrow(new SQLException("test"));
    }

    assertThat(createSessionResource().getStatus())
        .isEqualTo(Status.INTERNAL_SERVER_ERROR.getStatusCode());
  }

  private Response createSessionResource() {
    return new SessionResource(dataSource).summary("5");
  }
}
