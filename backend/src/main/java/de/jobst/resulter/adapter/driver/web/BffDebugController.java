package de.jobst.resulter.adapter.driver.web;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/bff")
@Profile("!prod")
@Slf4j
public class BffDebugController {

    @GetMapping("/debug/authorities")
    public ResponseEntity<Map<String, Object>> getAuthorities(@Nullable Authentication authentication) {
        log.info("GET /bff/debug/authorities called");

        Map<String, Object> debug = new HashMap<>();

        if (authentication != null) {
            debug.put("authenticated", authentication.isAuthenticated());
            debug.put("principal", authentication.getPrincipal() != null ?
                                   authentication.getPrincipal().getClass().getName() : "-");
            debug.put("name", authentication.getName());
            debug.put("authorities", authentication.getAuthorities().stream()
                    .map(org.springframework.security.core.GrantedAuthority::getAuthority)
                    .collect(Collectors.toList()));
        } else {
            debug.put("authentication", "null");
        }

        return ResponseEntity.ok(debug);
    }
}
