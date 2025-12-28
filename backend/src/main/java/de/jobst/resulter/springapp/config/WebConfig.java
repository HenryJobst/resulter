package de.jobst.resulter.springapp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Profile("e2e-frontend-tests")
public class WebConfig implements WebMvcConfigurer {

    private final DataSourceInterceptor dataSourceInterceptor;

    public WebConfig(DataSourceInterceptor dataSourceInterceptor) {
        this.dataSourceInterceptor = dataSourceInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(dataSourceInterceptor)
            .addPathPatterns("/**"); // optional, um nur bestimmte Pfade zu intercepten
    }
}
