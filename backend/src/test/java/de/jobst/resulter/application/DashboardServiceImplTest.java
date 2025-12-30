package de.jobst.resulter.application;

import de.jobst.resulter.application.port.DashboardStatisticsDto;
import de.jobst.resulter.application.port.DashboardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class DashboardServiceImplTest {

    private DashboardServiceImpl dashboardService;
    private DashboardRepository dashboardRepository;

    @BeforeEach
    void setUp() {
        dashboardRepository = Mockito.mock(DashboardRepository.class);
        dashboardService = new DashboardServiceImpl(dashboardRepository);
    }

    @Test
    void getStatistics_shouldReturnCorrectCounts() {
        // Given
        when(dashboardRepository.countEvents()).thenReturn(10L);
        when(dashboardRepository.countCups()).thenReturn(5L);
        when(dashboardRepository.countPersons()).thenReturn(100L);
        when(dashboardRepository.countOrganisationsExcludingIndividuals()).thenReturn(20L);
        when(dashboardRepository.countSplitTimes()).thenReturn(5000L);
        when(dashboardRepository.countRaces()).thenReturn(30L);
        when(dashboardRepository.countResultLists()).thenReturn(25L);
        when(dashboardRepository.countCertificates()).thenReturn(150L);

        // When
        DashboardStatisticsDto result = dashboardService.getStatistics();

        // Then
        assertThat(result.eventCount()).isEqualTo(10L);
        assertThat(result.cupCount()).isEqualTo(5L);
        assertThat(result.personCount()).isEqualTo(100L);
        assertThat(result.organisationCount()).isEqualTo(20L);
        assertThat(result.splitTimeCount()).isEqualTo(5000L);
        assertThat(result.raceCount()).isEqualTo(30L);
        assertThat(result.resultListCount()).isEqualTo(25L);
        assertThat(result.certificateCount()).isEqualTo(150L);
    }

    @Test
    void getStatistics_shouldHandleZeroCounts() {
        // Given
        when(dashboardRepository.countEvents()).thenReturn(0L);
        when(dashboardRepository.countCups()).thenReturn(0L);
        when(dashboardRepository.countPersons()).thenReturn(0L);
        when(dashboardRepository.countOrganisationsExcludingIndividuals()).thenReturn(0L);
        when(dashboardRepository.countSplitTimes()).thenReturn(0L);
        when(dashboardRepository.countRaces()).thenReturn(0L);
        when(dashboardRepository.countResultLists()).thenReturn(0L);
        when(dashboardRepository.countCertificates()).thenReturn(0L);

        // When
        DashboardStatisticsDto result = dashboardService.getStatistics();

        // Then
        assertThat(result.eventCount()).isZero();
        assertThat(result.cupCount()).isZero();
        assertThat(result.personCount()).isZero();
        assertThat(result.organisationCount()).isZero();
        assertThat(result.splitTimeCount()).isZero();
        assertThat(result.raceCount()).isZero();
        assertThat(result.resultListCount()).isZero();
        assertThat(result.certificateCount()).isZero();
    }
}
