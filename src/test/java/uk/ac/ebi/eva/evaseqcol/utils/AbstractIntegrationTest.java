package uk.ac.ebi.eva.evaseqcol.utils;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class AbstractIntegrationTest {
    private static final String POSTGRES_IMAGE = "postgres:11";

    public static final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>(POSTGRES_IMAGE);

    static {
        postgreSQLContainer.start();
        createEvaSchema();
    }

    @DynamicPropertySource
    static void dataSourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    }

    // The connection pool is configured (spring.datasource.hikari.schema, in application.properties)
    // It needs to be created up front, as Postgres won't implicitly create it for ddl-auto=update.
    private static void createEvaSchema() {
        try (Connection connection = DriverManager.getConnection(
                postgreSQLContainer.getJdbcUrl(), postgreSQLContainer.getUsername(), postgreSQLContainer.getPassword());
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE SCHEMA IF NOT EXISTS eva");
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to create eva schema for integration test", e);
        }
    }
}
