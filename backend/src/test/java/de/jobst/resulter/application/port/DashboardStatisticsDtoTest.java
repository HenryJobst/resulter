package de.jobst.resulter.application.port;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class DashboardStatisticsDtoTest {

    @Test
    void constructor_withValidValues_succeeds() {
        DashboardStatisticsDto dto = new DashboardStatisticsDto(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L);
        assertThat(dto.eventCount()).isEqualTo(1L);
        assertThat(dto.cupCount()).isEqualTo(2L);
        assertThat(dto.personCount()).isEqualTo(3L);
        assertThat(dto.organisationCount()).isEqualTo(4L);
        assertThat(dto.splitTimeCount()).isEqualTo(5L);
        assertThat(dto.raceCount()).isEqualTo(6L);
        assertThat(dto.resultListCount()).isEqualTo(7L);
        assertThat(dto.certificateCount()).isEqualTo(8L);
    }

    @Test
    void constructor_withNegativeEventCount_throws() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new DashboardStatisticsDto(-1L, 0L, 0L, 0L, 0L, 0L, 0L, 0L));
    }

    @Test
    void constructor_withNegativeCupCount_throws() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new DashboardStatisticsDto(0L, -1L, 0L, 0L, 0L, 0L, 0L, 0L));
    }

    @Test
    void constructor_withNegativePersonCount_throws() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new DashboardStatisticsDto(0L, 0L, -1L, 0L, 0L, 0L, 0L, 0L));
    }

    @Test
    void constructor_withNegativeOrganisationCount_throws() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new DashboardStatisticsDto(0L, 0L, 0L, -1L, 0L, 0L, 0L, 0L));
    }

    @Test
    void constructor_withNegativeSplitTimeCount_throws() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new DashboardStatisticsDto(0L, 0L, 0L, 0L, -1L, 0L, 0L, 0L));
    }

    @Test
    void constructor_withNegativeRaceCount_throws() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new DashboardStatisticsDto(0L, 0L, 0L, 0L, 0L, -1L, 0L, 0L));
    }

    @Test
    void constructor_withNegativeResultListCount_throws() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new DashboardStatisticsDto(0L, 0L, 0L, 0L, 0L, 0L, -1L, 0L));
    }

    @Test
    void constructor_withNegativeCertificateCount_throws() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new DashboardStatisticsDto(0L, 0L, 0L, 0L, 0L, 0L, 0L, -1L));
    }
}
