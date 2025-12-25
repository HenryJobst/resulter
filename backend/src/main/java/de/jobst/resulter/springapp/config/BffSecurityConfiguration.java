package de.jobst.resulter.springapp.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
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

@Configuration
@Profile("!nosecurity")
@Slf4j
public class BffSecurityConfiguration {

    @Value("${spring.security.oauth2.client.provider.keycloak.jwk-set-uri}")
    private String jwkSetUri;

    @Bean
    @Order(2) // Between Prometheus (1) and API (3)
    public SecurityFilterChain bffSecurityFilterChain(HttpSecurity http) {
        http.securityMatcher("/bff/**", "/oauth2/**", "/login/**", "/error")
                .authorizeHttpRequests(auth -> auth.requestMatchers(
                                "/bff/logout", "/oauth2/authorization/**", "/login/oauth2/code/**", "/login", "/error")
                        .permitAll()
                        .anyRequest()
                        .authenticated())
                .oauth2Login(oauth2 -> oauth2.defaultSuccessUrl("/bff/user", true)
                        .failureHandler(oauth2AuthenticationFailureHandler()))
                .logout(logout -> logout.logoutUrl("/bff/logout")
                        .logoutSuccessHandler(oidcLogoutSuccessHandler())
                        .invalidateHttpSession(true)
                        .deleteCookies("RESULTER_SESSION"))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false))
                .csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
                .cors(AbstractHttpConfigurer::disable); // Use global CORS from main chain

        return http.build();
    }

    private LogoutSuccessHandler oidcLogoutSuccessHandler() {
        return (request, response, authentication) -> {
            // Return 200 OK with empty body for SPA logout
            response.setStatus(200);
            response.getWriter().flush();
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
            response.sendRedirect("/login?error");
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
}
