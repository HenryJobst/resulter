package de.jobst.resulter.springapp.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoderFactory;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Configuration
@Profile("!nosecurity")
@Slf4j
public class BffSecurityConfiguration {

    @Value("${spring.security.oauth2.client.provider.keycloak.jwk-set-uri}")
    private String jwkSetUri;

    @Value("${bff.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    private final BffAuthenticationSuccessHandler bffAuthenticationSuccessHandler;
    private final CorsConfigurationSource corsConfigurationSource;

    public BffSecurityConfiguration(
            BffAuthenticationSuccessHandler bffAuthenticationSuccessHandler,
            CorsConfigurationSource corsConfigurationSource) {
        this.bffAuthenticationSuccessHandler = bffAuthenticationSuccessHandler;
        this.corsConfigurationSource = corsConfigurationSource;
    }

    @Bean
    @Order(2) // Between Prometheus (1) and API (3)
    public SecurityFilterChain bffSecurityFilterChain(HttpSecurity http) {
        http.securityMatcher("/bff/**", "/oauth2/**", "/login/**", "/error")
                .authorizeHttpRequests(auth -> auth.requestMatchers(
                                "/bff/logout", "/oauth2/authorization/**", "/login/oauth2/code/**", "/login", "/error")
                        .permitAll()
                        .anyRequest()
                        .authenticated())
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(authorization -> authorization
                                // Use cookie-based storage instead of session to avoid cleanup issues
                                .authorizationRequestRepository(new CookieOAuth2AuthorizationRequestRepository()))
                        .successHandler(bffAuthenticationSuccessHandler)
                        .failureHandler(oauth2AuthenticationFailureHandler()))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            // For /bff/user requests, return 401 instead of redirecting to login
                            if (request.getRequestURI().startsWith("/bff/")) {
                                log.debug("Unauthorized access to {}, returning 401", request.getRequestURI());
                                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                response.setContentType("application/json");
                                response.getWriter().write("{\"error\":\"Unauthorized\"}");
                            } else {
                                // For other requests, redirect to OAuth2 login
                                response.sendRedirect("/oauth2/authorization/keycloak");
                            }
                        }))
                .logout(logout -> logout
                        .logoutRequestMatcher(PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.GET, "/bff/logout"))
                        .logoutSuccessHandler(oidcLogoutSuccessHandler())
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("RESULTER_SESSION", "XSRF-TOKEN"))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false))
                .csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
                .cors(cors -> cors.configurationSource(corsConfigurationSource));

        return http.build();
    }

    private LogoutSuccessHandler oidcLogoutSuccessHandler() {
        return (request, response, authentication) -> {
            String username = authentication != null ? authentication.getName() : "anonymous";
            log.info("Logout successful for user: {}, initiating OIDC logout", username);

            // Session is already invalidated by logout configuration
            if (request.getSession(false) != null) {
                log.warn("Session still exists after logout: {}", request.getSession(false).getId());
            } else {
                log.info("Session successfully invalidated");
            }

            // Explicitly delete cookies - must match ALL attributes of the original cookie
            var sessionCookie = new jakarta.servlet.http.Cookie("RESULTER_SESSION", "");
            sessionCookie.setPath("/");
            sessionCookie.setMaxAge(0);
            sessionCookie.setHttpOnly(true);
            sessionCookie.setSecure(false); // Must match dev setting
            sessionCookie.setDomain("localhost");
            response.addCookie(sessionCookie);

            var csrfCookie = new jakarta.servlet.http.Cookie("XSRF-TOKEN", "");
            csrfCookie.setPath("/");
            csrfCookie.setMaxAge(0);
            csrfCookie.setHttpOnly(false);
            csrfCookie.setSecure(false);
            csrfCookie.setDomain("localhost");
            response.addCookie(csrfCookie);

            // Also add explicit header to delete cookies (fallback method)
            response.addHeader("Set-Cookie",
                "RESULTER_SESSION=; Path=/; Domain=localhost; Max-Age=0; HttpOnly; SameSite=Lax");
            response.addHeader("Set-Cookie",
                "XSRF-TOKEN=; Path=/; Domain=localhost; Max-Age=0; SameSite=Lax");

            log.info("Cookies cleared");

            // OIDC RP-Initiated Logout: Redirect to Keycloak logout endpoint
            // This will clear the Keycloak SSO session and then redirect back to our frontend
            // Format: {issuer-uri}/protocol/openid-connect/logout?id_token_hint={token}&post_logout_redirect_uri={redirect}
            String issuerUri = request.getServletContext().getInitParameter("issuer-uri");
            if (issuerUri == null) {
                // Fallback: construct from known Keycloak URL
                issuerUri = "https://keycloak.jobst24.de/realms/resulter";
            }

            try {
                // Extract ID token from authentication for automatic logout (no confirmation needed)
                String idTokenValue = null;
                if (authentication instanceof org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken oauth2Auth) {
                    var principal = oauth2Auth.getPrincipal();
                    if (principal instanceof org.springframework.security.oauth2.core.oidc.user.OidcUser oidcUser) {
                        idTokenValue = oidcUser.getIdToken().getTokenValue();
                        log.debug("Extracted ID token for logout");
                    }
                }

                StringBuilder logoutUrl = new StringBuilder(issuerUri)
                    .append("/protocol/openid-connect/logout")
                    .append("?post_logout_redirect_uri=")
                    .append(java.net.URLEncoder.encode(frontendUrl, StandardCharsets.UTF_8));

                // Add id_token_hint for automatic logout without confirmation
                if (idTokenValue != null) {
                    logoutUrl.append("&id_token_hint=")
                        .append(java.net.URLEncoder.encode(idTokenValue, StandardCharsets.UTF_8));
                    log.info("Redirecting to Keycloak logout with id_token_hint (auto-logout)");
                } else {
                    log.warn("No ID token found, Keycloak may require manual logout confirmation");
                }

                response.sendRedirect(logoutUrl.toString());
            } catch (java.io.UnsupportedEncodingException e) {
                log.error("Failed to encode logout URL", e);
                // Fallback: just redirect to frontend
                response.sendRedirect(frontendUrl);
            }
        };
    }

    private AuthenticationFailureHandler oauth2AuthenticationFailureHandler() {
        return (HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) -> {
            log.error("OAuth2 authentication failed", exception);
            log.error("Error type: {}", exception.getClass().getName());
            log.error("Error message: {}", exception.getMessage());
            if (exception.getCause() != null) {
                log.error("Caused by: {}", exception.getCause().getClass().getName());
                log.error("Cause message: {}", exception.getCause().getMessage());
            }
            // Redirect to frontend with error parameter
            response.sendRedirect(frontendUrl + "?auth_error=oauth2_failed");
        };
    }

    @Bean
    public JwtDecoderFactory<ClientRegistration> jwtDecoderFactory() {
        return clientRegistration -> {
            // Create a decoder that supports RS256 (Keycloak default) and RS512
            return NimbusJwtDecoder.withJwkSetUri(jwkSetUri)
                    .jwsAlgorithm(SignatureAlgorithm.RS256)
                    .jwsAlgorithm(SignatureAlgorithm.RS512)
                    .build();
        };
    }

    /**
     * Maps OAuth2/OIDC authorities to include Keycloak realm roles.
     * Extracts roles from realm_access.roles claim and adds them as ROLE_* authorities.
     */
    @Bean
    public GrantedAuthoritiesMapper userAuthoritiesMapper() {
        return (authorities) -> {
            Set<GrantedAuthority> mappedAuthorities = new HashSet<>();

            authorities.forEach(authority -> {
                // Keep existing authorities
                mappedAuthorities.add(authority);

                // Extract realm roles from OIDC user
                if (authority instanceof OidcUserAuthority oidcUserAuthority) {
                    var userInfo = oidcUserAuthority.getUserInfo();
                    if (userInfo != null) {
                        Object realmAccess = userInfo.getClaim("realm_access");
                        convertRolesToAuthorities(realmAccess, mappedAuthorities);
                    }
                }
                // Fallback for OAuth2 (non-OIDC) user
                else if (authority instanceof OAuth2UserAuthority oauth2UserAuthority) {
                    var attributes = oauth2UserAuthority.getAttributes();
                    Object realmAccess = attributes.get("realm_access");
                    convertRolesToAuthorities(realmAccess, mappedAuthorities);
                }
            });

            log.debug("Total mapped authorities: {}", mappedAuthorities.size());
            return mappedAuthorities;
        };
    }

    private static void convertRolesToAuthorities(Object realmAccess, Set<GrantedAuthority> mappedAuthorities) {
        if (!(realmAccess instanceof Map<?, ?> realmAccessMap)) {
            return;
        }
        Object rolesList = realmAccessMap.get("roles");
        if (!(rolesList instanceof List<?> roles)) {
            return;
        }
        roles.stream()
            .filter(role -> role instanceof String)
            .map(role -> (String) role)
            .forEach(roleStr -> {
                mappedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + roleStr.toUpperCase()));
                log.debug("Mapped Keycloak role to authority: ROLE_{}", roleStr.toUpperCase());
            });
    }
}
