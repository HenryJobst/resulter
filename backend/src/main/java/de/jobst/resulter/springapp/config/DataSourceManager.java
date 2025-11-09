package de.jobst.resulter.springapp.config;

import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

@Component
@Profile("testcontainers")
public class DataSourceManager {

    private final Map<String, ManagedDataSource> dataSources = new HashMap<>();
    private final Duration defaultTimeout = Duration.ofSeconds(30);
    private final LiquibaseConfig liquibaseConfig;

    public DataSourceManager(LiquibaseConfig liquibaseConfig) {this.liquibaseConfig = liquibaseConfig;}

    public String createNewDatabase(Duration timeout) {
        String identifier = UUID.randomUUID().toString();

        @SuppressWarnings("resource") PostgreSQLContainer<?> postgresqlContainer =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:latest")).withDatabaseName("testdb_" + identifier)
                .withUsername("test")
                .withPassword("test");
        postgresqlContainer.start();

        String url = postgresqlContainer.getJdbcUrl();

        DataSource dataSource = DataSourceBuilder.create()
            .url(url)
            .username(postgresqlContainer.getUsername())
            .password(postgresqlContainer.getPassword())
            .driverClassName(postgresqlContainer.getDriverClassName())
            .build();

        dataSources.put(identifier,
            new ManagedDataSource(dataSource, postgresqlContainer, timeout != null ? timeout : defaultTimeout));

        // Run Liquibase
        liquibaseConfig.runLiquibaseForDataSource(dataSource);

        return identifier;
    }

    public DataSource getDataSource(String identifier) {
        ManagedDataSource managedDataSource = dataSources.get(identifier);
        if (managedDataSource != null) {
            managedDataSource.updateLastAccessTime();
            return managedDataSource.getDataSource();
        }
        return null;
    }

    public void checkForInactiveDataSources() {
        Iterator<Map.Entry<String, ManagedDataSource>> iterator = dataSources.entrySet().iterator();
        LocalDateTime now = LocalDateTime.now();

        while (iterator.hasNext()) {
            Map.Entry<String, ManagedDataSource> entry = iterator.next();
            ManagedDataSource managedDataSource = entry.getValue();

            if (Duration.between(managedDataSource.getLastAccessTime(), now).compareTo(managedDataSource.getTimeout()) >
                0) {
                managedDataSource.getContainer().stop();
                iterator.remove();
            }
        }
    }

    @PreDestroy
    public void cleanUp() {
        for (ManagedDataSource managedDataSource : dataSources.values()) {
            managedDataSource.getContainer().stop();
        }
    }

    @Getter
    public static class ManagedDataSource {

        private final DataSource dataSource;
        private final PostgreSQLContainer<?> container;
        private LocalDateTime lastAccessTime;
        @Setter
        private Duration timeout;

        public ManagedDataSource(DataSource dataSource, PostgreSQLContainer<?> container, Duration timeout) {
            this.dataSource = dataSource;
            this.container = container;
            this.timeout = timeout;
            this.lastAccessTime = LocalDateTime.now();
        }

        public void updateLastAccessTime() {
            this.lastAccessTime = LocalDateTime.now();
        }
    }
}

