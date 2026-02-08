package de.jobst.resulter.springapp.config;

import java.util.List;
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
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

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
        configuration.setAllowCredentials(true); // Required for session cookies
        configuration.setMaxAge(CORS_PREFLIGHT_CACHE_MAX_AGE);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    CookieCsrfTokenRepository csrfTokenRepository() {
        CookieCsrfTokenRepository repository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        // IMPORTANT: Set cookie path to root (/) so frontend JavaScript can read it
        // Default would be the request path (/api), which prevents frontend from accessing the cookie
        repository.setCookiePath("/");
        return repository;
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
    @Order(0)
    @Profile("e2e-frontend-tests")
    public SecurityFilterChain createDatabaseSecurityFilterChain(
            HttpSecurity http, CreateDatabaseApiTokenFilter createDatabaseApiTokenFilter) {
        http.securityMatcher(PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST, "/createDatabase"))
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .addFilterBefore(createDatabaseApiTokenFilter, UsernamePasswordAuthenticationFilter.class)
                // Disable CSRF for this endpoint
                .csrf(AbstractHttpConfigurer::disable)
                // Disable JWT auth for this chain
                .oauth2ResourceServer(AbstractHttpConfigurer::disable)
                // Disable session creation
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }

    /**
     * JWT-only API Security Chain
     * Order 2: Between Prometheus (1) and BFF (3)
     * Handles requests with Authorization: Bearer header
     * For future API-only access (when BFF is separated)
     * Excludes /actuator/** endpoints (they have dedicated security chains)
     */
    @Bean
    @Order(2)
    public SecurityFilterChain jwtApiSecurityFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher(request -> {
                    // Match requests with Authorization: Bearer header
                    // BUT exclude actuator endpoints (they use PrometheusApiTokenFilter, not JWT)
                    String authHeader = request.getHeader("Authorization");
                    String path = request.getRequestURI();
                    boolean hasBearer = authHeader != null && authHeader.startsWith("Bearer ");
                    // Check both /actuator/ and /api/actuator/ because path might be seen before or after Traefik stripprefix
                    boolean isActuator = path.startsWith("/actuator/") || path.startsWith("/api/actuator/");
                    boolean shouldMatch = hasBearer && !isActuator;

                    log.debug("JWT Chain Matcher: path={}, hasBearer={}, isActuator={}, shouldMatch={}",
                              path, hasBearer, isActuator, shouldMatch);

                    return shouldMatch;
                })
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/public/**").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers(EndpointRequest.to(HealthEndpoint.class)).permitAll()
                        .requestMatchers(EndpointRequest.toAnyEndpoint()).hasAnyRole(ADMIN, ENDPOINT_ADMIN)
                        .requestMatchers("/admin/**").hasRole(ADMIN)
                        .requestMatchers(HttpMethod.POST, "/upload", "/media/upload").hasRole(ADMIN)
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwtConfigurer -> jwtConfigurer.jwtAuthenticationConverter(jwtAuthConverter)))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // No CSRF needed for JWT (stateless)
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()));

        return http.build();
    }

    /**
     * Session-based API Security Chain
     * Order 4: Handles all other requests (without Authorization: Bearer header)
     * Uses session cookies from BFF authentication
     */
    @Bean
    @Order(4)
    protected SecurityFilterChain sessionApiSecurityFilterChain(HttpSecurity http) {
        http.securityMatcher("/**")
                .authorizeHttpRequests(auth -> auth.requestMatchers("/public/**")
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
                                "/dashboard/statistics",
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
                // Session-based authentication from BFF login (no JWT)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfTokenRepository())
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()));

        return http.build();
    }

    @Bean
    @Primary
    public JwtDecoder jwtDecoder() {
        // Create decoder that supports both RS256 (OIDC standard) and RS512.
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri)
                .jwsAlgorithm(SignatureAlgorithm.RS256)
                .jwsAlgorithm(SignatureAlgorithm.RS512)
                .build();

        OAuth2TokenValidator<Jwt> defaultValidator = JwtValidators.createDefaultWithIssuer(issuerUri);
        OAuth2TokenValidator<Jwt> audienceValidator = token -> {
            List<String> audience = token.getAudience();
            if (audience == null || audience.isEmpty() || !audience.contains(clientAudience)) {
                return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token",
                        "Invalid token audience", null));
            }
            return OAuth2TokenValidatorResult.success();
        };
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(defaultValidator, audienceValidator));

        return decoder;
    }
}
