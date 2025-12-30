package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.application.port.DashboardRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled
@SpringBootTest
@ActiveProfiles("test")
class DashboardJdbcRepositoryIT {

    @Autowired
    private DashboardRepository dashboardRepository;

    @Test
    void countEvents_shouldReturnNonNegativeCount() {
        long count = dashboardRepository.countEvents();
        assertThat(count).isGreaterThanOrEqualTo(0L);
    }

    @Test
    void countCups_shouldReturnNonNegativeCount() {
        long count = dashboardRepository.countCups();
        assertThat(count).isGreaterThanOrEqualTo(0L);
    }

    @Test
    void countPersons_shouldReturnNonNegativeCount() {
        long count = dashboardRepository.countPersons();
        assertThat(count).isGreaterThanOrEqualTo(0L);
    }

    @Test
    void countOrganisationsExcludingIndividuals_shouldReturnNonNegativeCount() {
        long count = dashboardRepository.countOrganisationsExcludingIndividuals();
        assertThat(count).isGreaterThanOrEqualTo(0L);
    }

    @Test
    void countSplitTimes_shouldReturnNonNegativeCount() {
        long count = dashboardRepository.countSplitTimes();
        assertThat(count).isGreaterThanOrEqualTo(0L);
    }

    @Test
    void countRaces_shouldReturnNonNegativeCount() {
        long count = dashboardRepository.countRaces();
        assertThat(count).isGreaterThanOrEqualTo(0L);
    }

    @Test
    void countResultLists_shouldReturnNonNegativeCount() {
        long count = dashboardRepository.countResultLists();
        assertThat(count).isGreaterThanOrEqualTo(0L);
    }

    @Test
    void countCertificates_shouldReturnNonNegativeCount() {
        long count = dashboardRepository.countCertificates();
        assertThat(count).isGreaterThanOrEqualTo(0L);
    }
}
