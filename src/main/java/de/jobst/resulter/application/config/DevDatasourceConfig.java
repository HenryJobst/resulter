package de.jobst.resulter.application.config;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

@Configuration
@Profile("dev && !inmem && !testcontainers")
public class DevDatasourceConfig {

    @Bean
    @Primary
    public DataSource dataSource() {
        return DataSourceBuilder.create()
            .url("jdbc:p6spy:postgresql://localhost:5432/resulter")
            .username("resulter")
            .password("resulter")
            .driverClassName("com.p6spy.engine.spy.P6SpyDriver")
            .build();
    }
}
