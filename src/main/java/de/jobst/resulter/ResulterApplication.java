package de.jobst.resulter;

import liquibase.changelog.ChangeLogHistoryServiceFactory;
import org.springframework.aot.hint.ExecutableMode;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

import java.util.Collections;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

@SpringBootApplication
@EnableJdbcRepositories
@ImportRuntimeHints(ResulterApplication.ResulterApplicationRuntimeHints.class)
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
public class ResulterApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(ResulterApplication.class);
        app.addInitializers(new DotenvInitializer());
        app.run(args);
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

