package de.jobst.resulter.springapp.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.sql.DataSource;
import java.util.Arrays;

@Component
@Profile("testcontainers")
public class DataSourceInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(DataSourceInterceptor.class);

    private final DataSourceManager dataSourceManager;

    public DataSourceInterceptor(DataSourceManager dataSourceManager) {
        this.dataSourceManager = dataSourceManager;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) {
        log.info("[DataSourceInterceptor] Request: {} {}", request.getMethod(), request.getRequestURI());

        // First, try to get database identifier from HTTP header (preferred for API calls)
        String dbIdentifier = request.getHeader("X-DB-Identifier");
        log.info("[DataSourceInterceptor] X-DB-Identifier header: {}", dbIdentifier);

        // If not found in header, try to get it from cookie (used by E2E tests to avoid OAuth2 interference)
        if (dbIdentifier == null) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                dbIdentifier = Arrays.stream(cookies)
                    .filter(cookie -> "X-DB-Identifier".equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
                log.info("[DataSourceInterceptor] X-DB-Identifier from cookie: {}", dbIdentifier);
            }
        }

        // If database identifier is found (via header or cookie), route to that database
        if (dbIdentifier != null) {
            log.info("[DataSourceInterceptor] Routing to database: {}", dbIdentifier);
            DataSource dataSource = dataSourceManager.getDataSource(dbIdentifier);
            if (dataSource != null) {
                log.info("[DataSourceInterceptor] DataSource found and set for: {}", dbIdentifier);
                DataSourceContextHolder.setDataSource(dataSource);
            } else {
                log.warn("[DataSourceInterceptor] DataSource NOT FOUND for: {}", dbIdentifier);
            }
        } else {
            log.info("[DataSourceInterceptor] No database identifier found, using default database");
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

