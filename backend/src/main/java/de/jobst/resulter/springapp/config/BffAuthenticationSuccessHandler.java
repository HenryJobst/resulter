package de.jobst.resulter.springapp.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Custom OAuth2 authentication success handler for BFF pattern.
 * Redirects to the frontend application after successful OAuth2 login.
 * The frontend will then call /bff/user to retrieve user information.
 */
@Component
public class BffAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(BffAuthenticationSuccessHandler.class);

    @Value("${bff.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(
        HttpServletRequest request,
        @NonNull HttpServletResponse response,
        Authentication authentication) throws IOException {

        log.info("OAuth2 authentication successful for user: {}", authentication.getName());

        // Session management (including cleaning up old sessions) is handled by Spring Security
        // via maximumSessions(1) configuration - do NOT manually invalidate here!
        var session = request.getSession(true);
        log.info("Session created/retrieved: ID={}, isNew={}", session.getId(), session.isNew());

        // Log authentication details
        log.debug("Authentication: {}", authentication.getClass().getName());
        log.debug("Principal: {}",
            authentication.getPrincipal() != null ?
            authentication.getPrincipal().getClass().getName() : "?");

        // Log cookies in request
        if (request.getCookies() != null) {
            log.info("Cookies in request: {}", request.getCookies().length);
            for (var cookie : request.getCookies()) {
                log.info("Request cookie: name={}, domain={}, path={}, secure={}, httpOnly={}, maxAge={}, value={}",
                    cookie.getName(),
                    cookie.getDomain(),
                    cookie.getPath(),
                    cookie.getSecure(),
                    cookie.isHttpOnly(),
                    cookie.getMaxAge(),
                    cookie.getValue().substring(0, Math.min(20, cookie.getValue().length())) + "...");
            }
        } else {
            log.warn("No cookies in request!");
        }

        // Log session cookie that will be sent
        var sessionCookie = request.getSession(false);
        if (sessionCookie != null) {
            log.info("Session will be sent with cookie. Session ID: {}", sessionCookie.getId());
        }

        log.info("Redirecting to frontend: {} (this redirect should carry the session cookie)", frontendUrl);

        // Redirect to frontend base URL
        // Frontend will:
        // 1. Call /bff/user to get user info (via initAuth())
        // 2. Use sessionStorage 'bff_post_login_redirect' to navigate to the original page
        response.sendRedirect(frontendUrl);
    }
}
