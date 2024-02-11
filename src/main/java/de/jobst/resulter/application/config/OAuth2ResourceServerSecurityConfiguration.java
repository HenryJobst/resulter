package de.jobst.resulter.application.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@Profile("!nosecurity")
public class OAuth2ResourceServerSecurityConfiguration {

    private final JwtAuthConverter jwtAuthConverter;

    @Value("#{'${cors.allowed-origins}'.split(',')}")
    private List<String> allowedOrigins;

    public OAuth2ResourceServerSecurityConfiguration(JwtAuthConverter jwtAuthConverter) {
        this.jwtAuthConverter = jwtAuthConverter;
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(List.of("*"));
        configuration.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    protected SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth.requestMatchers("/public/**")
            .permitAll()
            .requestMatchers("/swagger-ui/**")
            .permitAll()
            .requestMatchers("/v3/api-docs/**")
            .permitAll()
            .requestMatchers("/actuator/health")
            .permitAll()
            .requestMatchers("/actuator/**")
            .hasRole("ADMIN")
            .requestMatchers("/admin/**")
            .hasRole("ADMIN")
            .requestMatchers(HttpMethod.GET, "/event", "/organisation", "/cup")
            .permitAll()
            .anyRequest()
            .hasRole("ADMIN"));
        http.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwtConfigurer -> jwtConfigurer.jwtAuthenticationConverter(
            jwtAuthConverter)));
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));
        http.csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }
}
