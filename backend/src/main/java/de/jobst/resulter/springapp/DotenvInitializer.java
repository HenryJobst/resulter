package de.jobst.resulter.springapp;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.lang.NonNull;

@SuppressWarnings("unused")
@Slf4j
public class DotenvInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(@NonNull ConfigurableApplicationContext context) {
        if (isDevProfileActive(context)) {
            Dotenv dotenv = Dotenv.load();
            dotenv.entries().forEach(entry -> {
                System.setProperty(entry.getKey(), entry.getValue());  // In System-Properties setzen
            });
            log.info("Dotenv-Variablen geladen!");
        }
    }

    private boolean isDevProfileActive(ConfigurableApplicationContext context) {
        String[] activeProfiles = context.getEnvironment().getActiveProfiles();
        for (String profile : activeProfiles) {
            if ("dev".equals(profile)) {
                return true;
            }
        }
        return false;
    }
}
