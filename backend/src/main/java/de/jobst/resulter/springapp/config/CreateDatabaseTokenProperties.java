package de.jobst.resulter.springapp.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Setter
@Getter
@Configuration
@Profile("testcontainers")
@ConfigurationProperties(prefix = "security.createdatabase")
public class CreateDatabaseTokenProperties {
    private String apiToken;
}
