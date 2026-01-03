package de.jobst.resulter.springapp.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

/**
 * Spring Session configuration.
 * Configures session cookie attributes (SameSite, Secure, etc.).
 */
@Configuration
@Slf4j
public class SessionConfiguration {

    @Value("${server.servlet.session.cookie.name:RESULTER_SESSION}")
    private String sessionCookieName;

    @Value("${server.servlet.session.cookie.domain:}")
    private String cookieDomain;

    @Value("${server.servlet.session.cookie.secure:false}")
    private boolean cookieSecure;

    @Value("${server.servlet.session.cookie.same-site:Lax}")
    private String cookieSameSite;

    @Value("${server.servlet.session.cookie.path:/}")
    private String cookiePath;

    /**
     * Custom CookieSerializer for Spring Session.
     * Ensures session cookies are properly configured for same-domain architecture.
     */
    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();

        serializer.setCookieName(sessionCookieName);
        serializer.setCookiePath(cookiePath);
        serializer.setUseSecureCookie(cookieSecure);
        serializer.setSameSite(cookieSameSite);

        // Only set domain if configured and not empty
        if (cookieDomain != null && !cookieDomain.isEmpty()) {
            serializer.setDomainName(cookieDomain);
            log.info("Spring Session cookie configured with domain: {}", cookieDomain);
        } else {
            log.info("Spring Session cookie configured without domain (request host will be used)");
        }

        log.info("Spring Session cookie configuration: name={}, path={}, secure={}, sameSite={}",
                 sessionCookieName, cookiePath, cookieSecure, cookieSameSite);

        return serializer;
    }
}
