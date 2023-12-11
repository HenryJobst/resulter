package de.jobst.resulter.adapter.driven.jpa;

import de.jobst.resulter.domain.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

class EventDboMappingTest {

    @Test
    public void databaseEntityToDomainIsMappedCorrectly() {
        EventDbo eventDbo = new EventDbo();
        eventDbo.setId(19L);
        eventDbo.setName("Entity");

        Event event = eventDbo.asEvent();

        assertThat(Objects.requireNonNull(event.getId()).value()).isEqualTo(19L);
        assertThat(event.getName().value())
                .isEqualTo("Entity");
    }

    @Test
    public void domainToDatabaseEntityIsMappedCorrectly() {
        Collection<PersonRaceResult> personRaceResults = new ArrayList<>();
        Collection<PersonResult> personResults = new ArrayList<>();
        personResults.add(PersonResult.of(
                Person.of(
                        FamilyName.of("Knopf"), GivenName.of("Jim"),
                        LocalDate.of(2000, 11, 1),
                        Gender.M),
                Organisation.of("O-Club", "OC"),
                personRaceResults));

        Collection<ClassResult> classResults = new ArrayList<>();
        classResults.add(ClassResult.of("H50- (Herren ab 50)", "H50-",
                Gender.M, personResults));

        Event event = Event.of("Domain", classResults);

        EventDbo entity = EventDbo.from(event);

        assertThat(entity.getName())
                .isEqualTo("Domain");
        assertThat(entity.getClassResults()).hasSize(1);
    }

}