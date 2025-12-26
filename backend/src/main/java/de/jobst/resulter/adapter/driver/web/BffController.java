package de.jobst.resulter.adapter.driver.web;

import de.jobst.resulter.adapter.driver.web.dto.BffUserInfoDto;
import de.jobst.resulter.application.auth.BffUserInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bff")
@Slf4j
public class BffController {

    private final BffUserInfoService bffUserInfoService;

    public BffController(BffUserInfoService bffUserInfoService) {
        this.bffUserInfoService = bffUserInfoService;
    }

    @GetMapping("/user")
    public ResponseEntity<BffUserInfoDto> getUserInfo(Authentication authentication, jakarta.servlet.http.HttpServletRequest request) {
        log.info("GET /bff/user called");
        log.info("Authentication: {}", authentication != null ? authentication.getName() : "null");
        log.info("Session ID: {}", request.getSession(false) != null ? request.getSession(false).getId() : "no session");

        if (request.getCookies() != null) {
            log.info("Cookies received: {}", java.util.Arrays.stream(request.getCookies())
                    .map(c -> c.getName())
                    .collect(java.util.stream.Collectors.joining(", ")));
        } else {
            log.warn("No cookies received in /bff/user request");
        }

        return bffUserInfoService
                .extractUserInfo(authentication)
                .map(userInfo -> {
                    log.info("Returning user info for: {}", userInfo.username());
                    return ResponseEntity.ok(userInfo);
                })
                .orElseGet(() -> {
                    log.warn("No user info found, returning 401");
                    return ResponseEntity.status(401).build();
                });
    }

    @GetMapping("/csrf")
    public ResponseEntity<Void> getCsrfToken() {
        // CSRF token automatically added to response by Spring Security
        // This endpoint exists to trigger token generation for SPA
        return ResponseEntity.ok().build();
    }
}
