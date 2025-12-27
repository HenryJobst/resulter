package de.jobst.resulter.springapp.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Profile;
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
@Profile("testcontainers")
public class CreateDatabaseApiTokenFilter extends OncePerRequestFilter {

    public static final String X_CREATE_DATABASE_TOKEN = "X-CREATE-DATABASE-TOKEN";
    public static final String BEARER = "Bearer "; // space at end is important
    public static final String AUTHORIZATION = "Authorization";
    private final CreateDatabaseTokenProperties configProperties;

    public CreateDatabaseApiTokenFilter(CreateDatabaseTokenProperties configProperties) {
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

        if (request.getRequestURI().equals("/createDatabase") && "POST".equals(request.getMethod())) {
            Optional<String> authHeader = Optional.ofNullable(request.getHeader(AUTHORIZATION))
                    .or(() -> Optional.ofNullable(request.getHeader(X_CREATE_DATABASE_TOKEN)).map(t -> BEARER + t));
            Optional<String> apiToken = Optional.ofNullable(configProperties.getApiToken());

            ResponseInfo responseInfo = new ResponseInfo();

            // Default handling: Check if token is configured and header is provided
            if (apiToken.map(String::isEmpty).orElse(true)) {
                responseInfo.setStatusCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                responseInfo.setMessage("API Token for createDatabase endpoint not configured");
            } else if (authHeader.map(String::isEmpty).orElse(true)) {
                responseInfo.setStatusCode(HttpServletResponse.SC_FORBIDDEN);
                responseInfo.setMessage("API Token for createDatabase endpoint not given. Provide token via Authorization: Bearer <token> or %s header."
                        .formatted(X_CREATE_DATABASE_TOKEN));
            } else {
                authHeader.filter(t -> !t.startsWith(BEARER)
                                || !t.substring(BEARER.length()).equals(apiToken.orElseThrow()))
                        .ifPresent(t -> {
                            responseInfo.setStatusCode(HttpServletResponse.SC_UNAUTHORIZED);
                            responseInfo.setMessage(
                                    "Unauthorized request to createDatabase endpoint. Provide valid API Token as %s header."
                                            .formatted(AUTHORIZATION));
                        });
            }

            if (responseInfo.getStatusCode() != HttpServletResponse.SC_OK) {
                response.sendError(responseInfo.getStatusCode(), responseInfo.getMessage());
                return;
            }

            Authentication auth = new UsernamePasswordAuthenticationToken(
                "create-database-user",
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_CREATE_DATABASE"))
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        // Continue the filter chain
        filterChain.doFilter(request, response);
    }
}
