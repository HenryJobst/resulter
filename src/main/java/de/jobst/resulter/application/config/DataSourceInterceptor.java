package de.jobst.resulter.application.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.sql.DataSource;

@Component
@Profile("testcontainers")
public class DataSourceInterceptor implements HandlerInterceptor {

    private final DataSourceManager dataSourceManager;

    public DataSourceInterceptor(DataSourceManager dataSourceManager) {
        this.dataSourceManager = dataSourceManager;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) {
        String dbIdentifier = request.getHeader("X-DB-Identifier");
        if (dbIdentifier != null) {
            DataSource dataSource = dataSourceManager.getDataSource(dbIdentifier);
            if (dataSource != null) {
                DataSourceContextHolder.setDataSource(dataSource);
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request,
                                @NonNull HttpServletResponse response,
                                @NonNull Object handler,
                                Exception ex) {
        DataSourceContextHolder.clearDataSource();
    }
}

