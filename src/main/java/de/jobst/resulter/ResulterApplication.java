package de.jobst.resulter;

import liquibase.changelog.ChangeLogHistoryServiceFactory;
import org.springframework.aot.hint.ExecutableMode;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;

import java.util.Collections;

@SpringBootApplication
@EnableJdbcRepositories
@ImportRuntimeHints(ResulterApplication.ResulterApplicationRuntimeHints.class)
public class ResulterApplication {

    public static void main(String[] args) {
        SpringApplication.run(ResulterApplication.class, args);
    }

    static class ResulterApplicationRuntimeHints implements RuntimeHintsRegistrar {

        // see: https://github.com/oracle/graalvm-reachability-metadata/issues/431
        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            hints.reflection()
                .registerType(ChangeLogHistoryServiceFactory.class,
                    (type) -> type.withConstructor(Collections.emptyList(), ExecutableMode.INVOKE));
        }
    }

}

