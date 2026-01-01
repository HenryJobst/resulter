package de.jobst.resulter.springapp.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;

/**
 * Debug filter to log request details for troubleshooting cookie domain issues.
 * This filter logs the request host, scheme, and forwarded headers to help diagnose
 * issues with Traefik proxy header forwarding.
 * <p>
 * To enable this filter, set the Spring profile to 'debug' or include 'debug' in active profiles.
 */
@Slf4j
@Component
@Profile("debug")
@Order(1)
public class RequestDebugFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (request instanceof HttpServletRequest httpRequest) {
            // Log request details for debugging
            log.info("=== Request Debug Info ===");
            log.info("Request URL: {}", httpRequest.getRequestURL());
            log.info("Request URI: {}", httpRequest.getRequestURI());
            log.info("Server Name: {}", httpRequest.getServerName());
            log.info("Server Port: {}", httpRequest.getServerPort());
            log.info("Scheme: {}", httpRequest.getScheme());
            log.info("Remote Addr: {}", httpRequest.getRemoteAddr());

            // Log important headers
            log.info("--- Headers ---");
            log.info("Host: {}", httpRequest.getHeader("Host"));
            log.info("X-Forwarded-Host: {}", httpRequest.getHeader("X-Forwarded-Host"));
            log.info("X-Forwarded-Proto: {}", httpRequest.getHeader("X-Forwarded-Proto"));
            log.info("X-Forwarded-For: {}", httpRequest.getHeader("X-Forwarded-For"));
            log.info("Forwarded: {}", httpRequest.getHeader("Forwarded"));

            // Log all headers for complete debugging
            log.info("--- All Headers ---");
            Collections.list(httpRequest.getHeaderNames()).forEach(headerName ->
                log.info("{}: {}", headerName, httpRequest.getHeader(headerName))
            );
            log.info("========================");
        }

        chain.doFilter(request, response);
    }
}
