package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.domain.*;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

class EventDboMappingTest {

    @Test
    public void databaseEntityToDomainIsMappedCorrectly() {
        EventDbo eventDbo = new EventDbo();
        eventDbo.setId(EventTestDataGenerator.A_EVENT_ID);
        eventDbo.setName(EventTestDataGenerator.A_EVENT_NAME);

        Event event = EventDbo.asEvents(List.of(eventDbo)).getFirst();

        assertThat(Objects.requireNonNull(event.getId()).value()).isEqualTo(EventTestDataGenerator.A_EVENT_ID);
        assertThat(event.getName().value()).isEqualTo(EventTestDataGenerator.A_EVENT_NAME);
        assertThat(event.getStartTime()).isNotNull();
        assertThat(event.getStartTime().value()).isNull();
        assertThat(event.getOrganisationIds()).isNotNull();
        assertThat(event.getOrganisationIds()).isEmpty();
        assertThat(event.getClassResults().value()).isEmpty();
        assertThat(event.getEventState()).isNull();
    }

    @Test
    public void domainToDatabaseEntityIsMappedCorrectly() {
        Event event = EventTestDataGenerator.getTestEvent();

        DboResolvers dboResolvers = DboResolvers.empty();
        EventDbo entity = EventDbo.from(event, null, dboResolvers);

        assertThat(entity.getName()).isEqualTo(event.getName().value());
        assertThat(entity.getStartTime()).isEqualTo(event.getStartTime().value());
        assertThat(entity.getState()).isEqualTo(event.getEventState());

        assertThat(entity.getClassResults()).isNotEmpty();
        assertThat(entity.getClassResults()).hasSize(event.getClassResults().value().size());

        ClassResult classResult = event.getClassResults().value().stream().findFirst().orElse(null);
        ClassResultDbo classResultDbo = entity.getClassResults().stream().findFirst().orElse(null);

        if (ObjectUtils.isNotEmpty(classResult)) {
            assertThat(classResultDbo).isNotNull();
            assertThat(classResultDbo.getName()).isEqualTo(classResult.getClassResultName().value());
            assertThat(classResultDbo.getShortName()).isEqualTo(classResult.getClassResultShortName().value());
            assertThat(classResultDbo.getGender()).isEqualTo(classResult.getGender());

            assertThat(classResultDbo.getPersonResults()).isNotEmpty();
            assertThat(classResultDbo.getPersonResults()).hasSize(classResult.getPersonResults().value().size());

            PersonResult personResult = classResult.getPersonResults().value().stream().findFirst().orElse(null);
            PersonResultDbo personResultDbo = classResultDbo.getPersonResults().stream().findFirst().orElse(null);

            if (ObjectUtils.isNotEmpty(personResult)) {
                assertThat(personResultDbo).isNotNull();

                if (personResult.getPersonId() != null) {
                    assertThat(personResultDbo).isNotNull();
                    assertThat(personResultDbo.getPerson()).isNotNull();
                            /*
                            Person person = personResult.getPersonId();
                            PersonDbo personDbo = personResultDbo.getPerson();

                            assertThat(personDbo.getFamilyName()).isEqualTo(person.getPersonName()
                                .familyName()
                                .value());
                            assertThat(personDbo.getGivenName()).isEqualTo(person.getPersonName().givenName().value());
                            assertThat(personDbo.getBirthDate()).isEqualTo(person.getBirthDate().value());
                            */
                } else {
                    assertThat(personResultDbo.getPerson()).isNull();
                }

                if (personResult.getOrganisationId() != null) {
                    assertThat(personResultDbo.getOrganisation()).isNotNull();
                } else {
                    assertThat(personResultDbo.getOrganisation()).isNull();
                }

                assertThat(personResultDbo.getPersonRaceResults()).isNotEmpty();
                assertThat(personResultDbo.getPersonRaceResults()).hasSize(personResult.getPersonRaceResults()
                    .value()
                    .size());

                PersonRaceResult personRaceResult =
                    personResult.getPersonRaceResults().value().stream().findFirst().orElse(null);
                PersonRaceResultDbo personRaceResultDbo =
                    personResultDbo.getPersonRaceResults().stream().findFirst().orElse(null);

                if (ObjectUtils.isNotEmpty(personRaceResult)) {
                    assertThat(personRaceResultDbo).isNotNull();

                    assertThat(personRaceResultDbo.getRaceNumber()).isEqualTo(personRaceResult.getRaceNumber().value());
                    assertThat(personRaceResultDbo.getStartTime()).isEqualTo(personRaceResult.getStartTime().value());
                    assertThat(personRaceResultDbo.getFinishTime()).isEqualTo(personRaceResult.getFinishTime().value());
                    assertThat(personRaceResultDbo.getPunchTime()).isEqualTo(personRaceResult.getRuntime().value());
                    assertThat(personRaceResultDbo.getPosition()).isEqualTo(personRaceResult.getPosition().value());
                    assertThat(personRaceResultDbo.getState()).isEqualTo(personRaceResult.getState());

                }
            } else {
                assertThat(personResultDbo.getPersonRaceResults()).isNull();
            }
        } else {
            assertThat(classResultDbo).isNull();
        }
    }
}
