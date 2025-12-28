package de.jobst.resulter.springapp.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.Setter;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@DependsOnDatabaseInitialization
@Profile("e2e-frontend-tests")
public class DataSourceManager {

    private final Map<String, ManagedDataSource> dataSources = new ConcurrentHashMap<>();
    private final Duration defaultTimeout = Duration.ofSeconds(90);
    private final LiquibaseConfig liquibaseConfig;

    public DataSourceManager(LiquibaseConfig liquibaseConfig) {this.liquibaseConfig = liquibaseConfig;}

    public String createNewDatabase(Duration timeout) {
        String identifier = UUID.randomUUID().toString();

        PostgreSQLContainer postgresqlContainer =
            new PostgreSQLContainer(DockerImageName.parse("postgres:latest"))
                .withDatabaseName("testdb_" + identifier)
                .withUsername("test")
                .withPassword("test");

        postgresqlContainer.start();

        DataSource dataSource = getDataSource(postgresqlContainer, identifier);

        DataSource loggingDataSource =
            ProxyDataSourceBuilder
                .create(dataSource)
                .name(getDataSourceName(identifier))
                .logQueryBySlf4j()
                .build();

        dataSources.put(
            identifier,
            new ManagedDataSource(
                loggingDataSource,
                postgresqlContainer,
                timeout != null ? timeout : defaultTimeout
            ));

        // Liquibase explizit auf dieser DataSource
        liquibaseConfig.runLiquibaseForDataSource(loggingDataSource);

        return identifier;
    }

    private static @NonNull DataSource getDataSource(PostgreSQLContainer postgresqlContainer, String identifier) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(postgresqlContainer.getJdbcUrl());
        hikariConfig.setUsername(postgresqlContainer.getUsername());
        hikariConfig.setPassword(postgresqlContainer.getPassword());
        hikariConfig.setDriverClassName(postgresqlContainer.getDriverClassName());

        hikariConfig.setKeepaliveTime(0);
        hikariConfig.setMaximumPoolSize(1);
        hikariConfig.setMinimumIdle(0);
        hikariConfig.setIdleTimeout(10_000);
        hikariConfig.setMaxLifetime(30_000);

        hikariConfig.setPoolName(getDataSourceName(identifier));

        return new HikariDataSource(hikariConfig);
    }

    private static @NonNull String getDataSourceName(String identifier) {
        return "test-db-" + identifier;
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
        private final PostgreSQLContainer container;
        private LocalDateTime lastAccessTime;
        @Setter
        private Duration timeout;

        public ManagedDataSource(DataSource dataSource, PostgreSQLContainer container, Duration timeout) {
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

