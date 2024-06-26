package de.jobst.resulter.application.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;

@Configuration
@Profile("testcontainers")
public class TestContainersConfig {

    private static final PostgreSQLContainer<?> postgresContainer =
        new PostgreSQLContainer<>(DockerImageName.parse("postgres:latest")).withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    static {
        postgresContainer.start();
    }

    @Bean
    @Primary
    public DataSource dataSource() {
        return DataSourceBuilder.create()
            .url(postgresContainer.getJdbcUrl())
            .username(postgresContainer.getUsername())
            .password(postgresContainer.getPassword())
            .driverClassName(postgresContainer.getDriverClassName())
            .build();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void createSnapshotAfterLiquibase() {
        createDatabaseSnapshot();
    }

    private void createDatabaseSnapshot() {
        executeCommandInContainer("pg_dump -U test -d testdb -F p -f /tmp/snapshot.sql");
    }

    public static void restoreDatabaseSnapshot() {
        executeCommandInContainer("psql -U test -d testdb -f /tmp/snapshot.sql");
    }

    private static void executeCommandInContainer(String command) {
        try {
            String[] commandArray = {"sh", "-c", command};
            Container.ExecResult result = postgresContainer.execInContainer(commandArray);

            if (result.getExitCode() != 0) {
                throw new RuntimeException(
                    "Command execution failed with exit code " + result.getExitCode() + ": " + result.getStderr());
            }

            System.out.println(result.getStdout());
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute command in container", e);
        }
    }
}

