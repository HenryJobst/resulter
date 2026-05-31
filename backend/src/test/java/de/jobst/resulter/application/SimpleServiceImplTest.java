package de.jobst.resulter.application;

import de.jobst.resulter.application.port.CountryRepository;
import de.jobst.resulter.application.port.CourseRepository;
import de.jobst.resulter.application.port.RaceRepository;
import de.jobst.resulter.application.port.SplitTimeListRepository;
import de.jobst.resulter.domain.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SimpleServiceImplTest {

    // -------------------------------------------------------------------------
    // CourseServiceImpl
    // -------------------------------------------------------------------------

    @Mock CourseRepository courseRepository;
    @InjectMocks CourseServiceImpl courseService;

    @Test
    void courseService_findById_delegatesToRepository() {
        when(courseRepository.findById(CourseId.of(1L))).thenReturn(Optional.empty());
        assertThat(courseService.findById(CourseId.of(1L))).isEmpty();
    }

    @Test
    void courseService_findAll_delegatesToRepository() {
        when(courseRepository.findAll()).thenReturn(List.of());
        assertThat(courseService.findAll()).isEmpty();
    }

    @Test
    void courseService_findOrCreate_delegatesToRepository() {
        when(courseRepository.findOrCreate(List.of())).thenReturn(List.of());
        assertThat(courseService.findOrCreate(List.of())).isEmpty();
    }

    // -------------------------------------------------------------------------
    // SplitTimeListServiceImpl
    // -------------------------------------------------------------------------

    @Mock SplitTimeListRepository splitTimeListRepository;
    @InjectMocks SplitTimeListServiceImpl splitTimeListService;

    @Test
    void splitTimeListService_findAll_delegatesToRepository() {
        when(splitTimeListRepository.findAll()).thenReturn(List.of());
        assertThat(splitTimeListService.findAll()).isEmpty();
    }

    @Test
    void splitTimeListService_findById_delegatesToRepository() {
        when(splitTimeListRepository.findById(SplitTimeListId.of(1L))).thenReturn(Optional.empty());
        assertThat(splitTimeListService.findById(SplitTimeListId.of(1L))).isEmpty();
    }

    @Test
    void splitTimeListService_findOrCreate_delegatesToRepository() {
        when(splitTimeListRepository.findOrCreate(List.of())).thenReturn(List.of());
        assertThat(splitTimeListService.findOrCreate(List.of())).isEmpty();
    }

    // -------------------------------------------------------------------------
    // RaceServiceImpl
    // -------------------------------------------------------------------------

    @Mock RaceRepository raceRepository;
    @InjectMocks RaceServiceImpl raceService;

    @Test
    void raceService_findAll_delegatesToRepository() {
        when(raceRepository.findAll()).thenReturn(List.of());
        assertThat(raceService.findAll()).isEmpty();
    }

    @Test
    void raceService_findById_delegatesToRepository() {
        when(raceRepository.findById(RaceId.of(1L))).thenReturn(Optional.empty());
        assertThat(raceService.findById(RaceId.of(1L))).isEmpty();
    }

    @Test
    void raceService_findOrCreate_collection_delegatesToRepository() {
        when(raceRepository.findOrCreate(List.of())).thenReturn(List.of());
        assertThat(raceService.findOrCreate(List.of())).isEmpty();
    }

    @Test
    void raceService_findAllByEventIds_delegatesToRepository() {
        when(raceRepository.findAllByEventIds(List.of())).thenReturn(List.of());
        assertThat(raceService.findAllByEventIds(List.of())).isEmpty();
    }

    @Test
    void raceService_findOrCreate_single_delegatesToRepository() {
        Race race = Race.of(EventId.of(1L), "Sprint", (byte) 1);
        when(raceRepository.findOrCreate(race)).thenReturn(race);
        assertThat(raceService.findOrCreate(race)).isEqualTo(race);
    }

    // -------------------------------------------------------------------------
    // CountryServiceImpl
    // -------------------------------------------------------------------------

    @Mock CountryRepository countryRepository;
    @InjectMocks CountryServiceImpl countryService;

    @Test
    void countryService_findAll_delegatesToRepository() {
        when(countryRepository.findAll()).thenReturn(List.of());
        assertThat(countryService.findAll()).isEmpty();
    }

    @Test
    void countryService_findById_delegatesToRepository() {
        when(countryRepository.findById(CountryId.of(1L))).thenReturn(Optional.empty());
        assertThat(countryService.findById(CountryId.of(1L))).isEmpty();
    }

    @Test
    void countryService_findOrCreate_single_delegatesToRepository() {
        Country country = Country.of(1L, "DE", "Deutschland");
        when(countryRepository.findOrCreate(country)).thenReturn(country);
        assertThat(countryService.findOrCreate(country)).isEqualTo(country);
    }

    @Test
    void countryService_findOrCreate_collection_delegatesToRepository() {
        when(countryRepository.findOrCreate(List.of())).thenReturn(List.of());
        assertThat(countryService.findOrCreate(List.of())).isEmpty();
    }

    @Test
    void countryService_findAllById_delegatesToRepository() {
        when(countryRepository.findAllById(Set.of())).thenReturn(Map.of());
        assertThat(countryService.findAllById(Set.of())).isEmpty();
    }

    @Test
    void countryService_batchLoadForOrganisations_withOrgWithoutCountry_returnsEmpty() {
        Organisation org = Organisation.of("OLOV", "O");
        when(countryRepository.findAllById(Set.of())).thenReturn(Map.of());
        assertThat(countryService.batchLoadForOrganisations(List.of(org))).isEmpty();
    }

    @Test
    void countryService_batchLoadForOrganisations_withOrgWithCountry_loadsCountry() {
        Country country = Country.of(1L, "DE", "Deutschland");
        Organisation org = Organisation.of("TSB", "T", CountryId.of(1L));
        when(countryRepository.findAllById(Set.of(CountryId.of(1L)))).thenReturn(Map.of(CountryId.of(1L), country));
        var result = countryService.batchLoadForOrganisations(List.of(org));
        assertThat(result).containsKey(CountryId.of(1L));
    }

    @Test
    void countryService_createCountry_savesCountry() {
        Country country = Country.of(CountryCode.of("DE"), CountryName.of("Deutschland"));
        when(countryRepository.save(org.mockito.ArgumentMatchers.any())).thenReturn(country);
        assertThat(countryService.createCountry(CountryCode.of("DE"), CountryName.of("Deutschland")))
                .isNotNull();
    }

    @Test
    void countryService_getById_returnsCountryWhenFound() {
        Country country = Country.of(1L, "DE", "Deutschland");
        when(countryRepository.findById(CountryId.of(1L))).thenReturn(Optional.of(country));
        assertThat(countryService.getById(CountryId.of(1L))).isEqualTo(country);
    }

    @Test
    void countryService_updateCountry_savesUpdatedCountry() {
        Country existing = Country.of(1L, "AT", "Österreich");
        Country updated = Country.of(1L, "DE", "Deutschland");
        when(countryRepository.findById(CountryId.of(1L))).thenReturn(Optional.of(existing));
        when(countryRepository.save(org.mockito.ArgumentMatchers.any())).thenReturn(updated);
        var result = countryService.updateCountry(CountryId.of(1L), CountryCode.of("DE"), CountryName.of("Deutschland"));
        assertThat(result).isNotNull();
    }
}
