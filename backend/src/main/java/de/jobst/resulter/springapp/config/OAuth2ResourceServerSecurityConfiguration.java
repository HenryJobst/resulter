package de.jobst.resulter.springapp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.health.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.micrometer.metrics.autoconfigure.export.prometheus.PrometheusScrapeEndpoint;
import org.springframework.boot.security.autoconfigure.actuate.web.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.Instant;
import java.util.List;

@Configuration
@EnableWebSecurity
@Profile("!nosecurity")
public class OAuth2ResourceServerSecurityConfiguration {

    public static final long CORS_PREFLIGHT_CACHE_MAX_AGE = 3600L;
    public static final String ADMIN = "ADMIN";
    public static final String ENDPOINT_ADMIN = "ENDPOINT_ADMIN";
    private final JwtAuthConverter jwtAuthConverter;
    private final PrometheusApiTokenFilter prometheusApiTokenFilter;

    @Value("#{'${cors.allowed-origins}'.split(',')}")
    private List<String> allowedOrigins;

    @Value("#{'${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}'}")
    private String jwkSetUri;

    @Value("#{'${resulter.settings.api-token.client-audience}'}")
    private String clientAudience;

    public OAuth2ResourceServerSecurityConfiguration(
            JwtAuthConverter jwtAuthConverter, PrometheusApiTokenFilter prometheusApiTokenFilter) {
        this.jwtAuthConverter = jwtAuthConverter;
        this.prometheusApiTokenFilter = prometheusApiTokenFilter;
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(List.of("*"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Content-Disposition"));
        configuration.setMaxAge(CORS_PREFLIGHT_CACHE_MAX_AGE);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    @Order(1)
    public SecurityFilterChain actuatorSecurityFilterChain(HttpSecurity http) {
        http.securityMatcher(EndpointRequest.to(PrometheusScrapeEndpoint.class)) // Define specific matcher
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .addFilterBefore(prometheusApiTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .csrf(csrf -> csrf.ignoringRequestMatchers(EndpointRequest.to(PrometheusScrapeEndpoint.class)))
                // Disable JWT auth for this chain
                .oauth2ResourceServer(AbstractHttpConfigurer::disable)
                // Disable session creation
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }

    @Bean
    @Order(2)
    protected SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http.securityMatcher("/**")
                .authorizeHttpRequests(auth -> auth.requestMatchers(HttpMethod.POST, "/createDatabase")
                        .hasRole(ADMIN)
                        .requestMatchers("/public/**")
                        .permitAll()
                        .requestMatchers("/swagger-ui/**")
                        .permitAll()
                        .requestMatchers("/v3/api-docs/**")
                        .permitAll()
                        .requestMatchers(EndpointRequest.to(HealthEndpoint.class))
                        .permitAll()
                        .requestMatchers(EndpointRequest.toAnyEndpoint())
                        .hasAnyRole(ADMIN, ENDPOINT_ADMIN)
                        .requestMatchers("/admin/**")
                        .hasRole(ADMIN)
                        .requestMatchers(HttpMethod.POST, "/upload", "/media/upload")
                        .hasRole(ADMIN)
                        .requestMatchers(
                                HttpMethod.GET,
                                "/certificate_schema",
                                "/course",
                                "/course/all",
                                "/cup",
                                "/cup/all",
                                "/cup/{id}/results",
                                "/cup_status",
                                "/cup_types",
                                "/event",
                                "/event/all",
                                "/event/{id}",
                                "/event/{id}/certificate_stats",
                                "/event/{id}/results",
                                "/event_certificate",
                                "/event_status",
                                "/media",
                                "/media/all",
                                "/organisation",
                                "/organisation/all",
                                "/person",
                                "/person/all",
                                "/race",
                                "/race/all",
                                "/result_list/{id}/certificate",
                                "/result_list/{id}/cup_score_lists",
                                "/split_time_analysis/result_list/{id}/ranking",
                                "/split_time_analysis/result_list/{id}/persons",
                                "/split_time_analysis/result_list/{id}/mental_resilience",
                                "/split_time_analysis/result_list/{id}/anomaly_detection",
                                "/split_time_analysis/result_list/{id}/hanging_detection",
                                "/split_time_analysis/result_list/{id}/split_table",
                                "/split_time_analysis/result_list/{id}/split_table/options",
                                "/version")
                        .permitAll()
                        .anyRequest()
                        .hasRole(ADMIN))
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwtConfigurer -> jwtConfigurer.jwtAuthenticationConverter(jwtAuthConverter)))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.ignoringRequestMatchers(
                    PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST, "/createDatabase")));

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withJwkSetUri(jwkSetUri)
                .jwsAlgorithm(SignatureAlgorithm.RS512)
                .build();
    }

    @Bean
    @Primary
    public JwtDecoder enhancedJwtDecoder() {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri)
                .jwsAlgorithm(SignatureAlgorithm.RS512)
                .build();

        decoder.setJwtValidator(token -> {
            // Validate expiration
            if (token.getExpiresAt() != null && token.getExpiresAt().isBefore(Instant.now())) {
                return OAuth2TokenValidatorResult.failure(new OAuth2Error("Invalid token: expired"));
            }

            // Validate audience (example)
            List<String> audience = token.getAudience();
            if (audience == null || clientAudience == null || !audience.contains(clientAudience)) {
                return OAuth2TokenValidatorResult.failure(new OAuth2Error("Invalid token: invalid audience"));
            }

            return OAuth2TokenValidatorResult.success();
        });

        return decoder;
    }
}
