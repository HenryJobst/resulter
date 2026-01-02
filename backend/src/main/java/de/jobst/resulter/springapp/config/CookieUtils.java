package de.jobst.resulter.springapp.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Utility class for creating cookies with consistent attributes.
 * Ensures all cookies use the same domain, secure, and path settings.
 */
@Slf4j
@Component
public class CookieUtils {

    /**
     * -- GETTER --
     *  Gets the session cookie name from configuration.
     */
    @Getter
    @Value("${server.servlet.session.cookie.name:RESULTER_SESSION}")
    private String sessionCookieName;

    @Value("${server.servlet.session.cookie.same-site:Lax}")
    private @Nullable String sameSiteDefault;

    @Value("${server.servlet.session.cookie.domain:}")
    private @Nullable String cookieDomain;

    @Value("${server.servlet.session.cookie.secure:false}")
    private boolean cookieSecure;

    // CSRF cookie name (Spring Security default)
    private static final String CSRF_COOKIE_NAME = "XSRF-TOKEN";

    /**
     * Creates a cookie with standard attributes (domain, secure, path, SameSite).
     * For domain cookies starting with ".", uses Set-Cookie header to bypass Servlet API validation.
     *
     * @param name      Cookie name
     * @param value     Cookie value
     * @param maxAge    Max age in seconds (0 to delete)
     * @param httpOnly  Whether cookie should be HTTP-only
     * @param sameSite  SameSite attribute (Lax, Strict, None)
     * @param request   Optional HTTP request for domain validation (can be null)
     * @return Configured cookie (maybe null if using header-based approach)
     */
    private @Nullable Cookie createCookie(String name, String value, int maxAge, boolean httpOnly,
                                          @Nullable String sameSite,
                                          @Nullable HttpServletRequest request) {
        // For domain cookies starting with ".", we cannot use Cookie API due to Servlet 6.0 validation
        // Instead, we return null and the caller must use header-based approach
        if (cookieDomain != null && !cookieDomain.isEmpty() && cookieDomain.startsWith(".")) {
            if (request != null) {
                String requestHost = request.getServerName();
                String domainWithoutDot = cookieDomain.substring(1);
                boolean matches = requestHost.endsWith(domainWithoutDot) || requestHost.equals(domainWithoutDot);

                if (!matches) {
                    logCookieWarning(requestHost);
                }
            }
            // Return null to signal caller to use header-based approach
            return null;
        }

        // Standard cookie (no domain or exact domain match)
        Cookie cookie = new Cookie(name, value);

        // Only set domain if configured, not empty, and not starting with "."
        if (cookieDomain != null && !cookieDomain.isEmpty()) {
            if (request != null) {
                String requestHost = request.getServerName();
                boolean matches = requestHost.equals(cookieDomain);

                if (matches) {
                    cookie.setDomain(cookieDomain);
                    log.debug("Cookie '{}' created with domain: {}", name, cookieDomain);
                } else {
                    logCookieWarning(requestHost);
                }
            } else {
                cookie.setDomain(cookieDomain);
            }
        }

        cookie.setSecure(cookieSecure);
        cookie.setPath("/");
        cookie.setHttpOnly(httpOnly);
        cookie.setMaxAge(maxAge);

        // Servlet API 6.0+ supports setAttribute for SameSite
        if (sameSite != null && !sameSite.isEmpty()) {
            cookie.setAttribute("SameSite", sameSite);
        }

        return cookie;
    }

    /**
     * Creates a Set-Cookie header value for cookies with domain starting with ".".
     * Bypasses Servlet API validation by constructing header manually.
     */
    private String buildSetCookieHeader(String name, String value, int maxAge, boolean httpOnly,
                                        @Nullable String sameSite) {
        StringBuilder header = new StringBuilder();

        // Name=Value
        header.append(name).append("=").append(value);

        // Domain (with leading dot)
        if (cookieDomain != null && !cookieDomain.isEmpty()) {
            header.append("; Domain=").append(cookieDomain);
        }

        // Path
        header.append("; Path=/");

        // Max-Age
        if (maxAge >= 0) {
            header.append("; Max-Age=").append(maxAge);
        }

        // Secure
        if (cookieSecure) {
            header.append("; Secure");
        }

        // HttpOnly
        if (httpOnly) {
            header.append("; HttpOnly");
        }

        // SameSite
        if (sameSite != null && !sameSite.isEmpty()) {
            header.append("; SameSite=").append(sameSite);
        }

        return header.toString();
    }

    private void logCookieWarning(String requestHost) {
        log.warn("Cookie domain {} does not match request host {}. Cookie will be set without domain attribute.",
                cookieDomain, requestHost);
    }

    /**
     * Adds a cookie to the response.
     * For domain cookies starting with ".", uses Set-Cookie header to bypass Servlet API validation.
     * Otherwise uses Cookie API.
     *
     * @param response  HTTP response
     * @param name      Cookie name
     * @param value     Cookie value
     * @param maxAge    Max age in seconds (0 to delete)
     * @param httpOnly  Whether cookie should be HTTP-only
     * @param sameSite  SameSite attribute (Lax, Strict, None)
     * @param request   HTTP request for domain validation (optional)
     */
    public void addCookieWithHeader(HttpServletResponse response, String name, String value,
                                     int maxAge, boolean httpOnly,
                                    @Nullable String sameSite,
                                    @Nullable HttpServletRequest request) {
        Cookie cookie = createCookie(name, value, maxAge, httpOnly, sameSite, request);

        if (cookie != null) {
            // Standard Cookie API approach
            response.addCookie(cookie);
            log.debug("Cookie '{}' added via Cookie API", name);
        } else {
            // Header-based approach for domain cookies starting with "."
            String setCookieHeader = buildSetCookieHeader(name, value, maxAge, httpOnly, sameSite);
            response.addHeader("Set-Cookie", setCookieHeader);
            log.debug("Cookie '{}' added via Set-Cookie header: {}", name, setCookieHeader);
        }
    }

    /**
     * Adds a cookie to the response using the Cookie API (without request validation).
     * Uses setAttribute for SameSite (available in Servlet 6.0+).
     *
     * @param response  HTTP response
     * @param name      Cookie name
     * @param value     Cookie value
     * @param maxAge    Max age in seconds (0 to delete)
     * @param httpOnly  Whether cookie should be HTTP-only
     * @param sameSite  SameSite attribute (Lax, Strict, None)
     */
    public void addCookieWithHeader(HttpServletResponse response, String name, String value,
                                     int maxAge, boolean httpOnly, @Nullable String sameSite) {
        addCookieWithHeader(response, name, value, maxAge, httpOnly, sameSite, null);
    }

    /**
     * Deletes a cookie by setting its max age to 0.
     *
     * @param response  HTTP response
     * @param name      Cookie name
     * @param httpOnly  Whether cookie should be HTTP-only (must match original cookie)
     */
    public void deleteCookie(HttpServletResponse response, String name, boolean httpOnly) {
        addCookieWithHeader(response, name, "", 0, httpOnly, sameSiteDefault);
    }

    /**
     * Deletes the session cookie.
     * Uses the cookie name from Spring Boot configuration.
     *
     * @param response HTTP response
     */
    public void deleteSessionCookie(HttpServletResponse response) {
        deleteCookie(response, sessionCookieName, true);
    }

    /**
     * Deletes the CSRF token cookie.
     * Uses Spring Security's default CSRF cookie name.
     *
     * @param response HTTP response
     */
    public void deleteCsrfCookie(HttpServletResponse response) {
        deleteCookie(response, CSRF_COOKIE_NAME, false);
    }

    /**
     * Gets the CSRF cookie name.
     * @return CSRF cookie name
     */
    public String getCsrfCookieName() {
        return CSRF_COOKIE_NAME;
    }
}
