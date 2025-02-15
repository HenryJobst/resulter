package de.jobst.resulter.application.config;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@Profile("!nosecurity")
public class OAuth2ResourceServerSecurityConfiguration {

    public static final long CORS_PREFLIGHT_CACHE_MAX_AGE = 3600L;
    public static final String ADMIN = "ADMIN";
    public static final String ENDPOINT_ADMIN = "ENDPOINT_ADMIN";
    private final JwtAuthConverter jwtAuthConverter;

    @Value("#{'${cors.allowed-origins}'.split(',')}")
    private List<String> allowedOrigins;

    @Value("#{'${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}'}")
    private String jwkSetUri;

    public OAuth2ResourceServerSecurityConfiguration(JwtAuthConverter jwtAuthConverter) {
        this.jwtAuthConverter = jwtAuthConverter;
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
    protected SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth.requestMatchers(HttpMethod.POST, "/createDatabase")
                .hasRole(ADMIN)
                .requestMatchers("/public/**")
                .permitAll()
                .requestMatchers("/swagger-ui/**")
                .permitAll()
                .requestMatchers("/v3/api-docs/**")
                .permitAll()
                .requestMatchers(EndpointRequest.to("health"))
                .permitAll()
                .requestMatchers("/actuator/**")
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
                        "/version")
                .permitAll()
                .anyRequest()
                .hasRole(ADMIN));
        http.oauth2ResourceServer(
                oauth2 -> oauth2.jwt(jwtConfigurer -> jwtConfigurer.jwtAuthenticationConverter(jwtAuthConverter)));
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));
        http.csrf(csrf ->
                csrf.ignoringRequestMatchers(new AntPathRequestMatcher("/createDatabase", HttpMethod.POST.name())));

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withJwkSetUri(jwkSetUri)
                .jwsAlgorithm(SignatureAlgorithm.RS512)
                .build();
    }
}
