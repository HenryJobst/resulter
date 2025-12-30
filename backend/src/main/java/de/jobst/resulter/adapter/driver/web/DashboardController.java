package de.jobst.resulter.adapter.driver.web;

import de.jobst.resulter.application.port.DashboardStatisticsDto;
import de.jobst.resulter.application.port.DashboardService;
import de.jobst.resulter.springapp.config.ApiResponse;
import de.jobst.resulter.springapp.config.LocalizableString;
import de.jobst.resulter.springapp.config.MessageKeys;
import de.jobst.resulter.springapp.config.ResponseUtil;
import jakarta.servlet.http.HttpServletRequest;
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
     * @param request HTTP servlet request
     * @return ResponseEntity containing DashboardStatisticsDto
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<DashboardStatisticsDto>> getStatistics(
            HttpServletRequest request) {
        log.debug("GET /dashboard/statistics - Fetching dashboard statistics");

        DashboardStatisticsDto statistics = dashboardService.getStatistics();

        // Cache for 5 minutes to reduce database load
        CacheControl cacheControl = CacheControl.maxAge(5, TimeUnit.MINUTES)
            .cachePublic();

        return ResponseUtil.success(
            statistics,
            LocalizableString.of(MessageKeys.SUCCESSFULLY_RETRIEVED),
            request.getRequestURI(),
            cacheControl
        );
    }
}
