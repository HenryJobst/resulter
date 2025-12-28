package de.jobst.resulter.adapter.driver.web;

import de.jobst.resulter.springapp.config.DataSourceManager;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Map;

@RestController
@Profile("e2e-frontend-tests")
public class DatabaseController {

    public static final String TIMEOUT = "timeout";
    private final DataSourceManager dataSourceManager;

    public DatabaseController(DataSourceManager dataSourceManager) {
        this.dataSourceManager = dataSourceManager;
    }

    @PostMapping("/createDatabase")
    public ResponseEntity<String> createDatabase(@RequestBody Map<String, Object> request) {
        Duration timeout = request.containsKey(TIMEOUT) ? Duration.ofMinutes((Integer) request.get(TIMEOUT)) : null;
        return ResponseEntity.ok(dataSourceManager.createNewDatabase(timeout));
    }
}

