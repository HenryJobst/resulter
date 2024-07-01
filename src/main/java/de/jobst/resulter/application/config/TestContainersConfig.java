package de.jobst.resulter.application.config;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
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
}

