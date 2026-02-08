package de.jobst.resulter.adapter.driver.web;

import de.jobst.resulter.adapter.BffUserInfoDto;
import de.jobst.resulter.application.auth.BffUserInfoService;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/bff")
@Slf4j
public class BffController {

    private final BffUserInfoService bffUserInfoService;

    public BffController(BffUserInfoService bffUserInfoService) {
        this.bffUserInfoService = bffUserInfoService;
    }

    @GetMapping("/user")
    public ResponseEntity<BffUserInfoDto> getUserInfo(@Nullable Authentication authentication) {
        log.debug("GET /bff/user called, authenticated={}",
                authentication != null && authentication.isAuthenticated());

        return bffUserInfoService
                .extractUserInfo(authentication)
                .map(userInfo -> {
                    BffUserInfoDto dto = BffUserInfoDto.from(
                            userInfo.username(),
                            userInfo.email(),
                            userInfo.name(),
                            userInfo.roles(),
                            userInfo.groups());
                    return ResponseEntity.ok(dto);
                })
                .orElseGet(() -> ResponseEntity.status(401).build());
    }

    @GetMapping("/csrf")
    public ResponseEntity<Void> getCsrfToken(org.springframework.security.web.csrf.CsrfToken csrfToken) {
        // CSRF token automatically added to response by Spring Security
        // This endpoint exists to trigger token generation for SPA
        // By accepting CsrfToken as parameter, we force Spring to generate and set the cookie
        log.debug("CSRF token requested");
        return ResponseEntity.ok().build();
    }

    @GetMapping("/debug/authorities")
    public ResponseEntity<Map<String, Object>> getAuthorities(@Nullable Authentication authentication) {
        log.info("GET /bff/debug/authorities called");

        Map<String, Object> debug = new java.util.HashMap<>();

        if (authentication != null) {
            debug.put("authenticated", authentication.isAuthenticated());
            debug.put("principal", authentication.getPrincipal() != null ?
                                   authentication.getPrincipal().getClass().getName() : "-");
            debug.put("name", authentication.getName());
            debug.put("authorities", authentication.getAuthorities().stream()
                    .map(org.springframework.security.core.GrantedAuthority::getAuthority)
                    .collect(java.util.stream.Collectors.toList()));
        } else {
            debug.put("authentication", "null");
        }

        return ResponseEntity.ok(debug);
    }
}
