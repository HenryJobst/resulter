package de.jobst.resulter.application;

import de.jobst.resulter.application.port.*;
import de.jobst.resulter.domain.*;
import de.jobst.resulter.domain.util.ResourceNotFoundException;
import de.jobst.resulter.springapp.config.SpringSecurityAuditorAware;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Year;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CupServiceImplTest {

    @Mock CupRepository cupRepository;
    @Mock OrganisationRepository organisationRepository;
    @Mock OrganisationService organisationService;
    @Mock RaceService raceService;
    @Mock ResultListService resultListService;
    @Mock EventService eventService;
    @Mock CupScoreListRepository cupScoreListRepository;
    @Mock SpringSecurityAuditorAware springSecurityAuditorAware;
    @Mock PersonRepository personRepository;

    @InjectMocks
    CupServiceImpl service;

    private Cup cup(long id) {
        return Cup.of(id, "MyCup", CupType.ADD, Year.of(2024), List.of());
    }

    @Test
    void findAll_delegatesToRepository() {
        when(cupRepository.findAll()).thenReturn(List.of(cup(1L)));
        assertThat(service.findAll()).hasSize(1);
    }

    @Test
    void findOrCreate_delegatesToRepository() {
        Cup c = cup(1L);
        when(cupRepository.findOrCreate(c)).thenReturn(c);
        assertThat(service.findOrCreate(c)).isEqualTo(c);
    }

    @Test
    void findById_returnsPresent_whenFound() {
        Cup c = cup(1L);
        when(cupRepository.findById(CupId.of(1L))).thenReturn(Optional.of(c));
        assertThat(service.findById(CupId.of(1L))).isPresent();
    }

    @Test
    void findById_returnsEmpty_whenNotFound() {
        when(cupRepository.findById(CupId.of(99L))).thenReturn(Optional.empty());
        assertThat(service.findById(CupId.of(99L))).isEmpty();
    }

    @Test
    void getById_returnsCup_whenFound() {
        Cup c = cup(1L);
        when(cupRepository.findById(CupId.of(1L))).thenReturn(Optional.of(c));
        assertThat(service.getById(CupId.of(1L))).isEqualTo(c);
    }

    @Test
    void getById_throws_whenNotFound() {
        when(cupRepository.findById(CupId.of(99L))).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getById(CupId.of(99L)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createCup_savesNewCup() {
        Cup c = cup(1L);
        when(cupRepository.save(any(Cup.class))).thenReturn(c);
        Cup result = service.createCup("MyCup", CupType.ADD, Year.of(2024), List.of());
        verify(cupRepository).save(any(Cup.class));
        assertThat(result).isEqualTo(c);
    }

    @Test
    void deleteCup_callsDeleteOnRepository() {
        Cup c = cup(1L);
        when(cupRepository.findById(CupId.of(1L))).thenReturn(Optional.of(c));
        service.deleteCup(CupId.of(1L));
        verify(cupRepository).deleteCup(c);
    }

    @Test
    void deleteCup_throws_whenCupNotFound() {
        when(cupRepository.findById(CupId.of(99L))).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.deleteCup(CupId.of(99L)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateCup_savesUpdatedCup() {
        Cup c = cup(1L);
        when(cupRepository.findById(CupId.of(1L))).thenReturn(Optional.of(c));
        when(eventService.findAllById(any())).thenReturn(List.of());
        when(cupRepository.save(any(Cup.class))).thenReturn(c);
        Cup result = service.updateCup(CupId.of(1L), CupName.of("Neu"), CupType.NOR, Year.of(2025), List.of());
        verify(cupRepository).save(any(Cup.class));
        assertThat(result).isEqualTo(c);
    }

    @Test
    void findAll_paged_delegatesToRepository() {
        when(cupRepository.findAll(any(), any()))
                .thenReturn(new PageImpl<>(List.of(cup(1L))));
        var page = service.findAll(null, PageRequest.of(0, 10));
        assertThat(page).isNotEmpty();
    }

}
