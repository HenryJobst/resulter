package de.jobst.resulter.springapp.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Utility class for creating cookies with consistent attributes.
 * Ensures all cookies use the same domain, secure, and path settings.
 */
@Component
public class CookieUtils {

    // Read cookie configuration from Spring Boot properties (Single Source of Truth)
    @Value("${server.servlet.session.cookie.name:RESULTER_SESSION}")
    private String sessionCookieName;

    @Value("${server.servlet.session.cookie.same-site:Lax}")
    private String sameSiteDefault;

    @Value("${server.servlet.session.cookie.domain:localhost}")
    private String cookieDomain;

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
     * @return Configured cookie
     */
    public Cookie createCookie(String name, String value, int maxAge, boolean httpOnly, String sameSite) {
        Cookie cookie = new Cookie(name, value);
        cookie.setDomain(cookieDomain);
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
     */
    public void addCookieWithHeader(HttpServletResponse response, String name, String value,
                                     int maxAge, boolean httpOnly, String sameSite) {
        Cookie cookie = createCookie(name, value, maxAge, httpOnly, sameSite);
        response.addCookie(cookie);
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
     * Gets the session cookie name from configuration.
     * @return Session cookie name
     */
    public String getSessionCookieName() {
        return sessionCookieName;
    }

    /**
     * Gets the CSRF cookie name.
     * @return CSRF cookie name
     */
    public String getCsrfCookieName() {
        return CSRF_COOKIE_NAME;
    }
}
