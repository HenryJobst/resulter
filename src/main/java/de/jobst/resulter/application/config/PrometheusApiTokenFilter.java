package de.jobst.resulter.application.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

@Component
public class PrometheusApiTokenFilter extends OncePerRequestFilter {

    public static final String X_PROMETHEUS_API_TOKEN = "X-PROMETHEUS-API-TOKEN";
    public static final String BEARER = "Bearer "; // space at end is important
    public static final String AUTHORIZATION = "Authorization";
    private final PrometheusConfigProperties configProperties;

    public PrometheusApiTokenFilter(PrometheusConfigProperties configProperties) {
        this.configProperties = configProperties;
    }

    @Data
    static class ResponseInfo {
        int statusCode = HttpServletResponse.SC_OK;
        String message = "";
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        if (request.getRequestURI().contains("/actuator/prometheus")) {
            Optional<String> authHeader = Optional.ofNullable(request.getHeader(AUTHORIZATION));
            Optional<String> apiToken = Optional.ofNullable(configProperties.getApiToken());

            ResponseInfo responseInfo = new ResponseInfo();

            // Default handling: Check if headerToken is missing or invalid
            if (apiToken.map(String::isEmpty).orElse(true)) {
                responseInfo.setStatusCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                responseInfo.setMessage("API Token for prometheus endpoint not configured");
            } else if (authHeader.map(String::isEmpty).orElse(true)) {
                responseInfo.setStatusCode(HttpServletResponse.SC_FORBIDDEN);
                responseInfo.setMessage("API Token for prometheus endpoint not given");
            } else {
                authHeader.filter(t -> !t.startsWith(BEARER)
                                || !t.substring(BEARER.length()).equals(apiToken.orElseThrow()))
                        .ifPresent(t -> {
                            responseInfo.setStatusCode(HttpServletResponse.SC_UNAUTHORIZED);
                            responseInfo.setMessage(
                                    "Unauthorized request to prometheus endpoint. Provide API Token as %s header."
                                            .formatted(X_PROMETHEUS_API_TOKEN));
                        });
            }

            if (responseInfo.getStatusCode() != HttpServletResponse.SC_OK) {
                response.sendError(responseInfo.getStatusCode(), responseInfo.getMessage());
                return;
            }

            Authentication auth = new UsernamePasswordAuthenticationToken(
                "prometheus",
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_PROMETHEUS"))
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        // Continue the filter chain
        filterChain.doFilter(request, response);
    }
}
