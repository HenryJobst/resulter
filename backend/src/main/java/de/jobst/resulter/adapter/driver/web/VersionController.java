package de.jobst.resulter.adapter.driver.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VersionController {

    @Value("${app.version}")
    private String appVersion;

    @GetMapping("/version")
    public ResponseEntity<String> getAppVersion() {
        return ResponseEntity.ok(appVersion);
    }
}
