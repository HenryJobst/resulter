package de.jobst.resulter.application.port;

import org.jmolecules.architecture.hexagonal.PrimaryPort;

/**
 * Service interface for dashboard operations.
 */
@PrimaryPort
public interface DashboardService {

    /**
     * Get system-wide statistics for dashboard display.
     *
     * @return DashboardStatisticsDto containing all counts
     */
    DashboardStatisticsDto getStatistics();
}
