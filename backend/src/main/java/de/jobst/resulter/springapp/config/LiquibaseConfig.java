package de.jobst.resulter.springapp.config;

import liquibase.integration.spring.SpringLiquibase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ResourceLoader;

import javax.sql.DataSource;

@Configuration
@Profile("testcontainers")
@Slf4j
public class LiquibaseConfig {

    private final ResourceLoader resourceLoader;

    public LiquibaseConfig(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public void runLiquibaseForDataSource(DataSource dataSource) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog("classpath:db/changelog/db.changelog-master.yaml");
        liquibase.setResourceLoader(resourceLoader);
        try {
            liquibase.afterPropertiesSet();
        } catch (Exception e) {
            log.error("Error while running Liquibase", e);
        }
    }
}

