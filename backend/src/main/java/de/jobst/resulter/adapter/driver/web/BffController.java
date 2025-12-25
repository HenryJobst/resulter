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
    public ResponseEntity<BffUserInfoDto> getUserInfo(Authentication authentication) {
        return bffUserInfoService
                .extractUserInfo(authentication)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(401).build());
    }

    @GetMapping("/csrf")
    public ResponseEntity<Void> getCsrfToken() {
        // CSRF token automatically added to response by Spring Security
        // This endpoint exists to trigger token generation for SPA
        return ResponseEntity.ok().build();
    }
}
