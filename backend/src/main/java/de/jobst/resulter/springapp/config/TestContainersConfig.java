package de.jobst.resulter.springapp.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;

@Configuration
@Profile("e2e-frontend-tests")
public class TestContainersConfig {

    private static final PostgreSQLContainer postgresContainer =
        new PostgreSQLContainer(DockerImageName.parse("postgres:latest"))
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    static {
        postgresContainer.start();
    }

    /**
     * Creates the default (main) DataSource from testcontainer.
     * This is used when no X-DB-Identifier is provided (normal operation).
     */
    @Bean
    @Qualifier("defaultDataSource")
    public DataSource defaultDataSource() {
        return DataSourceBuilder.create()
            .url(postgresContainer.getJdbcUrl())
            .username(postgresContainer.getUsername())
            .password(postgresContainer.getPassword())
            .driverClassName(postgresContainer.getDriverClassName())
            .build();
    }

    /**
     * Creates the primary DataSource bean with dynamic routing capability.
     * This routes to different databases based on X-DB-Identifier header/cookie.
     * <p>
     * &#064;Primary  ensures Spring uses this DataSource for all database operations.
     */
    @Bean
    @Primary
    public DataSource dataSource(@Qualifier("defaultDataSource") DataSource defaultDataSource) {
        return new DynamicRoutingDataSource(defaultDataSource);
    }
}

