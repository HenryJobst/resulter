package de.jobst.resulter.application.config;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

@Configuration
@Profile("inmem")
public class InmemDatasourceConfig {

    @Bean
    @Primary
    public DataSource dataSource() {
        return DataSourceBuilder.create()
            .url("jdbc:p6spy:h2:mem:testdb;DATABASE_TO_UPPER=FALSE")
            .username("test")
            .password("test")
            .driverClassName("com.p6spy.engine.spy.P6SpyDriver")
            .build();
    }
}
