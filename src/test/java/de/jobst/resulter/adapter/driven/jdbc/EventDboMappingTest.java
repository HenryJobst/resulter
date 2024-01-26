package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.domain.Event;
import de.jobst.resulter.domain.EventTestDataGenerator;
import de.jobst.resulter.domain.TestEventResult;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled
class EventDboMappingTest {

    @Test
    public void databaseEntityToDomainIsMappedCorrectly() {
        EventDbo eventDbo = new EventDbo(EventTestDataGenerator.A_EVENT_NAME);

        Event event = EventDbo.asEvents(List.of(eventDbo)).getFirst();

        assertThat(event.getId().value()).isNull();
        assertThat(event.getName().value()).isEqualTo(EventTestDataGenerator.A_EVENT_NAME);
        assertThat(event.getStartTime()).isNotNull();
        assertThat(event.getStartTime().value()).isNull();
        assertThat(event.getOrganisationIds()).isNotNull();
        assertThat(event.getOrganisationIds()).isEmpty();
        assertThat(event.getResultListIds()).isEmpty();
        assertThat(event.getEventState()).isNull();
    }

    @Test
    public void domainToDatabaseEntityIsMappedCorrectly() {
        TestEventResult testEventResult = EventTestDataGenerator.getTestEvent();
        Event event = testEventResult.event();

        DboResolvers dboResolvers = DboResolvers.empty();
        EventDbo entity = EventDbo.from(event, dboResolvers);

        assertThat(entity.getName()).isEqualTo(event.getName().value());
        assertThat(entity.getStartTime()).isEqualTo(event.getStartTime().value());
        assertThat(entity.getState()).isEqualTo(event.getEventState());

        assertThat(entity.getResultLists()).isNotEmpty();
        assertThat(entity.getResultLists()).hasSize(event.getResultListIds().size());

        /* TODO Transform test to new domain and dbo structure
        ResultListId resultListId = event.getResultListIds().stream().findFirst().orElse(null);
        ClassResultDbo classResultDbo = entity.getClassResults().stream().findFirst().orElse(null);

        if (ObjectUtils.isNotEmpty(classResult)) {
            assertThat(classResultDbo).isNotNull();
            assertThat(classResultDbo.getName()).isEqualTo(classResult.classResultName().value());
            assertThat(classResultDbo.getShortName()).isEqualTo(classResult.classResultShortName().value());
            assertThat(classResultDbo.getGender()).isEqualTo(classResult.gender());

            assertThat(classResultDbo.getPersonResults()).isNotEmpty();
            assertThat(classResultDbo.getPersonResults()).hasSize(classResult.personResults().value().size());

            PersonResult personResult = classResult.personResults().value().stream().findFirst().orElse(null);
            PersonResultDbo personResultDbo = classResultDbo.getPersonResults().stream().findFirst().orElse(null);

            if (ObjectUtils.isNotEmpty(personResult)) {
                assertThat(personResultDbo).isNotNull();

                if (personResult.personId() != null) {
                    assertThat(personResultDbo).isNotNull();
                    assertThat(personResultDbo.getPerson()).isNotNull();
                            Person person = personResult.getPersonId();
                            PersonDbo personDbo = personResultDbo.getPerson();

                            assertThat(personDbo.getFamilyName()).isEqualTo(person.getPersonName()
                                .familyName()
                                .value());
                            assertThat(personDbo.getGivenName()).isEqualTo(person.getPersonName().givenName().value());
                            assertThat(personDbo.getBirthDate()).isEqualTo(person.getBirthDate().value());
                } else {
                    assertThat(personResultDbo.getPerson()).isNull();
                }

                if (personResult.organisationId() != null) {
                    assertThat(personResultDbo.getOrganisation()).isNotNull();
                } else {
                    assertThat(personResultDbo.getOrganisation()).isNull();
                }

                assertThat(personResultDbo.getPersonRaceResults()).isNotEmpty();
                assertThat(personResultDbo.getPersonRaceResults()).hasSize(personResult.personRaceResults()
                    .value()
                    .size());

                PersonRaceResult personRaceResult =
                    personResult.personRaceResults().value().stream().findFirst().orElse(null);
                PersonRaceResultDbo personRaceResultDbo =
                    personResultDbo.getPersonRaceResults().stream().findFirst().orElse(null);

                if (ObjectUtils.isNotEmpty(personRaceResult)) {
                    assertThat(personRaceResultDbo).isNotNull();

                    assertThat(personRaceResultDbo.getRaceNumber()).isEqualTo(personRaceResult.raceNumber().value());
                    assertThat(personRaceResultDbo.getStartTime()).isEqualTo(personRaceResult.startTime().value());
                    assertThat(personRaceResultDbo.getFinishTime()).isEqualTo(personRaceResult.finishTime().value());
                    assertThat(personRaceResultDbo.getPunchTime()).isEqualTo(personRaceResult.runtime().value());
                    assertThat(personRaceResultDbo.getPosition()).isEqualTo(personRaceResult.position().value());
                    assertThat(personRaceResultDbo.getState()).isEqualTo(personRaceResult.state());

                }
            } else {
                assertThat(personResultDbo.getPersonRaceResults()).isNull();
            }
        } else {
            assertThat(classResultDbo).isNull();
        }

         */
    }
}
