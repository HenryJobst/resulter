package de.jobst.resulter.adapter.driver.web;

import de.jobst.resulter.application.port.DashboardStatisticsDto;
import de.jobst.resulter.application.port.DashboardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * REST Controller for dashboard operations.
 * Provides endpoints for retrieving system statistics.
 */
@RestController
@RequestMapping("/dashboard")
@Slf4j
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * Get system-wide statistics for dashboard display.
     * Public endpoint - no authentication required.
     *
     * @return ResponseEntity containing DashboardStatisticsDto
     */
    @GetMapping("/statistics")
    public ResponseEntity<DashboardStatisticsDto> getStatistics() {
        log.debug("GET /dashboard/statistics - Fetching dashboard statistics");

        DashboardStatisticsDto statistics = dashboardService.getStatistics();

        // Cache for 5 minutes to reduce database load
        CacheControl cacheControl = CacheControl.maxAge(5, TimeUnit.MINUTES)
            .cachePublic();

        return ResponseEntity.ok()
            .cacheControl(cacheControl)
            .body(statistics);
    }
}
