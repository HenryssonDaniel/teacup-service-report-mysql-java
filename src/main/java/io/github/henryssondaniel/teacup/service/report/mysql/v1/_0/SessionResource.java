package io.github.henryssondaniel.teacup.service.report.mysql.v1._0;

import static io.github.henryssondaniel.teacup.service.report.mysql.Utils.createMySqlDataSource;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

/**
 * Session resource. Handles session related requests.
 *
 * @since 1.0
 */
@Path("{a:v1/session|v1.0/session|session}")
public class SessionResource {
  private static final Logger LOGGER = Logger.getLogger(SessionResource.class.getName());
  private final DataSource dataSource;

  /**
   * Constructor.
   *
   * @since 1.0
   */
  public SessionResource() {
    this(createMySqlDataSource());
  }

  SessionResource(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  /**
   * Summary.
   *
   * @param limit the limit
   * @return the response
   * @since 1.0
   */
  @GET
  @Path("summary")
  @Produces(MediaType.APPLICATION_JSON)
  public Response summary(@DefaultValue("5") @QueryParam("limit") String limit) {
    LOGGER.log(Level.FINE, "Summary");

    ResponseBuilder responseBuilder;

    try (var connection = dataSource.getConnection();
        var preparedStatement =
            connection.prepareStatement(
                "SELECT COUNT(`execution`.`id`) AS executions, "
                    + "SUM(if(result.status = 'successful', 1, 0)) AS successful, "
                    + "SUM(if(result.status = 'failed', 1, 0)) AS failed, "
                    + "SUM(if(result.status = 'aborted', 1, 0)) AS aborted, "
                    + "SUM(if(result.status IS NULL AND result.started IS NOT NULL, 1, 0)) AS running, "
                    + "COUNT(`skipped`.`id`) AS skipped, "
                    + "initialized "
                    + "FROM `teacup_report`.`session_execution` "
                    + "LEFT JOIN `teacup_report`.`execution` ON `session_execution`.`id` = `execution`.`session_execution` "
                    + "LEFT JOIN `teacup_report`.`skipped` ON `execution`.`id` = `skipped`.`execution` "
                    + "LEFT JOIN `teacup_report`.`result` ON `execution`.`id` = `result`.`execution` "
                    + "GROUP BY `session_execution`.`id` "
                    + "ORDER BY `session_execution`.`id` DESC "
                    + "LIMIT ?;")) {
      preparedStatement.setInt(1, Integer.parseInt(limit));

      responseBuilder = summary(preparedStatement);
    } catch (SQLException e) {
      LOGGER.log(Level.SEVERE, "An error occurred during summary", e);
      responseBuilder = Response.serverError();
    }

    return responseBuilder.build();
  }

  private static ResponseBuilder summary(PreparedStatement preparedStatement) throws SQLException {
    ResponseBuilder responseBuilder;
    try (var resultSet = preparedStatement.executeQuery()) {
      var stringBuilder = new StringBuilder(0);

      while (resultSet.next())
        stringBuilder
            .append(stringBuilder.length() == 0 ? "" : ", ")
            .append("{\"aborted\": ")
            .append(resultSet.getInt("aborted"))
            .append(", \"executions\": ")
            .append(resultSet.getInt("executions"))
            .append(", \"failed\": ")
            .append(resultSet.getInt("failed"))
            .append(", \"initialized\": \"")
            .append(resultSet.getTimestamp("initialized"))
            .append("\", \"running\": ")
            .append(resultSet.getInt("running"))
            .append(", \"skipped\": ")
            .append(resultSet.getInt("skipped"))
            .append(", \"successful\": ")
            .append(resultSet.getInt("successful"))
            .append('}');

      responseBuilder =
          Response.ok("{\"sessions\": [" + stringBuilder + "]}", MediaType.APPLICATION_JSON);
    }
    return responseBuilder;
  }
}
