package io.github.henryssondaniel.teacup.service.report.mysql;

import static io.github.henryssondaniel.teacup.service.report.mysql.Utils.createMySqlDataSource;

import io.github.henryssondaniel.teacup.service.report.mysql.v1._0.SessionResource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * REST application. This is the starting point for the REST server. All the resources will have the
 * /api/ in front of the path.
 *
 * @since 1.0
 */
@ApplicationPath("api")
public class RestApplication extends Application {
  private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS ";
  private static final String EXECUTION_FK =
      " FOREIGN KEY (`execution`) REFERENCES `teacup_report`.`execution` (`id`)";
  private static final String EXECUTION_INT = "`execution` INT UNSIGNED NOT NULL,";
  private static final String ID = "`id` INT UNSIGNED NOT NULL AUTO_INCREMENT,";
  private static final Logger LOGGER = Logger.getLogger(RestApplication.class.getName());
  private static final String NO_ACTION = " ON DELETE NO ACTION ON UPDATE NO ACTION";
  private static final String PRIMARY_KEY = "PRIMARY KEY (`id`),";
  private static final String SESSION_EXECUTION_FK =
      " FOREIGN KEY (`session_execution`) REFERENCES `teacup_report`.`session_execution` (`id`)";
  private static final String UNIQUE_INDEX_EXECUTION =
      "UNIQUE INDEX `execution_UNIQUE` (`execution` ASC) VISIBLE,";
  private static final String UNIQUE_INDEX_ID = "UNIQUE INDEX `id_UNIQUE` (`id` ASC) VISIBLE";

  private final DataSource dataSource;

  /**
   * Constructor.
   *
   * @since 1.0
   */
  public RestApplication() {
    this(createMySqlDataSource());
  }

  RestApplication(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  public Set<Class<?>> getClasses() {
    LOGGER.log(Level.FINE, "Get classes");

    initialize();

    return new HashSet<>(Collections.singleton(SessionResource.class));
  }

  private static void createExecution(Connection connection) throws SQLException {
    try (var statement = connection.createStatement()) {
      statement.execute(
          CREATE_TABLE
              + "`teacup_report`.`execution` ("
              + ID
              + "  `node` INT UNSIGNED NOT NULL,"
              + "  `session_execution` INT UNSIGNED NOT NULL,"
              + PRIMARY_KEY
              + UNIQUE_INDEX_ID
              + ','
              + "  INDEX `execution.nod_idx` (`node` ASC) VISIBLE,"
              + "  INDEX `execution.session_execution_idx` (`session_execution` ASC) VISIBLE,"
              + "  CONSTRAINT `execution.node`"
              + "    FOREIGN KEY (`node`)"
              + "    REFERENCES `teacup_report`.`node` (`id`)"
              + NO_ACTION
              + ','
              + "  CONSTRAINT `execution.session_execution`"
              + SESSION_EXECUTION_FK
              + NO_ACTION
              + ");");
    }
  }

  private static void createNode(Connection connection) throws SQLException {
    try (var statement = connection.createStatement()) {
      statement.execute(
          CREATE_TABLE
              + "`teacup_report`.`node` ("
              + ID
              + "  `name` VARCHAR(255) NOT NULL,"
              + PRIMARY_KEY
              + UNIQUE_INDEX_ID
              + ','
              + "  UNIQUE INDEX `name_UNIQUE` (`name` ASC) VISIBLE);");
    }
  }

  private static void createResult(Connection connection) throws SQLException {
    try (var statement = connection.createStatement()) {
      statement.execute(
          CREATE_TABLE
              + "`teacup_report`.`result` ("
              + EXECUTION_INT
              + "  `finished` TIMESTAMP(3) NULL,"
              + ID
              + "  `started` TIMESTAMP(3) NULL,"
              + "  `status` ENUM('aborted', 'failed', 'successful') NULL,"
              + PRIMARY_KEY
              + UNIQUE_INDEX_ID
              + ','
              + UNIQUE_INDEX_EXECUTION
              + "  CONSTRAINT `result.execution`"
              + EXECUTION_FK
              + NO_ACTION
              + ");");
    }
  }

  private static void createSchema(Connection connection) throws SQLException {
    try (var statement = connection.createStatement()) {
      statement.execute("CREATE SCHEMA IF NOT EXISTS teacup_report");
    }
  }

  private static void createSessionExecution(Connection connection) throws SQLException {
    try (var statement = connection.createStatement()) {
      statement.execute(
          CREATE_TABLE
              + "`teacup_report`.`session_execution` ("
              + ID
              + "  `initialized` TIMESTAMP(3) NOT NULL DEFAULT NOW(3),"
              + "  `terminated_time` TIMESTAMP(3) NULL,"
              + PRIMARY_KEY
              + UNIQUE_INDEX_ID
              + ");");
    }
  }

  private static void createSkipped(Connection connection) throws SQLException {
    try (var statement = connection.createStatement()) {
      statement.execute(
          CREATE_TABLE
              + "`teacup_report`.`skipped` ("
              + EXECUTION_INT
              + ID
              + PRIMARY_KEY
              + UNIQUE_INDEX_ID
              + ','
              + UNIQUE_INDEX_EXECUTION
              + "  CONSTRAINT `skipped.execution`"
              + EXECUTION_FK
              + NO_ACTION
              + ");");
    }
  }

  private void initialize() {
    try (var connection = dataSource.getConnection()) {
      createSchema(connection);
      createNode(connection);
      createSessionExecution(connection);
      createExecution(connection);
      createSkipped(connection);
      createResult(connection);
    } catch (SQLException e) {
      LOGGER.log(Level.SEVERE, "Could not initialize the database", e);
    }
  }
}
