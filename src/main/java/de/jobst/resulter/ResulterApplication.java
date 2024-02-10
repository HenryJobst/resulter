package de.jobst.resulter;

import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;

@SpringBootApplication
@EnableJdbcRepositories
@ImportRuntimeHints(ResulterApplication.LiquibaseRuntimeHints.class)
public class ResulterApplication {

    public static void main(String[] args) {
        SpringApplication.run(ResulterApplication.class, args);
    }

    static class LiquibaseRuntimeHints implements RuntimeHintsRegistrar {

        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            hints.resources().registerPattern("db/changelog/db.changelog-master.yaml");
            hints.resources().registerPattern("db/jdbc_migrations/*.yaml");
            hints.resources().registerPattern("db/initial_sql_data/*.sql");
        }
    }
}

