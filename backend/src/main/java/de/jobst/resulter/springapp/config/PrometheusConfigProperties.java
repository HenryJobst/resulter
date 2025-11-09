package de.jobst.resulter.springapp.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "security.prometheus")
public class PrometheusConfigProperties {
    private String apiToken;
}
