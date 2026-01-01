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
    private String sameSiteDefault;

    @Value("${server.servlet.session.cookie.domain:}")
    private @Nullable String cookieDomain;

    @Value("${server.servlet.session.cookie.secure:false}")
    private boolean cookieSecure;

    // CSRF cookie name (Spring Security default)
    private static final String CSRF_COOKIE_NAME = "XSRF-TOKEN";

    /**
     * Creates a cookie with standard attributes (domain, secure, path, SameSite).
     *
     * @param name      Cookie name
     * @param value     Cookie value
     * @param maxAge    Max age in seconds (0 to delete)
     * @param httpOnly  Whether cookie should be HTTP-only
     * @param sameSite  SameSite attribute (Lax, Strict, None)
     * @param request   Optional HTTP request for domain validation (can be null)
     * @return Configured cookie
     */
    public Cookie createCookie(String name, String value, int maxAge, boolean httpOnly, String sameSite,
                               @Nullable HttpServletRequest request) {
        Cookie cookie = new Cookie(name, value);

        // Only set domain if configured and not empty
        // Domain validation: If domain is set, validate it against request host to avoid Tomcat RFC 6265 validation errors
        if (cookieDomain != null && !cookieDomain.isEmpty()) {
            if (request != null) {
                String requestHost = request.getServerName();
                // Check if domain is valid for this request
                // For domain cookies (starting with .), the request host must end with the domain
                if (cookieDomain.startsWith(".")) {
                    if (requestHost.endsWith(cookieDomain.substring(1)) || requestHost.equals(cookieDomain.substring(1))) {
                        cookie.setDomain(cookieDomain);
                    } else {
                        logCookieWarning(requestHost);
                    }
                } else {
                    // Exact domain match required
                    if (requestHost.equals(cookieDomain)) {
                        cookie.setDomain(cookieDomain);
                    } else {
                        logCookieWarning(requestHost);
                    }
                }
            } else {
                // No request available, set domain anyway and let Tomcat validate
                cookie.setDomain(cookieDomain);
            }
        }

        cookie.setSecure(cookieSecure);
        cookie.setPath("/");
        cookie.setHttpOnly(httpOnly);
        cookie.setMaxAge(maxAge);

        // Servlet API 6.0+ supports setAttribute for SameSite
        if (!sameSite.isEmpty()) {
            cookie.setAttribute("SameSite", sameSite);
        }

        return cookie;
    }

    private void logCookieWarning(String requestHost) {
        log.warn("Cookie domain {} does not match request host {}. Cookie will be set without domain attribute.",
                cookieDomain, requestHost);
    }

    /**
     * Adds a cookie to the response using the Cookie API.
     * Uses setAttribute for SameSite (available in Servlet 6.0+).
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
                                     int maxAge, boolean httpOnly, String sameSite,
                                    @Nullable HttpServletRequest request) {
        Cookie cookie = createCookie(name, value, maxAge, httpOnly, sameSite, request);
        response.addCookie(cookie);
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
                                     int maxAge, boolean httpOnly, String sameSite) {
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
