package de.jobst.resulter.adapter.driven.inmemory;

import de.jobst.resulter.domain.*;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.time.Year;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InMemoryRepositoriesTest {

    // -------------------------------------------------------------------------
    // InMemoryCountryRepository
    // -------------------------------------------------------------------------

    @Test
    void countryRepo_save_withEmptyId_assignsNewId() {
        InMemoryCountryRepository repo = new InMemoryCountryRepository();
        Country country = Country.of(null, "DE", "Deutschland");
        Country saved = repo.save(country);
        assertThat(saved.getId().value()).isGreaterThan(0L);
    }

    @Test
    void countryRepo_save_withExistingId_keepsId() {
        InMemoryCountryRepository repo = new InMemoryCountryRepository();
        Country country = Country.of(5L, "AT", "Österreich");
        Country saved = repo.save(country);
        assertThat(saved.getId().value()).isEqualTo(5L);
    }

    @Test
    void countryRepo_findAll_returnsAllSaved() {
        InMemoryCountryRepository repo = new InMemoryCountryRepository();
        repo.save(Country.of(1L, "DE", "Deutschland"));
        repo.save(Country.of(2L, "AT", "Österreich"));
        assertThat(repo.findAll()).hasSize(2);
    }

    @Test
    void countryRepo_findById_returnsCountryWhenPresent() {
        InMemoryCountryRepository repo = new InMemoryCountryRepository();
        Country saved = repo.save(Country.of(1L, "DE", "Deutschland"));
        assertThat(repo.findById(CountryId.of(1L))).isPresent();
    }

    @Test
    void countryRepo_findById_returnsEmptyWhenAbsent() {
        InMemoryCountryRepository repo = new InMemoryCountryRepository();
        assertThat(repo.findById(CountryId.of(99L))).isEmpty();
    }

    @Test
    void countryRepo_findAllById_returnsMatchingCountries() {
        InMemoryCountryRepository repo = new InMemoryCountryRepository();
        repo.save(Country.of(1L, "DE", "Deutschland"));
        repo.save(Country.of(2L, "AT", "Österreich"));
        var result = repo.findAllById(Set.of(CountryId.of(1L)));
        assertThat(result).containsKey(CountryId.of(1L));
        assertThat(result).doesNotContainKey(CountryId.of(2L));
    }

    @Test
    void countryRepo_findOrCreate_findsExistingByName() {
        InMemoryCountryRepository repo = new InMemoryCountryRepository();
        Country original = repo.save(Country.of(1L, "DE", "Deutschland"));
        Country found = repo.findOrCreate(Country.of(null, "DE", "Deutschland"));
        assertThat(found.getId()).isEqualTo(original.getId());
    }

    @Test
    void countryRepo_findOrCreate_createsWhenNotFound() {
        InMemoryCountryRepository repo = new InMemoryCountryRepository();
        Country created = repo.findOrCreate(Country.of(null, "FR", "Frankreich"));
        assertThat(created.getId().value()).isGreaterThan(0L);
    }

    @Test
    void countryRepo_findOrCreate_collection_mapsAll() {
        InMemoryCountryRepository repo = new InMemoryCountryRepository();
        var results = repo.findOrCreate(List.of(
                Country.of(null, "DE", "Deutschland"),
                Country.of(null, "AT", "Österreich")));
        assertThat(results).hasSize(2);
    }

    @Test
    void countryRepo_saveCount_andReset() {
        InMemoryCountryRepository repo = new InMemoryCountryRepository();
        repo.save(Country.of(1L, "DE", "Deutschland"));
        assertThat(repo.saveCount()).isEqualTo(1);
        repo.resetSaveCount();
        assertThat(repo.saveCount()).isEqualTo(0);
    }

    // -------------------------------------------------------------------------
    // InMemoryRaceRepository
    // -------------------------------------------------------------------------

    @Test
    void raceRepo_save_withNullId_assignsNewId() {
        InMemoryRaceRepository repo = new InMemoryRaceRepository();
        Race race = Race.of(EventId.of(1L), "Sprint", (byte) 1);
        Race saved = repo.save(race);
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void raceRepo_findAll_returnsAllSaved() {
        InMemoryRaceRepository repo = new InMemoryRaceRepository();
        repo.save(Race.of(EventId.of(1L), "Sprint", (byte) 1));
        repo.save(Race.of(EventId.of(1L), "Lang", (byte) 2));
        assertThat(repo.findAll()).hasSize(2);
    }

    @Test
    void raceRepo_findById_returnsRaceWhenPresent() {
        InMemoryRaceRepository repo = new InMemoryRaceRepository();
        Race saved = repo.save(Race.of(RaceId.of(1L), EventId.of(1L), "Sprint", (byte) 1));
        assertThat(repo.findById(RaceId.of(1L))).isPresent();
    }

    @Test
    void raceRepo_findById_returnsEmptyWhenAbsent() {
        InMemoryRaceRepository repo = new InMemoryRaceRepository();
        assertThat(repo.findById(RaceId.of(99L))).isEmpty();
    }

    @Test
    void raceRepo_findOrCreate_findsExisting() {
        InMemoryRaceRepository repo = new InMemoryRaceRepository();
        Race race = Race.of(RaceId.of(1L), EventId.of(1L), "Sprint", (byte) 1);
        repo.save(race);
        Race found = repo.findOrCreate(Race.of(EventId.of(1L), "Sprint", (byte) 1));
        assertThat(found).isNotNull();
    }

    @Test
    void raceRepo_findOrCreate_collection() {
        InMemoryRaceRepository repo = new InMemoryRaceRepository();
        var results = repo.findOrCreate(List.of(Race.of(EventId.of(1L), "Sprint", (byte) 1)));
        assertThat(results).hasSize(1);
    }

    @Test
    void raceRepo_findAllByEventIds_returnsMatchingRaces() {
        InMemoryRaceRepository repo = new InMemoryRaceRepository();
        repo.save(Race.of(RaceId.of(1L), EventId.of(1L), "Sprint", (byte) 1));
        repo.save(Race.of(RaceId.of(2L), EventId.of(2L), "Lang", (byte) 1));
        var result = repo.findAllByEventIds(List.of(EventId.of(1L)));
        assertThat(result).hasSize(1);
    }

    @Test
    void raceRepo_saveCount_andReset() {
        InMemoryRaceRepository repo = new InMemoryRaceRepository();
        repo.save(Race.of(EventId.of(1L), "Sprint", (byte) 1));
        assertThat(repo.saveCount()).isEqualTo(1);
        repo.resetSaveCount();
        assertThat(repo.saveCount()).isEqualTo(0);
    }

    // -------------------------------------------------------------------------
    // InMemoryOrganisationRepository
    // -------------------------------------------------------------------------

    @Test
    void orgRepo_save_withNullId_assignsNewId() {
        InMemoryOrganisationRepository repo = new InMemoryOrganisationRepository();
        Organisation org = Organisation.of("TSB OJ", "TSB");
        Organisation saved = repo.save(org);
        assertThat(saved.getId().value()).isGreaterThan(0L);
    }

    @Test
    void orgRepo_findAll_returnsAllSaved() {
        InMemoryOrganisationRepository repo = new InMemoryOrganisationRepository();
        repo.save(Organisation.of("TSB OJ", "TSB"));
        repo.save(Organisation.of("OLOV", "O"));
        assertThat(repo.findAll()).hasSize(2);
    }

    @Test
    void orgRepo_findById_returnsOrgWhenPresent() {
        InMemoryOrganisationRepository repo = new InMemoryOrganisationRepository();
        Organisation saved = repo.save(Organisation.of(1L, "TSB OJ", "TSB"));
        assertThat(repo.findById(OrganisationId.of(1L))).isPresent();
    }

    @Test
    void orgRepo_findOrCreate_findsExistingByName() {
        InMemoryOrganisationRepository repo = new InMemoryOrganisationRepository();
        repo.save(Organisation.of("TSB OJ", "TSB"));
        Organisation found = repo.findOrCreate(Organisation.of("TSB OJ", "TSB"));
        assertThat(found).isNotNull();
    }

    @Test
    void orgRepo_findOrCreate_createsWhenNotFound() {
        InMemoryOrganisationRepository repo = new InMemoryOrganisationRepository();
        Organisation created = repo.findOrCreate(Organisation.of("Neu", "N"));
        assertThat(created.getId().value()).isGreaterThan(0L);
    }

    @Test
    void orgRepo_findOrCreate_collection() {
        InMemoryOrganisationRepository repo = new InMemoryOrganisationRepository();
        var results = repo.findOrCreate(List.of(Organisation.of("A", "A"), Organisation.of("B", "B")));
        assertThat(results).hasSize(2);
    }

    @Test
    void orgRepo_deleteOrganisation_removesIt() {
        InMemoryOrganisationRepository repo = new InMemoryOrganisationRepository();
        Organisation saved = repo.save(Organisation.of(1L, "TSB", "T"));
        repo.deleteOrganisation(saved);
        assertThat(repo.findById(OrganisationId.of(1L))).isEmpty();
    }

    @Test
    void orgRepo_findAllById_returnsMatchingOrgs() {
        InMemoryOrganisationRepository repo = new InMemoryOrganisationRepository();
        repo.save(Organisation.of(1L, "TSB", "T"));
        repo.save(Organisation.of(2L, "OLOV", "O"));
        var result = repo.findAllById(Set.of(OrganisationId.of(1L)));
        assertThat(result).containsKey(OrganisationId.of(1L));
    }

    @Test
    void orgRepo_loadOrganisationTree_returnsNull() {
        InMemoryOrganisationRepository repo = new InMemoryOrganisationRepository();
        repo.save(Organisation.of(1L, "TSB", "T"));
        var result = repo.loadOrganisationTree(Set.of(OrganisationId.of(1L)));
        assertThat(result).isNull();
    }

    @Test
    void orgRepo_findAll_paged_returnsPage() {
        InMemoryOrganisationRepository repo = new InMemoryOrganisationRepository();
        repo.save(Organisation.of("TSB", "T"));
        var page = repo.findAll(null, PageRequest.of(0, 10));
        assertThat(page).isNotEmpty();
    }

    @Test
    void orgRepo_findByIds_returnsMatchingOrgs() {
        InMemoryOrganisationRepository repo = new InMemoryOrganisationRepository();
        repo.save(Organisation.of(1L, "TSB", "T"));
        repo.save(Organisation.of(2L, "OLOV", "O"));
        var result = repo.findByIds(List.of(OrganisationId.of(1L)));
        assertThat(result).hasSize(1);
    }

    @Test
    void orgRepo_saveCount_andReset() {
        InMemoryOrganisationRepository repo = new InMemoryOrganisationRepository();
        repo.save(Organisation.of("TSB", "T"));
        assertThat(repo.saveCount()).isEqualTo(1);
        repo.resetSaveCount();
        assertThat(repo.saveCount()).isEqualTo(0);
    }

    // -------------------------------------------------------------------------
    // InMemoryCourseRepository
    // -------------------------------------------------------------------------

    @Test
    void courseRepo_save_withNullId_assignsNewId() {
        InMemoryCourseRepository repo = new InMemoryCourseRepository();
        Course course = Course.of(EventId.of(1L), "Sprint", 3.0, 100.0, 10);
        Course saved = repo.save(course);
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void courseRepo_findAll_returnsAllSaved() {
        InMemoryCourseRepository repo = new InMemoryCourseRepository();
        repo.save(Course.of(EventId.of(1L), "A", 1.0, 50.0, 5));
        repo.save(Course.of(EventId.of(1L), "B", 2.0, 100.0, 10));
        assertThat(repo.findAll()).hasSize(2);
    }

    @Test
    void courseRepo_findOrCreate_findsExisting() {
        InMemoryCourseRepository repo = new InMemoryCourseRepository();
        Course course = Course.of(EventId.of(1L), "Sprint", 3.0, 100.0, 10);
        repo.save(course);
        Course found = repo.findOrCreate(Course.of(EventId.of(1L), "Sprint", 3.0, 100.0, 10));
        assertThat(found).isNotNull();
    }

    @Test
    void courseRepo_findOrCreate_collection() {
        InMemoryCourseRepository repo = new InMemoryCourseRepository();
        var results = repo.findOrCreate(List.of(Course.of(EventId.of(1L), "A", 1.0, 50.0, 5)));
        assertThat(results).hasSize(1);
    }

    @Test
    void courseRepo_saveCount_andReset() {
        InMemoryCourseRepository repo = new InMemoryCourseRepository();
        repo.save(Course.of(EventId.of(1L), "X", 1.0, 50.0, 5));
        assertThat(repo.saveCount()).isEqualTo(1);
        repo.resetSaveCount();
        assertThat(repo.saveCount()).isEqualTo(0);
    }

    // -------------------------------------------------------------------------
    // InMemoryEventRepository
    // -------------------------------------------------------------------------

    @Test
    void eventRepo_save_withNullId_assignsNewId() {
        InMemoryEventRepository repo = new InMemoryEventRepository();
        Event event = Event.of("Sprint");
        Event saved = repo.save(event);
        assertThat(saved.getId().value()).isGreaterThan(0L);
    }

    @Test
    void eventRepo_findAll_returnsAllSaved() {
        InMemoryEventRepository repo = new InMemoryEventRepository();
        repo.save(Event.of("Sprint"));
        repo.save(Event.of("Lang"));
        assertThat(repo.findAll()).hasSize(2);
    }

    @Test
    void eventRepo_findById_returnsEventWhenPresent() {
        InMemoryEventRepository repo = new InMemoryEventRepository();
        Event saved = repo.save(Event.of(1L, "Sprint"));
        assertThat(repo.findById(EventId.of(1L))).isPresent();
    }

    @Test
    void eventRepo_findById_returnsEmptyWhenAbsent() {
        InMemoryEventRepository repo = new InMemoryEventRepository();
        assertThat(repo.findById(EventId.of(99L))).isEmpty();
    }

    @Test
    void eventRepo_findOrCreate_findsExisting() {
        InMemoryEventRepository repo = new InMemoryEventRepository();
        repo.save(Event.of("Sprint"));
        Event found = repo.findOrCreate(Event.of("Sprint"));
        assertThat(found).isNotNull();
    }

    @Test
    void eventRepo_findOrCreate_createsWhenNotFound() {
        InMemoryEventRepository repo = new InMemoryEventRepository();
        Event created = repo.findOrCreate(Event.of("Neu"));
        assertThat(created.getId().value()).isGreaterThan(0L);
    }

    @Test
    void eventRepo_deleteEvent_removesIt() {
        InMemoryEventRepository repo = new InMemoryEventRepository();
        Event saved = repo.save(Event.of(1L, "Sprint"));
        repo.deleteEvent(saved);
        assertThat(repo.findById(EventId.of(1L))).isEmpty();
    }

    @Test
    void eventRepo_deleteEvent_withNullId_doesNothing() {
        InMemoryEventRepository repo = new InMemoryEventRepository();
        Event event = Event.of("Sprint");
        repo.deleteEvent(event);
        assertThat(repo.findAll()).isEmpty();
    }

    @Test
    void eventRepo_findAllById_returnsMatchingEvents() {
        InMemoryEventRepository repo = new InMemoryEventRepository();
        repo.save(Event.of(1L, "Sprint"));
        repo.save(Event.of(2L, "Lang"));
        var result = repo.findAllById(List.of(EventId.of(1L)));
        assertThat(result).hasSize(1);
    }

    @Test
    void eventRepo_findAll_paged_returnsPage() {
        InMemoryEventRepository repo = new InMemoryEventRepository();
        repo.save(Event.of("Sprint"));
        var page = repo.findAll(null, PageRequest.of(0, 10));
        assertThat(page).isNotEmpty();
    }

    @Test
    void eventRepo_saveCount_andReset() {
        InMemoryEventRepository repo = new InMemoryEventRepository();
        repo.save(Event.of("Sprint"));
        assertThat(repo.saveCount()).isEqualTo(1);
        repo.resetSaveCount();
        assertThat(repo.saveCount()).isEqualTo(0);
    }

    // -------------------------------------------------------------------------
    // InMemoryEventCertificateRepository
    // -------------------------------------------------------------------------

    private static EventCertificate cert(String name) {
        return EventCertificate.of(0L, name, null, null, null, true);
    }

    @Test
    void certRepo_save_withNullId_assignsNewId() {
        InMemoryEventCertificateRepository repo = new InMemoryEventCertificateRepository();
        EventCertificate saved = repo.save(cert("Cert A"));
        assertThat(saved.getId().value()).isGreaterThan(0L);
    }

    @Test
    void certRepo_findAll_returnsAllSaved() {
        InMemoryEventCertificateRepository repo = new InMemoryEventCertificateRepository();
        repo.save(cert("A"));
        repo.save(cert("B"));
        assertThat(repo.findAll()).hasSize(2);
    }

    @Test
    void certRepo_findById_returnsWhenPresent() {
        InMemoryEventCertificateRepository repo = new InMemoryEventCertificateRepository();
        EventCertificate saved = repo.save(cert("A"));
        assertThat(repo.findById(saved.getId())).isPresent();
    }

    @Test
    void certRepo_findById_returnsEmptyWhenAbsent() {
        InMemoryEventCertificateRepository repo = new InMemoryEventCertificateRepository();
        assertThat(repo.findById(EventCertificateId.of(99L))).isEmpty();
    }

    @Test
    void certRepo_delete_removesIt() {
        InMemoryEventCertificateRepository repo = new InMemoryEventCertificateRepository();
        EventCertificate saved = repo.save(cert("A"));
        repo.delete(saved);
        assertThat(repo.findById(saved.getId())).isEmpty();
    }

    @Test
    void certRepo_delete_withNullId_doesNothing() {
        InMemoryEventCertificateRepository repo = new InMemoryEventCertificateRepository();
        repo.delete(cert("A"));
        assertThat(repo.findAll()).isEmpty();
    }

    @Test
    void certRepo_deleteAllByEventId_removesMatching() {
        InMemoryEventCertificateRepository repo = new InMemoryEventCertificateRepository();
        EventCertificate withEvent = EventCertificate.of(0L, "A", EventId.of(1L), null, null, true);
        EventCertificate withoutEvent = cert("B");
        repo.save(withEvent);
        repo.save(withoutEvent);
        repo.deleteAllByEventId(EventId.of(1L));
        assertThat(repo.findAll()).hasSize(1);
    }

    @Test
    void certRepo_findAllByEvent_returnsMatching() {
        InMemoryEventCertificateRepository repo = new InMemoryEventCertificateRepository();
        repo.save(EventCertificate.of(0L, "A", EventId.of(1L), null, null, true));
        repo.save(cert("B"));
        assertThat(repo.findAllByEvent(EventId.of(1L))).hasSize(1);
    }

    @Test
    void certRepo_findAllByIdAsMap_returnsMatchingEntries() {
        InMemoryEventCertificateRepository repo = new InMemoryEventCertificateRepository();
        EventCertificate saved = repo.save(cert("A"));
        var result = repo.findAllByIdAsMap(Set.of(saved.getId()));
        assertThat(result).containsKey(saved.getId());
    }

    @Test
    void certRepo_findAllByIdAsMap_emptyIds_returnsEmpty() {
        InMemoryEventCertificateRepository repo = new InMemoryEventCertificateRepository();
        assertThat(repo.findAllByIdAsMap(Set.of())).isEmpty();
    }

    @Test
    void certRepo_saveAll_savesAll() {
        InMemoryEventCertificateRepository repo = new InMemoryEventCertificateRepository();
        repo.saveAll(List.of(cert("A"), cert("B")));
        assertThat(repo.findAll()).hasSize(2);
    }

    @Test
    void certRepo_findAll_paged_returnsPage() {
        InMemoryEventCertificateRepository repo = new InMemoryEventCertificateRepository();
        repo.save(cert("A"));
        var page = repo.findAll(null, PageRequest.of(0, 10));
        assertThat(page).isNotEmpty();
    }

    @Test
    void certRepo_saveCount_andReset() {
        InMemoryEventCertificateRepository repo = new InMemoryEventCertificateRepository();
        repo.save(cert("A"));
        assertThat(repo.saveCount()).isEqualTo(1);
        repo.resetSaveCount();
        assertThat(repo.saveCount()).isEqualTo(0);
    }

    // -------------------------------------------------------------------------
    // InMemoryEventCertificateStatRepository
    // -------------------------------------------------------------------------

    private static EventCertificateStat stat() {
        return EventCertificateStat.of(null, EventId.of(1L), PersonId.of(1L), Instant.now());
    }

    @Test
    void statRepo_save_withNullId_assignsNewId() {
        InMemoryEventCertificateStatRepository repo = new InMemoryEventCertificateStatRepository();
        EventCertificateStat saved = repo.save(stat());
        assertThat(saved.getId().value()).isGreaterThan(0L);
    }

    @Test
    void statRepo_findAll_returnsAllSaved() {
        InMemoryEventCertificateStatRepository repo = new InMemoryEventCertificateStatRepository();
        repo.save(stat());
        repo.save(stat());
        assertThat(repo.findAll()).hasSize(2);
    }

    @Test
    void statRepo_findById_returnsWhenPresent() {
        InMemoryEventCertificateStatRepository repo = new InMemoryEventCertificateStatRepository();
        EventCertificateStat saved = repo.save(stat());
        assertThat(repo.findById(saved.getId())).isPresent();
    }

    @Test
    void statRepo_findById_returnsEmptyWhenAbsent() {
        InMemoryEventCertificateStatRepository repo = new InMemoryEventCertificateStatRepository();
        assertThat(repo.findById(EventCertificateStatId.of(99L))).isEmpty();
    }

    @Test
    void statRepo_delete_removesIt() {
        InMemoryEventCertificateStatRepository repo = new InMemoryEventCertificateStatRepository();
        EventCertificateStat saved = repo.save(stat());
        repo.delete(saved);
        assertThat(repo.findById(saved.getId())).isEmpty();
    }

    @Test
    void statRepo_delete_withNullId_doesNothing() {
        InMemoryEventCertificateStatRepository repo = new InMemoryEventCertificateStatRepository();
        EventCertificateStat s = stat();
        repo.delete(s);
        assertThat(repo.findAll()).isEmpty();
    }

    @Test
    void statRepo_deleteAllByEventId_removesMatching() {
        InMemoryEventCertificateStatRepository repo = new InMemoryEventCertificateStatRepository();
        repo.save(EventCertificateStat.of(null, EventId.of(1L), PersonId.of(1L), Instant.now()));
        repo.save(EventCertificateStat.of(null, EventId.of(2L), PersonId.of(2L), Instant.now()));
        repo.deleteAllByEventId(EventId.of(1L));
        assertThat(repo.findAll()).hasSize(1);
    }

    @Test
    void statRepo_deleteById_removesIt() {
        InMemoryEventCertificateStatRepository repo = new InMemoryEventCertificateStatRepository();
        EventCertificateStat saved = repo.save(stat());
        repo.deleteById(saved.getId());
        assertThat(repo.findAll()).isEmpty();
    }

    @Test
    void statRepo_findAllByEvent_returnsMatching() {
        InMemoryEventCertificateStatRepository repo = new InMemoryEventCertificateStatRepository();
        repo.save(EventCertificateStat.of(null, EventId.of(1L), PersonId.of(1L), Instant.now()));
        repo.save(EventCertificateStat.of(null, EventId.of(2L), PersonId.of(2L), Instant.now()));
        assertThat(repo.findAllByEvent(EventId.of(1L))).hasSize(1);
    }

    @Test
    void statRepo_saveAll_savesAll() {
        InMemoryEventCertificateStatRepository repo = new InMemoryEventCertificateStatRepository();
        repo.saveAll(List.of(stat(), stat()));
        assertThat(repo.findAll()).hasSize(2);
    }

    @Test
    void statRepo_findAll_paged_returnsPage() {
        InMemoryEventCertificateStatRepository repo = new InMemoryEventCertificateStatRepository();
        repo.save(stat());
        var page = repo.findAll(null, PageRequest.of(0, 10));
        assertThat(page).isNotEmpty();
    }

    @Test
    void statRepo_saveCount_andReset() {
        InMemoryEventCertificateStatRepository repo = new InMemoryEventCertificateStatRepository();
        repo.save(stat());
        assertThat(repo.saveCount()).isEqualTo(1);
        repo.resetSaveCount();
        assertThat(repo.saveCount()).isEqualTo(0);
    }

    // -------------------------------------------------------------------------
    // InMemoryCupRepository
    // -------------------------------------------------------------------------

    private static Cup cup(String name) {
        return Cup.of(null, name, CupType.ADD, Year.of(2024), List.of());
    }

    @Test
    void cupRepo_save_withNullId_assignsNewId() {
        InMemoryCupRepository repo = new InMemoryCupRepository();
        Cup saved = repo.save(cup("MyCup"));
        assertThat(saved.getId().value()).isGreaterThan(0L);
    }

    @Test
    void cupRepo_findAll_returnsAllSaved() {
        InMemoryCupRepository repo = new InMemoryCupRepository();
        repo.save(cup("A"));
        repo.save(cup("B"));
        assertThat(repo.findAll()).hasSize(2);
    }

    @Test
    void cupRepo_findById_returnsWhenPresent() {
        InMemoryCupRepository repo = new InMemoryCupRepository();
        Cup saved = repo.save(cup("A"));
        assertThat(repo.findById(saved.getId())).isPresent();
    }

    @Test
    void cupRepo_findById_returnsEmptyWhenAbsent() {
        InMemoryCupRepository repo = new InMemoryCupRepository();
        assertThat(repo.findById(CupId.of(99L))).isEmpty();
    }

    @Test
    void cupRepo_findOrCreate_findsExisting() {
        InMemoryCupRepository repo = new InMemoryCupRepository();
        repo.save(cup("MyCup"));
        Cup found = repo.findOrCreate(cup("MyCup"));
        assertThat(found).isNotNull();
    }

    @Test
    void cupRepo_findOrCreate_createsWhenNotFound() {
        InMemoryCupRepository repo = new InMemoryCupRepository();
        Cup created = repo.findOrCreate(cup("Neu"));
        assertThat(created.getId().value()).isGreaterThan(0L);
    }

    @Test
    void cupRepo_deleteCup_removesIt() {
        InMemoryCupRepository repo = new InMemoryCupRepository();
        Cup saved = repo.save(cup("A"));
        repo.deleteCup(saved);
        assertThat(repo.findById(saved.getId())).isEmpty();
    }

    @Test
    void cupRepo_deleteCup_withNullId_doesNothing() {
        InMemoryCupRepository repo = new InMemoryCupRepository();
        repo.deleteCup(cup("A"));
        assertThat(repo.findAll()).isEmpty();
    }

    @Test
    void cupRepo_findByEvent_returnsMatchingCups() {
        InMemoryCupRepository repo = new InMemoryCupRepository();
        repo.save(Cup.of(null, "A", CupType.ADD, Year.of(2024), List.of(EventId.of(1L))));
        repo.save(Cup.of(null, "B", CupType.ADD, Year.of(2024), List.of(EventId.of(2L))));
        Collection<Cup> result = repo.findByEvent(EventId.of(1L));
        assertThat(result).hasSize(1);
    }

    @Test
    void cupRepo_findAll_paged_returnsPage() {
        InMemoryCupRepository repo = new InMemoryCupRepository();
        repo.save(cup("A"));
        var page = repo.findAll(null, PageRequest.of(0, 10));
        assertThat(page).isNotEmpty();
    }

    @Test
    void cupRepo_saveCount_andReset() {
        InMemoryCupRepository repo = new InMemoryCupRepository();
        repo.save(cup("A"));
        assertThat(repo.saveCount()).isEqualTo(1);
        repo.resetSaveCount();
        assertThat(repo.saveCount()).isEqualTo(0);
    }

    // -------------------------------------------------------------------------
    // InMemoryMediaFileRepository
    // -------------------------------------------------------------------------

    private static MediaFile mediaFile(String name) {
        return MediaFile.of(name, "thumb.jpg", "application/pdf", 1000L);
    }

    @Test
    void mediaRepo_save_withNullId_assignsNewId() {
        InMemoryMediaFileRepository repo = new InMemoryMediaFileRepository();
        MediaFile saved = repo.save(mediaFile("doc.pdf"));
        assertThat(saved.getId().value()).isGreaterThan(0L);
    }

    @Test
    void mediaRepo_findAll_returnsAllSaved() {
        InMemoryMediaFileRepository repo = new InMemoryMediaFileRepository();
        repo.save(mediaFile("a.pdf"));
        repo.save(mediaFile("b.pdf"));
        assertThat(repo.findAll()).hasSize(2);
    }

    @Test
    void mediaRepo_findById_returnsWhenPresent() {
        InMemoryMediaFileRepository repo = new InMemoryMediaFileRepository();
        MediaFile saved = repo.save(mediaFile("a.pdf"));
        assertThat(repo.findById(saved.getId())).isPresent();
    }

    @Test
    void mediaRepo_findById_returnsEmptyWhenAbsent() {
        InMemoryMediaFileRepository repo = new InMemoryMediaFileRepository();
        assertThat(repo.findById(MediaFileId.of(99L))).isEmpty();
    }

    @Test
    void mediaRepo_delete_removesIt() {
        InMemoryMediaFileRepository repo = new InMemoryMediaFileRepository();
        MediaFile saved = repo.save(mediaFile("a.pdf"));
        repo.delete(saved.getId());
        assertThat(repo.findById(saved.getId())).isEmpty();
    }

    @Test
    void mediaRepo_delete_withNullId_doesNothing() {
        InMemoryMediaFileRepository repo = new InMemoryMediaFileRepository();
        repo.delete(MediaFileId.empty());
        assertThat(repo.findAll()).isEmpty();
    }

    @Test
    void mediaRepo_findAllById_returnsMatchingFiles() {
        InMemoryMediaFileRepository repo = new InMemoryMediaFileRepository();
        MediaFile saved = repo.save(mediaFile("a.pdf"));
        repo.save(mediaFile("b.pdf"));
        var result = repo.findAllById(List.of(saved.getId()));
        assertThat(result).hasSize(1);
    }

    @Test
    void mediaRepo_findAll_paged_returnsPage() {
        InMemoryMediaFileRepository repo = new InMemoryMediaFileRepository();
        repo.save(mediaFile("a.pdf"));
        var page = repo.findAll(null, PageRequest.of(0, 10));
        assertThat(page).isNotEmpty();
    }

    @Test
    void mediaRepo_saveCount_andReset() {
        InMemoryMediaFileRepository repo = new InMemoryMediaFileRepository();
        repo.save(mediaFile("a.pdf"));
        assertThat(repo.saveCount()).isEqualTo(1);
        repo.resetSaveCount();
        assertThat(repo.saveCount()).isEqualTo(0);
    }

    // -------------------------------------------------------------------------
    // InMemoryResultListRepository
    // -------------------------------------------------------------------------

    private static ResultList resultList(long eventId) {
        return new ResultList(ResultListId.empty(), EventId.of(eventId), RaceId.empty(), null, null, null, null);
    }

    @Test
    void resultListRepo_save_withNullId_assignsNewId() {
        InMemoryResultListRepository repo = new InMemoryResultListRepository();
        ResultList saved = repo.save(resultList(1L));
        assertThat(saved.getId().value()).isGreaterThan(0L);
    }

    @Test
    void resultListRepo_findAll_returnsAllSaved() {
        InMemoryResultListRepository repo = new InMemoryResultListRepository();
        repo.save(resultList(1L));
        repo.save(resultList(2L));
        assertThat(repo.findAll()).hasSize(2);
    }

    @Test
    void resultListRepo_findById_returnsWhenPresent() {
        InMemoryResultListRepository repo = new InMemoryResultListRepository();
        ResultList saved = repo.save(resultList(1L));
        assertThat(repo.findById(saved.getId())).isPresent();
    }

    @Test
    void resultListRepo_findById_returnsEmptyWhenAbsent() {
        InMemoryResultListRepository repo = new InMemoryResultListRepository();
        assertThat(repo.findById(ResultListId.of(99L))).isEmpty();
    }

    @Test
    void resultListRepo_findOrCreate_findsExisting() {
        InMemoryResultListRepository repo = new InMemoryResultListRepository();
        repo.save(resultList(1L));
        ResultList found = repo.findOrCreate(resultList(1L));
        assertThat(found).isNotNull();
    }

    @Test
    void resultListRepo_findOrCreate_collection() {
        InMemoryResultListRepository repo = new InMemoryResultListRepository();
        var results = repo.findOrCreate(List.of(resultList(1L)));
        assertThat(results).hasSize(1);
    }

    @Test
    void resultListRepo_update_saves() {
        InMemoryResultListRepository repo = new InMemoryResultListRepository();
        ResultList saved = repo.save(resultList(1L));
        ResultList updated = repo.update(saved);
        assertThat(updated).isNotNull();
    }

    @Test
    void resultListRepo_findByEventId_returnsMatching() {
        InMemoryResultListRepository repo = new InMemoryResultListRepository();
        repo.save(resultList(1L));
        repo.save(resultList(2L));
        assertThat(repo.findByEventId(EventId.of(1L))).hasSize(1);
    }

    @Test
    void resultListRepo_findAllByEventIds_returnsMatching() {
        InMemoryResultListRepository repo = new InMemoryResultListRepository();
        repo.save(resultList(1L));
        repo.save(resultList(2L));
        assertThat(repo.findAllByEventIds(List.of(EventId.of(1L)))).hasSize(1);
    }

    @Test
    void resultListRepo_findAllByEventIds_emptyList_returnsEmpty() {
        InMemoryResultListRepository repo = new InMemoryResultListRepository();
        assertThat(repo.findAllByEventIds(List.of())).isEmpty();
    }

    @Test
    void resultListRepo_saveCount_andReset() {
        InMemoryResultListRepository repo = new InMemoryResultListRepository();
        repo.save(resultList(1L));
        assertThat(repo.saveCount()).isEqualTo(1);
        repo.resetSaveCount();
        assertThat(repo.saveCount()).isEqualTo(0);
    }

    @Test
    void resultListRepo_findByResultListIdAndClassResultShortNameAndPersonId_returnsNullWhenIdNotFound() {
        InMemoryResultListRepository repo = new InMemoryResultListRepository();
        repo.save(resultList(1L));
        var result = repo.findByResultListIdAndClassResultShortNameAndPersonId(
                ResultListId.of(99L), ClassResultShortName.of("D10"), PersonId.of(1L));
        assertThat(result).isNull();
    }

    @Test
    void resultListRepo_replacePersonId_throwsUnsupported() {
        InMemoryResultListRepository repo = new InMemoryResultListRepository();
        assertThatThrownBy(
                () -> repo.replacePersonId(PersonId.of(1L), PersonId.of(2L)))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void resultListRepo_findClassShortNamesByEventId_throwsUnsupported() {
        InMemoryResultListRepository repo = new InMemoryResultListRepository();
        assertThatThrownBy(
                () -> repo.findClassShortNamesByEventId(EventId.of(1L)))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void resultListRepo_countNonZeroRacesByEventId_throwsUnsupported() {
        InMemoryResultListRepository repo = new InMemoryResultListRepository();
        assertThatThrownBy(
                () -> repo.countNonZeroRacesByEventId(EventId.of(1L)))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    // -------------------------------------------------------------------------
    // InMemoryPersonRepository
    // -------------------------------------------------------------------------

    private static Person person(String family, String given) {
        return Person.of(family, given, null, Gender.M);
    }

    @Test
    void personRepo_save_withNullId_assignsNewId() {
        InMemoryPersonRepository repo = new InMemoryPersonRepository();
        Person saved = repo.save(person("Müller", "Hans"));
        assertThat(saved.id().value()).isGreaterThan(0L);
    }

    @Test
    void personRepo_findAll_returnsAllSaved() {
        InMemoryPersonRepository repo = new InMemoryPersonRepository();
        repo.save(person("Müller", "Hans"));
        repo.save(person("Schmidt", "Klaus"));
        assertThat(repo.findAll()).hasSize(2);
    }

    @Test
    void personRepo_findById_returnsWhenPresent() {
        InMemoryPersonRepository repo = new InMemoryPersonRepository();
        Person saved = repo.save(person("Müller", "Hans"));
        assertThat(repo.findById(saved.id())).isPresent();
    }

    @Test
    void personRepo_findById_returnsEmptyWhenAbsent() {
        InMemoryPersonRepository repo = new InMemoryPersonRepository();
        assertThat(repo.findById(PersonId.of(99L))).isEmpty();
    }

    @Test
    void personRepo_findOrCreate_findsExisting() {
        InMemoryPersonRepository repo = new InMemoryPersonRepository();
        repo.save(person("Müller", "Hans"));
        var result = repo.findOrCreate(person("Müller", "Hans"));
        assertThat(result.target()).isNotNull();
    }

    @Test
    void personRepo_findOrCreate_createsWhenNotFound() {
        InMemoryPersonRepository repo = new InMemoryPersonRepository();
        var result = repo.findOrCreate(person("Neu", "Person"));
        assertThat(result.target().id().value()).isGreaterThan(0L);
    }

    @Test
    void personRepo_findOrCreate_collection() {
        InMemoryPersonRepository repo = new InMemoryPersonRepository();
        var results = repo.findOrCreate(List.of(person("A", "B"), person("C", "D")));
        assertThat(results).hasSize(2);
    }

    @Test
    void personRepo_delete_removesIt() {
        InMemoryPersonRepository repo = new InMemoryPersonRepository();
        Person saved = repo.save(person("Müller", "Hans"));
        repo.delete(saved);
        assertThat(repo.findById(saved.id())).isEmpty();
    }

    @Test
    void personRepo_findAll_paged_returnsPage() {
        InMemoryPersonRepository repo = new InMemoryPersonRepository();
        repo.save(person("Müller", "Hans"));
        var page = repo.findAll(null, PageRequest.of(0, 10));
        assertThat(page).isNotEmpty();
    }

    @Test
    void personRepo_findDuplicates_returnsMatchingPage() {
        InMemoryPersonRepository repo = new InMemoryPersonRepository();
        repo.save(person("Müller", "Hans"));
        repo.save(person("Müller", "Hans"));
        var page = repo.findDuplicates(null, PageRequest.of(0, 10));
        assertThat(page.getTotalElements()).isGreaterThan(0L);
    }

    @Test
    void personRepo_findAllById_returnsMatchingPersons() {
        InMemoryPersonRepository repo = new InMemoryPersonRepository();
        Person saved = repo.save(person("Müller", "Hans"));
        repo.save(person("Schmidt", "Klaus"));
        var result = repo.findAllById(Set.of(saved.id()));
        assertThat(result).containsKey(saved.id());
    }

    @Test
    void personRepo_saveCount_andReset() {
        InMemoryPersonRepository repo = new InMemoryPersonRepository();
        repo.save(person("Müller", "Hans"));
        assertThat(repo.saveCount()).isEqualTo(1);
        repo.resetSaveCount();
        assertThat(repo.saveCount()).isEqualTo(0);
    }

    // -------------------------------------------------------------------------
    // InMemorySplitTimeListRepository
    // -------------------------------------------------------------------------

    private static SplitTimeList splitTimeList(long eventId, long resultListId, long personId) {
        return new SplitTimeList(
                SplitTimeListId.empty(),
                EventId.of(eventId),
                ResultListId.of(resultListId),
                ClassResultShortName.of("D10"),
                PersonId.of(personId),
                RaceNumber.of((byte) 1),
                List.of());
    }

    @Test
    void splitTimeRepo_save_withNullId_assignsNewId() {
        InMemorySplitTimeListRepository repo = new InMemorySplitTimeListRepository();
        SplitTimeList saved = repo.save(splitTimeList(1L, 1L, 1L));
        assertThat(saved.getId().value()).isGreaterThan(0L);
    }

    @Test
    void splitTimeRepo_findAll_returnsAllSaved() {
        InMemorySplitTimeListRepository repo = new InMemorySplitTimeListRepository();
        repo.save(splitTimeList(1L, 1L, 1L));
        repo.save(splitTimeList(1L, 1L, 2L));
        assertThat(repo.findAll()).hasSize(2);
    }

    @Test
    void splitTimeRepo_findById_returnsWhenPresent() {
        InMemorySplitTimeListRepository repo = new InMemorySplitTimeListRepository();
        SplitTimeList saved = repo.save(splitTimeList(1L, 1L, 1L));
        assertThat(repo.findById(saved.getId())).isPresent();
    }

    @Test
    void splitTimeRepo_findById_returnsEmptyWhenAbsent() {
        InMemorySplitTimeListRepository repo = new InMemorySplitTimeListRepository();
        assertThat(repo.findById(SplitTimeListId.of(99L))).isEmpty();
    }

    @Test
    void splitTimeRepo_findOrCreate_findsExisting() {
        InMemorySplitTimeListRepository repo = new InMemorySplitTimeListRepository();
        repo.save(splitTimeList(1L, 1L, 1L));
        SplitTimeList found = repo.findOrCreate(splitTimeList(1L, 1L, 1L));
        assertThat(found).isNotNull();
    }

    @Test
    void splitTimeRepo_findOrCreate_collection() {
        InMemorySplitTimeListRepository repo = new InMemorySplitTimeListRepository();
        var results = repo.findOrCreate(List.of(splitTimeList(1L, 1L, 1L)));
        assertThat(results).hasSize(1);
    }

    @Test
    void splitTimeRepo_findByResultListId_returnsMatching() {
        InMemorySplitTimeListRepository repo = new InMemorySplitTimeListRepository();
        repo.save(splitTimeList(1L, 1L, 1L));
        repo.save(splitTimeList(1L, 2L, 2L));
        assertThat(repo.findByResultListId(ResultListId.of(1L))).hasSize(1);
    }

    @Test
    void splitTimeRepo_existsByResultListIds_returnsMatchingIds() {
        InMemorySplitTimeListRepository repo = new InMemorySplitTimeListRepository();
        repo.save(splitTimeList(1L, 1L, 1L));
        var result = repo.existsByResultListIds(List.of(ResultListId.of(1L), ResultListId.of(99L)));
        assertThat(result).containsExactly(ResultListId.of(1L));
    }

    @Test
    void splitTimeRepo_existsByResultListIds_emptyInput_returnsEmpty() {
        InMemorySplitTimeListRepository repo = new InMemorySplitTimeListRepository();
        assertThat(repo.existsByResultListIds(List.of())).isEmpty();
    }

    @Test
    void splitTimeRepo_saveCount_andReset() {
        InMemorySplitTimeListRepository repo = new InMemorySplitTimeListRepository();
        repo.save(splitTimeList(1L, 1L, 1L));
        assertThat(repo.saveCount()).isEqualTo(1);
        repo.resetSaveCount();
        assertThat(repo.saveCount()).isEqualTo(0);
    }

    @Test
    void splitTimeRepo_replacePersonId_throwsUnsupported() {
        InMemorySplitTimeListRepository repo = new InMemorySplitTimeListRepository();
        assertThatThrownBy(() -> repo.replacePersonId(PersonId.of(1L), PersonId.of(2L)))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
