package de.jobst.resulter.application;

import de.jobst.resulter.application.port.DashboardStatisticsDto;
import de.jobst.resulter.application.port.DashboardRepository;
import de.jobst.resulter.application.port.DashboardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of DashboardService.
 * Aggregates statistics from the dashboard repository.
 */
@Service
@Slf4j
public class DashboardServiceImpl implements DashboardService {

    private final DashboardRepository dashboardRepository;

    public DashboardServiceImpl(DashboardRepository dashboardRepository) {
        this.dashboardRepository = dashboardRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardStatisticsDto getStatistics() {
        log.debug("Fetching dashboard statistics");

        long eventCount = dashboardRepository.countEvents();
        long cupCount = dashboardRepository.countCups();
        long personCount = dashboardRepository.countPersons();
        long organisationCount = dashboardRepository.countOrganisationsExcludingIndividuals();
        long splitTimeCount = dashboardRepository.countSplitTimes();
        long raceCount = dashboardRepository.countRaces();
        long resultListCount = dashboardRepository.countResultLists();
        long certificateCount = dashboardRepository.countCertificates();

        log.debug("Dashboard statistics: events={}, cups={}, persons={}, organisations={}, splitTimes={}, races={}, resultLists={}, certificates={}",
            eventCount, cupCount, personCount, organisationCount, splitTimeCount, raceCount, resultListCount, certificateCount);

        return new DashboardStatisticsDto(
            eventCount,
            cupCount,
            personCount,
            organisationCount,
            splitTimeCount,
            raceCount,
            resultListCount,
            certificateCount
        );
    }
}
