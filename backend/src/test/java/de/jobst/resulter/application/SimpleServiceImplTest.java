package de.jobst.resulter.application;

import de.jobst.resulter.application.port.CountryRepository;
import de.jobst.resulter.application.port.CourseRepository;
import de.jobst.resulter.application.port.CupScoreListRepository;
import de.jobst.resulter.application.port.EventCertificateQueryService;
import de.jobst.resulter.application.port.EventCertificateRepository;
import de.jobst.resulter.application.port.EventCertificateService;
import de.jobst.resulter.application.port.EventCertificateStatRepository;
import de.jobst.resulter.application.port.EventRepository;
import de.jobst.resulter.application.port.EventService;
import de.jobst.resulter.application.port.MediaFileRepository;
import de.jobst.resulter.application.port.MediaFileService;
import de.jobst.resulter.application.port.OrganisationRepository;
import de.jobst.resulter.application.port.PersonRepository;
import de.jobst.resulter.application.port.PersonService;
import de.jobst.resulter.application.port.RaceRepository;
import de.jobst.resulter.application.port.ResultListRepository;
import de.jobst.resulter.application.port.ResultListService;
import de.jobst.resulter.application.port.SplitTimeListRepository;
import de.jobst.resulter.domain.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
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

    // -------------------------------------------------------------------------
    // OrganisationServiceImpl
    // -------------------------------------------------------------------------

    @Mock OrganisationRepository organisationRepository;
    @InjectMocks OrganisationServiceImpl organisationService;

    @Test
    void organisationService_findAll_delegatesToRepository() {
        when(organisationRepository.findAll()).thenReturn(List.of());
        assertThat(organisationService.findAll()).isEmpty();
    }

    @Test
    void organisationService_findById_delegatesToRepository() {
        when(organisationRepository.findById(OrganisationId.of(1L))).thenReturn(Optional.empty());
        assertThat(organisationService.findById(OrganisationId.of(1L))).isEmpty();
    }

    @Test
    void organisationService_findOrCreate_single_delegatesToRepository() {
        Organisation org = Organisation.of("TSB OJ", "TSB");
        when(organisationRepository.findOrCreate(org)).thenReturn(org);
        assertThat(organisationService.findOrCreate(org)).isEqualTo(org);
    }

    @Test
    void organisationService_findOrCreate_collection_delegatesToRepository() {
        List<Organisation> empty = List.of();
        when(organisationRepository.findOrCreate(empty)).thenReturn(List.of());
        assertThat(organisationService.findOrCreate(empty)).isEmpty();
    }

    @Test
    void organisationService_getById_returnsOrganisationWhenFound() {
        Organisation org = Organisation.of(1L, "OLOV", "O");
        when(organisationRepository.findById(OrganisationId.of(1L))).thenReturn(Optional.of(org));
        assertThat(organisationService.getById(OrganisationId.of(1L))).isEqualTo(org);
    }

    @Test
    void organisationService_getById_throwsWhenNotFound() {
        when(organisationRepository.findById(OrganisationId.of(99L))).thenReturn(Optional.empty());
        assertThatThrownBy(() -> organisationService.getById(OrganisationId.of(99L)))
                .isInstanceOf(de.jobst.resulter.domain.util.ResourceNotFoundException.class);
    }

    @Test
    void organisationService_findByIds_delegatesToRepository() {
        when(organisationRepository.findByIds(List.of())).thenReturn(List.of());
        assertThat(organisationService.findByIds(List.of())).isEmpty();
    }

    @Test
    void organisationService_findAllById_delegatesToRepository() {
        when(organisationRepository.findByIds(Set.of())).thenReturn(List.of());
        assertThat(organisationService.findAllById(Set.of())).isEmpty();
    }

    @Test
    void organisationService_findAllByIdAsMap_delegatesToRepository() {
        when(organisationRepository.findAllById(Set.of())).thenReturn(Map.of());
        assertThat(organisationService.findAllByIdAsMap(Set.of())).isEmpty();
    }

    @Test
    void organisationService_deleteOrganisation_returnsFalseWhenNotFound() {
        when(organisationRepository.findById(OrganisationId.of(1L))).thenReturn(Optional.empty());
        assertThat(organisationService.deleteOrganisation(OrganisationId.of(1L))).isFalse();
    }

    @Test
    void organisationService_deleteOrganisation_returnsTrueWhenFound() {
        Organisation org = Organisation.of(1L, "TSB", "T");
        when(organisationRepository.findById(OrganisationId.of(1L))).thenReturn(Optional.of(org));
        assertThat(organisationService.deleteOrganisation(OrganisationId.of(1L))).isTrue();
        verify(organisationRepository).deleteOrganisation(org);
    }

    @Test
    void organisationService_findAllPaged_delegatesToRepository() {
        Page<Organisation> page = new PageImpl<>(List.of());
        when(organisationRepository.findAll(null, PageRequest.of(0, 10))).thenReturn(page);
        assertThat(organisationService.findAll(null, PageRequest.of(0, 10))).isEmpty();
    }

    @Test
    void organisationService_createOrganisation_returnsNullWhenCountryNotFound() {
        when(countryRepository.findById(CountryId.of(99L))).thenReturn(Optional.empty());
        var result = organisationService.createOrganisation(
                OrganisationName.of("Test"), OrganisationShortName.of("T"),
                OrganisationType.CLUB, CountryId.of(99L), List.of());
        assertThat(result).isNull();
    }

    @Test
    void organisationService_createOrganisation_savesWhenCountryFound() {
        Country country = Country.of(1L, "DE", "Deutschland");
        Organisation saved = Organisation.of("Test Org", "TO");
        when(countryRepository.findById(CountryId.of(1L))).thenReturn(Optional.of(country));
        when(organisationRepository.findByIds(List.of())).thenReturn(List.of());
        when(organisationRepository.save(any())).thenReturn(saved);
        var result = organisationService.createOrganisation(
                OrganisationName.of("Test Org"), OrganisationShortName.of("TO"),
                OrganisationType.CLUB, CountryId.of(1L), List.of());
        assertThat(result).isNotNull();
    }

    @Test
    void organisationService_updateOrganisation_savesUpdated() {
        Organisation existing = Organisation.of(1L, "Alt", "A");
        Organisation saved = Organisation.of(1L, "Neu", "N");
        Country country = Country.of(2L, "DE", "Deutschland");
        when(organisationRepository.findById(OrganisationId.of(1L))).thenReturn(Optional.of(existing));
        when(countryRepository.findById(CountryId.of(2L))).thenReturn(Optional.of(country));
        when(organisationRepository.findByIds(List.of())).thenReturn(List.of());
        when(organisationRepository.save(any())).thenReturn(saved);
        var result = organisationService.updateOrganisation(
                OrganisationId.of(1L), OrganisationName.of("Neu"), OrganisationShortName.of("N"),
                OrganisationType.CLUB, CountryId.of(2L), List.of());
        assertThat(result).isNotNull();
    }

    @Test
    void organisationService_batchLoadChildOrganisations_withNoChildren_returnsEmpty() {
        Organisation org = Organisation.of("OLOV", "O");
        when(organisationRepository.findAllById(Set.of())).thenReturn(Map.of());
        assertThat(organisationService.batchLoadChildOrganisations(List.of(org))).isEmpty();
    }

    // -------------------------------------------------------------------------
    // PersonServiceImpl
    // -------------------------------------------------------------------------

    @Mock PersonRepository personRepository;
    @Mock ResultListRepository resultListRepository;
    @Mock CupScoreListRepository cupScoreListRepository;
    @Mock EventCertificateStatRepository eventCertificateStatRepository;
    @InjectMocks PersonServiceImpl personService;

    @Test
    void personService_findAll_delegatesToRepository() {
        when(personRepository.findAll()).thenReturn(List.of());
        assertThat(personService.findAll()).isEmpty();
    }

    @Test
    void personService_findById_delegatesToRepository() {
        when(personRepository.findById(PersonId.of(1L))).thenReturn(Optional.empty());
        assertThat(personService.findById(PersonId.of(1L))).isEmpty();
    }

    @Test
    void personService_getById_returnsPersonWhenFound() {
        Person person = Person.of(1L, "Müller", "Hans", null, Gender.M);
        when(personRepository.findById(PersonId.of(1L))).thenReturn(Optional.of(person));
        assertThat(personService.getById(PersonId.of(1L))).isEqualTo(person);
    }

    @Test
    void personService_getById_throwsWhenNotFound() {
        when(personRepository.findById(PersonId.of(99L))).thenReturn(Optional.empty());
        assertThatThrownBy(() -> personService.getById(PersonId.of(99L)))
                .isInstanceOf(de.jobst.resulter.domain.util.ResourceNotFoundException.class);
    }

    @Test
    void personService_findAllByIdAsMap_withEmptySet_returnsEmpty() {
        assertThat(personService.findAllByIdAsMap(Set.of())).isEmpty();
    }

    @Test
    void personService_findAllByIdAsMap_withNull_returnsEmpty() {
        assertThat(personService.findAllByIdAsMap(null)).isEmpty();
    }

    @Test
    void personService_findAllByIdAsMap_withIds_delegatesToRepository() {
        Person person = Person.of(5L, "Koch", "Anna", null, Gender.F);
        when(personRepository.findAllById(Set.of(PersonId.of(5L)))).thenReturn(Map.of(PersonId.of(5L), person));
        var result = personService.findAllByIdAsMap(Set.of(PersonId.of(5L)));
        assertThat(result).containsKey(PersonId.of(5L));
    }

    @Test
    void personService_findOrCreate_single_delegatesToRepository() {
        Person person = Person.of(1L, "Weber", "Fritz", null, Gender.M);
        PersonRepository.PersonPerson pp = new PersonRepository.PersonPerson(person, person);
        when(personRepository.findOrCreate(person)).thenReturn(pp);
        assertThat(personService.findOrCreate(person)).isEqualTo(pp);
    }

    @Test
    void personService_findOrCreate_collection_delegatesToRepository() {
        when(personRepository.findOrCreate(List.of())).thenReturn(List.of());
        assertThat(personService.findOrCreate(List.of())).isEmpty();
    }

    @Test
    void personService_updatePerson_savesUpdated() {
        Person existing = Person.of(1L, "Alt", "Name", null, Gender.M);
        Person saved = Person.of(1L, "Neu", "Name", null, Gender.M);
        when(personRepository.findById(PersonId.of(1L))).thenReturn(Optional.of(existing));
        when(personRepository.save(any())).thenReturn(saved);
        var result = personService.updatePerson(
                PersonId.of(1L),
                PersonName.of(FamilyName.of("Neu"), GivenName.of("Name")),
                null, Gender.M);
        assertThat(result).isNotNull();
    }

    @Test
    void personService_findAllOrPossibleDuplicates_withoutDuplicates_callsFindAll() {
        Page<Person> page = new PageImpl<>(List.of());
        when(personRepository.findAll(null, PageRequest.of(0, 10))).thenReturn(page);
        assertThat(personService.findAllOrPossibleDuplicates(null, PageRequest.of(0, 10), false)).isEmpty();
    }

    @Test
    void personService_findAllOrPossibleDuplicates_withDuplicates_callsFindDuplicates() {
        Page<Person> page = new PageImpl<>(List.of());
        when(personRepository.findDuplicates(null, PageRequest.of(0, 10))).thenReturn(page);
        assertThat(personService.findAllOrPossibleDuplicates(null, PageRequest.of(0, 10), true)).isEmpty();
    }

    @Test
    void personService_deletePerson_throwsWhenNotFound() {
        when(personRepository.findById(PersonId.of(99L))).thenReturn(Optional.empty());
        assertThatThrownBy(() -> personService.deletePerson(PersonId.of(99L)))
                .isInstanceOf(de.jobst.resulter.domain.util.ResourceNotFoundException.class);
    }

    @Test
    void personService_deletePerson_deletesWhenFound() {
        Person person = Person.of(1L, "Braun", "Klaus", null, Gender.M);
        when(personRepository.findById(PersonId.of(1L))).thenReturn(Optional.of(person));
        personService.deletePerson(PersonId.of(1L));
        verify(personRepository).delete(person);
    }

    @Test
    void personService_findDoubles_noSimilarPersons_returnsEmpty() {
        Person base = Person.of(1L, "Müller", "Hans", null, Gender.M);
        Person other = Person.of(2L, "Xyz", "Abc", null, Gender.F);
        assertThat(personService.findDoubles(base, List.of(base, other))).isEmpty();
    }

    @Test
    void personService_findDoubles_sameNameSameId_excludesSelf() {
        Person person = Person.of(1L, "Müller", "Hans", null, Gender.M);
        assertThat(personService.findDoubles(person, List.of(person))).isEmpty();
    }

    @Test
    void personService_findDoubles_similarPersonFound_returnsMatch() {
        Person base = Person.of(1L, "Müller", "Hans", null, Gender.M);
        Person similar = Person.of(2L, "Müller", "Hans", null, Gender.M);
        var result = personService.findDoubles(base, List.of(base, similar));
        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(PersonId.of(2L));
    }

    @Test
    void personService_determineGroupLeaders_noPersons_returnsEmpty() {
        assertThat(personService.determineGroupLeaders(List.of())).isEmpty();
    }

    @Test
    void personService_determineGroupLeaders_withUniquePersons_returnsEmpty() {
        Person p1 = Person.of(1L, "Müller", "Hans", null, Gender.M);
        Person p2 = Person.of(2L, "Xyz", "Abc", null, Gender.F);
        assertThat(personService.determineGroupLeaders(List.of(p1, p2))).isEmpty();
    }

    @Test
    void personService_findDoubles_viaPersonId_usesRepository() {
        Person base = Person.of(1L, "Müller", "Hans", null, Gender.M);
        Person similar = Person.of(2L, "Müller", "Hans", null, Gender.M);
        when(personRepository.findById(PersonId.of(1L))).thenReturn(Optional.of(base));
        when(personRepository.findAll()).thenReturn(List.of(base, similar));
        var result = personService.findDoubles(PersonId.of(1L));
        assertThat(result).hasSize(1);
    }

    @Test
    void personService_findStrictDuplicates_withIdenticalPersons_returnsMatch() {
        Person base = Person.of(1L, "Müller", "Hans", null, Gender.M);
        Person identical = Person.of(2L, "Müller", "Hans", null, Gender.M);
        var result = personService.findStrictDuplicates(base, List.of(base, identical));
        assertThat(result).hasSize(1);
    }

    @Test
    void personService_findStrictDuplicates_withDifferentPersons_returnsEmpty() {
        Person base = Person.of(1L, "Müller", "Hans", null, Gender.M);
        Person different = Person.of(2L, "Xyz", "Abc", null, Gender.F);
        var result = personService.findStrictDuplicates(base, List.of(base, different));
        assertThat(result).isEmpty();
    }

    @Test
    void personService_mergePersons_replaceAndDelete() {
        Person person = Person.of(1L, "Müller", "Hans", null, Gender.M);
        Person merge = Person.of(2L, "Müller", "Hans", null, Gender.M);
        when(personRepository.findById(PersonId.of(1L))).thenReturn(Optional.of(person));
        when(personRepository.findById(PersonId.of(2L))).thenReturn(Optional.of(merge));
        var result = personService.mergePersons(PersonId.of(1L), PersonId.of(2L));
        assertThat(result).isEqualTo(person);
        verify(personRepository).delete(merge);
        verify(resultListRepository).replacePersonId(PersonId.of(2L), PersonId.of(1L));
        verify(splitTimeListRepository).replacePersonId(PersonId.of(2L), PersonId.of(1L));
        verify(cupScoreListRepository).replacePersonId(PersonId.of(2L), PersonId.of(1L));
        verify(eventCertificateStatRepository).replacePersonId(PersonId.of(2L), PersonId.of(1L));
    }

    @Test
    void personService_determineGroupLeaders_withStrictDuplicates_returnsLeader() {
        Person p1 = Person.of(1L, "Müller", "Hans", null, Gender.M);
        Person p2 = Person.of(2L, "Müller", "Hans", null, Gender.M);
        var leaders = personService.determineGroupLeaders(List.of(p1, p2));
        // The smallest ID should be the leader
        assertThat(leaders).contains(1L);
    }

    // -------------------------------------------------------------------------
    // EventServiceImpl
    // -------------------------------------------------------------------------

    @Mock EventRepository eventRepository;
    @Mock EventCertificateRepository eventCertificateRepository;
    @InjectMocks EventServiceImpl eventService;

    @Test
    void eventService_findAll_delegatesToRepository() {
        when(eventRepository.findAll()).thenReturn(List.of());
        assertThat(eventService.findAll()).isEmpty();
    }

    @Test
    void eventService_findById_delegatesToRepository() {
        when(eventRepository.findById(EventId.of(1L))).thenReturn(Optional.empty());
        assertThat(eventService.findById(EventId.of(1L))).isEmpty();
    }

    @Test
    void eventService_getById_returnsEventWhenFound() {
        Event event = Event.of(1L, "Sprint");
        when(eventRepository.findById(EventId.of(1L))).thenReturn(Optional.of(event));
        assertThat(eventService.getById(EventId.of(1L))).isEqualTo(event);
    }

    @Test
    void eventService_getById_throwsWhenNotFound() {
        when(eventRepository.findById(EventId.of(99L))).thenReturn(Optional.empty());
        assertThatThrownBy(() -> eventService.getById(EventId.of(99L)))
                .isInstanceOf(de.jobst.resulter.domain.util.ResourceNotFoundException.class);
    }

    @Test
    void eventService_findOrCreate_delegatesToRepository() {
        Event event = Event.of(1L, "Sprint");
        when(eventRepository.findOrCreate(event)).thenReturn(event);
        assertThat(eventService.findOrCreate(event)).isEqualTo(event);
    }

    @Test
    void eventService_findAllById_delegatesToRepository() {
        when(eventRepository.findAllById(List.of())).thenReturn(List.of());
        assertThat(eventService.findAllById(List.of())).isEmpty();
    }

    @Test
    void eventService_findAllByIdAsMap_withSingleEvent() {
        Event event = Event.of(1L, "Lauf");
        when(eventRepository.findAllById(Set.of(EventId.of(1L)))).thenReturn(List.of(event));
        var map = eventService.findAllByIdAsMap(Set.of(EventId.of(1L)));
        assertThat(map).containsKey(EventId.of(1L));
    }

    @Test
    void eventService_getByIds_allFound_returnsAll() {
        Event event = Event.of(1L, "Sprint");
        when(eventRepository.findAllById(List.of(EventId.of(1L)))).thenReturn(List.of(event));
        assertThat(eventService.getByIds(List.of(EventId.of(1L)))).hasSize(1);
    }

    @Test
    void eventService_getByIds_missingEvent_throwsException() {
        when(eventRepository.findAllById(List.of(EventId.of(1L), EventId.of(2L)))).thenReturn(List.of());
        assertThatThrownBy(() -> eventService.getByIds(List.of(EventId.of(1L), EventId.of(2L))))
                .isInstanceOf(de.jobst.resulter.domain.util.ResourceNotFoundException.class);
    }

    @Test
    void eventService_findAll_paged_delegatesToRepository() {
        Page<Event> page = new PageImpl<>(List.of());
        when(eventRepository.findAll(null, PageRequest.of(0, 10))).thenReturn(page);
        assertThat(eventService.findAll(null, PageRequest.of(0, 10))).isEmpty();
    }

    @Test
    void eventService_createEvent_savesEvent() {
        Event saved = Event.of(1L, "Test");
        when(organisationRepository.findByIds(Set.of())).thenReturn(List.of());
        when(eventRepository.save(any())).thenReturn(saved);
        var result = eventService.createEvent("Test", null, Set.of(), "Sprint", false);
        assertThat(result).isNotNull();
    }

    @Test
    void eventService_deleteEvent_deletesWhenFound() {
        Event event = Event.of(1L, "Sprint");
        when(eventRepository.findById(EventId.of(1L))).thenReturn(Optional.of(event));
        eventService.deleteEvent(EventId.of(1L));
        verify(eventRepository).deleteEvent(event);
    }

    @Test
    void eventService_deleteEvent_throwsWhenNotFound() {
        when(eventRepository.findById(EventId.of(99L))).thenReturn(Optional.empty());
        assertThatThrownBy(() -> eventService.deleteEvent(EventId.of(99L)))
                .isInstanceOf(de.jobst.resulter.domain.util.ResourceNotFoundException.class);
    }

    @Test
    void eventService_updateEvent_withoutCertificate_savesEvent() {
        Event event = Event.of(1L, "Sprint");
        when(eventRepository.findById(EventId.of(1L))).thenReturn(Optional.of(event));
        when(organisationRepository.findByIds(any())).thenReturn(List.of());
        when(eventRepository.save(any())).thenReturn(event);
        Event updated = eventService.updateEvent(
                EventId.of(1L), EventName.of("Neu"), null, EventStatus.PLANNED, List.of(), null, Discipline.SPRINT, false);
        assertThat(updated).isNotNull();
        verify(eventRepository).save(any());
    }

    @Test
    void eventService_updateEvent_withCertificate_savesCertificate() {
        Event event = Event.of(1L, "Sprint");
        EventCertificate cert = EventCertificate.of(0L, "MyCert", null, null, null, true);
        cert.setId(EventCertificateId.of(5L));
        when(eventRepository.findById(EventId.of(1L))).thenReturn(Optional.of(event));
        when(organisationRepository.findByIds(any())).thenReturn(List.of());
        when(eventCertificateRepository.findById(EventCertificateId.of(5L))).thenReturn(Optional.of(cert));
        when(eventCertificateRepository.findAllByEvent(EventId.of(1L))).thenReturn(List.of(cert));
        when(eventRepository.save(any())).thenReturn(event);
        Event updated = eventService.updateEvent(
                EventId.of(1L), EventName.of("Neu"), null, EventStatus.PLANNED, List.of(), EventCertificateId.of(5L), Discipline.SPRINT, false);
        assertThat(updated).isNotNull();
        verify(eventCertificateRepository).saveAll(any());
        verify(eventCertificateRepository).save(cert);
    }

    @Test
    void eventService_updateEvent_throwsWhenNotFound() {
        when(eventRepository.findById(EventId.of(99L))).thenReturn(Optional.empty());
        assertThatThrownBy(() -> eventService.updateEvent(
                EventId.of(99L), EventName.of("X"), null, EventStatus.PLANNED, List.of(), null, Discipline.SPRINT, false))
                .isInstanceOf(de.jobst.resulter.domain.util.ResourceNotFoundException.class);
    }

    // -------------------------------------------------------------------------
    // EventCertificateQueryServiceImpl
    // -------------------------------------------------------------------------

    @Mock EventCertificateService eventCertificateServiceMock;
    @Mock EventService eventServiceMock;
    @Mock MediaFileService mediaFileServiceMock;
    @Mock PersonService personServiceMock;
    @Mock ResultListService resultListServiceMock;
    @InjectMocks EventCertificateQueryServiceImpl eventCertQueryService;

    @Test
    void eventCertQueryService_findAll_withNoCertificates_returnsEmptyBatch() {
        when(eventCertificateServiceMock.findAll()).thenReturn(List.of());
        var result = eventCertQueryService.findAll();
        assertThat(result.eventCertificates()).isEmpty();
    }

    @Test
    void eventCertQueryService_findAll_withCertificates_buildsBatchResult() {
        EventCertificate cert = EventCertificate.of(1L, "Urkunde", EventId.of(1L), null, null, true);
        Event event = Event.of(1L, "Sprint");
        when(eventCertificateServiceMock.findAll()).thenReturn(List.of(cert));
        when(eventServiceMock.findAllByIdAsMap(any())).thenReturn(Map.of(EventId.of(1L), event));
        when(mediaFileServiceMock.findAllByIdAsMap(any())).thenReturn(Map.of());
        var result = eventCertQueryService.findAll();
        assertThat(result.eventCertificates()).hasSize(1);
    }

    @Test
    void eventCertQueryService_findById_whenFound_returnsBatch() {
        EventCertificate cert = EventCertificate.of(1L, "Urkunde", null, null, null, true);
        when(eventCertificateServiceMock.findById(EventCertificateId.of(1L))).thenReturn(Optional.of(cert));
        when(eventServiceMock.findAllByIdAsMap(any())).thenReturn(Map.of());
        when(mediaFileServiceMock.findAllByIdAsMap(any())).thenReturn(Map.of());
        var result = eventCertQueryService.findById(1L);
        assertThat(result).isPresent();
    }

    @Test
    void eventCertQueryService_findById_whenNotFound_returnsEmpty() {
        when(eventCertificateServiceMock.findById(EventCertificateId.of(99L))).thenReturn(Optional.empty());
        var result = eventCertQueryService.findById(99L);
        assertThat(result).isEmpty();
    }

    @Test
    void eventCertQueryService_getCertificateStats_withEmptyStats_returnsEmpty() {
        when(resultListServiceMock.getCertificateStats(EventId.of(1L))).thenReturn(List.of());
        var result = eventCertQueryService.getCertificateStats(EventId.of(1L));
        assertThat(result.eventCertificateStats()).isEmpty();
    }

    // -------------------------------------------------------------------------
    // EventCertificateServiceImpl
    // -------------------------------------------------------------------------

    @Mock MediaFileRepository mediaFileRepository;
    @InjectMocks EventCertificateServiceImpl eventCertService;

    @Test
    void eventCertService_findAll_delegatesToRepository() {
        when(eventCertificateRepository.findAll()).thenReturn(List.of());
        assertThat(eventCertService.findAll()).isEmpty();
    }

    @Test
    void eventCertService_findById_delegatesToRepository() {
        when(eventCertificateRepository.findById(EventCertificateId.of(1L))).thenReturn(Optional.empty());
        assertThat(eventCertService.findById(EventCertificateId.of(1L))).isEmpty();
    }

    @Test
    void eventCertService_getById_throwsWhenNotFound() {
        when(eventCertificateRepository.findById(EventCertificateId.of(99L))).thenReturn(Optional.empty());
        assertThatThrownBy(() -> eventCertService.getById(EventCertificateId.of(99L)))
                .isInstanceOf(de.jobst.resulter.domain.util.ResourceNotFoundException.class);
    }

    @Test
    void eventCertService_findAllByIdAsMap_delegatesToRepository() {
        when(eventCertificateRepository.findAllByIdAsMap(Set.of())).thenReturn(Map.of());
        assertThat(eventCertService.findAllByIdAsMap(Set.of())).isEmpty();
    }

    @Test
    void eventCertService_deleteEventCertificate_throwsWhenNotFound() {
        when(eventCertificateRepository.findById(EventCertificateId.of(99L))).thenReturn(Optional.empty());
        assertThatThrownBy(() -> eventCertService.deleteEventCertificate(EventCertificateId.of(99L)))
                .isInstanceOf(de.jobst.resulter.domain.util.ResourceNotFoundException.class);
    }

    @Test
    void eventCertService_deleteEventCertificate_deletesWhenFound() {
        EventCertificate cert = EventCertificate.of(1L, "Test", null, null, null, false);
        when(eventCertificateRepository.findById(EventCertificateId.of(1L))).thenReturn(Optional.of(cert));
        eventCertService.deleteEventCertificate(EventCertificateId.of(1L));
        verify(eventCertificateRepository).delete(cert);
    }

    @Test
    void eventCertService_createEventCertificate_withNullEventAndNoMediaFile_saves() {
        EventCertificate saved = EventCertificate.of(1L, "Neu", null, null, null, false);
        when(eventCertificateRepository.save(any())).thenReturn(saved);
        var result = eventCertService.createEventCertificate("Neu", null, null, null, false);
        assertThat(result).isNotNull();
    }

    @Test
    void eventCertService_findAll_paged_delegatesToRepository() {
        Page<EventCertificate> page = new PageImpl<>(List.of());
        when(eventCertificateRepository.findAll(null, PageRequest.of(0, 10))).thenReturn(page);
        assertThat(eventCertService.findAll(null, PageRequest.of(0, 10))).isEmpty();
    }
}
