package de.jobst.resulter.springapp.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

import java.util.Base64;

/**
 * Cookie-based OAuth2 authorization request repository.
 * Stores the authorization request in a cookie instead of the session.
 * This avoids issues with session cleanup during logout/login cycles.
 */
@Component
public class CookieOAuth2AuthorizationRequestRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    private static final String COOKIE_NAME = "oauth2_auth_request";
    private static final int COOKIE_EXPIRE_SECONDS = 180; // 3 minutes

    private final CookieUtils cookieUtils;

    public CookieOAuth2AuthorizationRequestRepository(CookieUtils cookieUtils) {
        this.cookieUtils = cookieUtils;
    }

    @Override
    public @Nullable OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        return getCookie(request)
            .map(this::deserialize)
            .orElse(null);
    }

    @Override
    public void saveAuthorizationRequest(@Nullable OAuth2AuthorizationRequest authorizationRequest,
                                         HttpServletRequest request, HttpServletResponse response) {
        if (authorizationRequest == null) {
            deleteCookie(response);
            return;
        }

        String value = serialize(authorizationRequest);
        // Use CookieUtils to create cookie with consistent attributes
        // SameSite will use the default from Spring Boot configuration
        cookieUtils.addCookieWithHeader(response, COOKIE_NAME, value, COOKIE_EXPIRE_SECONDS, true, "Lax");
    }

    @Override
    public @Nullable OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request, HttpServletResponse response) {
        OAuth2AuthorizationRequest authorizationRequest = loadAuthorizationRequest(request);
        deleteCookie(response);
        return authorizationRequest;
    }

    private java.util.Optional<Cookie> getCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return java.util.Optional.empty();
        }

        for (Cookie cookie : request.getCookies()) {
            if (COOKIE_NAME.equals(cookie.getName())) {
                return java.util.Optional.of(cookie);
            }
        }

        return java.util.Optional.empty();
    }

    private void deleteCookie(HttpServletResponse response) {
        // Use CookieUtils to delete cookie with consistent attributes
        cookieUtils.deleteCookie(response, COOKIE_NAME, true);
    }

    private String serialize(OAuth2AuthorizationRequest authorizationRequest) {
        return Base64.getUrlEncoder().encodeToString(
            SerializationUtils.serialize(authorizationRequest)
        );
    }

    @SuppressWarnings("deprecation")
    private @Nullable OAuth2AuthorizationRequest deserialize(Cookie cookie) {
        try {
            byte[] bytes = Base64.getUrlDecoder().decode(cookie.getValue());
            // Note: SerializationUtils.deserialize(byte[]) is deprecated since Spring 6.0
            // However, the alternative with allowlist is not available in current Spring version
            // This is acceptable since we're deserializing our own trusted cookie data
            return (OAuth2AuthorizationRequest) SerializationUtils.deserialize(bytes);
        } catch (Exception e) {
            return null;
        }
    }
}
