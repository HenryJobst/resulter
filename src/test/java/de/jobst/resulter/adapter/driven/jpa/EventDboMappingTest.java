package de.jobst.resulter.adapter.driven.jpa;

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

        Event event = EventDbo.asEvents(EventConfig.full(), List.of(eventDbo)).getFirst();

        assertThat(Objects.requireNonNull(event.getId()).value()).isEqualTo(EventTestDataGenerator.A_EVENT_ID);
        assertThat(event.getName().value()).isEqualTo(EventTestDataGenerator.A_EVENT_NAME);
        assertThat(event.getStartTime()).isNotNull();
        assertThat(event.getStartTime().value()).isNull();
        assertThat(event.getOrganisations().isLoaded()).isTrue();
        assertThat(event.getOrganisations().get().value()).isEmpty();
        assertThat(event.getClassResults().isLoaded()).isTrue();
        assertThat(event.getClassResults().get().value()).isEmpty();
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

        if (event.getClassResults().isLoaded()) {
            assertThat(entity.getClassResults()).isNotEmpty();
            assertThat(entity.getClassResults()).hasSize(event.getClassResults().get().value().size());

            ClassResult classResult = event.getClassResults().get().value().stream().findFirst().orElse(null);
            ClassResultDbo classResultDbo = entity.getClassResults().stream().findFirst().orElse(null);

            if (ObjectUtils.isNotEmpty(classResult)) {
                assertThat(classResultDbo).isNotNull();
                assertThat(classResultDbo.getName()).isEqualTo(classResult.getClassResultName().value());
                assertThat(classResultDbo.getShortName()).isEqualTo(classResult.getClassResultShortName().value());
                assertThat(classResultDbo.getGender()).isEqualTo(classResult.getGender());

                if (classResult.getPersonResults().isLoaded()) {
                    assertThat(classResultDbo.getPersonResults()).isNotEmpty();
                    assertThat(classResultDbo.getPersonResults()).hasSize(classResult.getPersonResults()
                            .get()
                            .value()
                            .size());

                    PersonResult
                            personResult =
                            classResult.getPersonResults().get().value().stream().findFirst().orElse(null);
                    PersonResultDbo
                            personResultDbo =
                            classResultDbo.getPersonResults().stream().findFirst().orElse(null);

                    if (ObjectUtils.isNotEmpty(personResult)) {
                        assertThat(personResultDbo).isNotNull();

                        if (personResult.getPerson().isLoaded()) {
                            assertThat(personResultDbo).isNotNull();
                            assertThat(personResultDbo.getPerson()).isNotNull();

                            Person person = personResult.getPerson().get();
                            PersonDbo personDbo = personResultDbo.getPerson();

                            assertThat(personDbo.getFamilyName()).isEqualTo(person.getPersonName()
                                    .familyName()
                                    .value());
                            assertThat(personDbo.getGivenName()).isEqualTo(person.getPersonName().givenName().value());
                            assertThat(personDbo.getBirthDate()).isEqualTo(person.getBirthDate().value());
                        } else {
                            assertThat(personResultDbo.getPerson()).isNull();
                        }

                        if (personResult.getOrganisation().isLoaded()) {
                            assertThat(personResultDbo).isNotNull();
                            assertThat(personResultDbo.getOrganisation()).isNotNull();

                            Organisation organisation = personResult.getOrganisation().get();
                            OrganisationDbo organisationDbo = personResultDbo.getOrganisation();

                            assertThat(organisationDbo.getName()).isEqualTo(organisation.getName().value());
                            assertThat(organisationDbo.getShortName()).isEqualTo(organisation.getShortName().value());
                            assertThat(organisationDbo.getCountry()).isNotNull();
                            assertThat(organisationDbo.getCountry().getName()).isEqualTo(organisation.getCountry()
                                    .get()
                                    .getName()
                                    .value());
                            assertThat(organisationDbo.getCountry().getCode()).isEqualTo(organisation.getCountry()
                                    .get()
                                    .getCode()
                                    .value());

                        } else {
                            assertThat(personResultDbo.getOrganisation()).isNull();
                        }

                        if (personResult.getPersonRaceResults().isLoaded()) {
                            assertThat(personResultDbo.getPersonRaceResults()).isNotEmpty();
                            assertThat(personResultDbo.getPersonRaceResults()).hasSize(personResult.getPersonRaceResults()
                                    .get()
                                    .value()
                                    .size());

                            PersonRaceResult
                                    personRaceResult =
                                    personResult.getPersonRaceResults().get().value().stream().findFirst().orElse(null);
                            PersonRaceResultDbo
                                    personRaceResultDbo =
                                    personResultDbo.getPersonRaceResults().stream().findFirst().orElse(null);

                            if (ObjectUtils.isNotEmpty(personRaceResult)) {
                                assertThat(personRaceResultDbo).isNotNull();

                                assertThat(personRaceResultDbo.getRaceNumber()).isEqualTo(personRaceResult.getRaceNumber()
                                        .value());
                                assertThat(personRaceResultDbo.getStartTime()).isEqualTo(personRaceResult.getStartTime()
                                        .value());
                                assertThat(personRaceResultDbo.getFinishTime()).isEqualTo(personRaceResult.getFinishTime()
                                        .value());
                                assertThat(personRaceResultDbo.getPunchTime()).isEqualTo(personRaceResult.getRuntime()
                                        .value());
                                assertThat(personRaceResultDbo.getPosition()).isEqualTo(personRaceResult.getPosition()
                                        .value());
                                assertThat(personRaceResultDbo.getState()).isEqualTo(personRaceResult.getState());

                                if (personRaceResult.getSplitTimes().isLoaded()) {
                                    assertThat(personRaceResultDbo.getSplitTimes()).isNotEmpty();
                                    assertThat(personRaceResultDbo.getSplitTimes()).hasSize(personRaceResult.getSplitTimes()
                                            .get()
                                            .value()
                                            .size());

                                    SplitTime
                                            splitTime =
                                            personRaceResult.getSplitTimes()
                                                    .get()
                                                    .value()
                                                    .stream()
                                                    .findFirst()
                                                    .orElse(null);
                                    if (ObjectUtils.isNotEmpty(splitTime)) {
                                        SplitTimeDbo
                                                splitTimeDbo =
                                                personRaceResultDbo.getSplitTimes().stream()
                                                        .filter(x -> x.getControlCode()
                                                                .equals(splitTime.getControlCode().value()))
                                                        .findFirst().orElse(null);
                                        assertThat(splitTimeDbo).isNotNull();

                                        assertThat(splitTimeDbo.getControlCode()).isEqualTo(splitTime.getControlCode()
                                                .value());
                                        assertThat(splitTimeDbo.getPunchTime()).isEqualTo(splitTime.getPunchTime()
                                                .value());
                                    }
                                } else {
                                    assertThat(personRaceResultDbo.getSplitTimes()).isEmpty();
                                }
                            }
                        } else {
                            assertThat(personResultDbo.getPersonRaceResults()).isNull();
                        }
                    } else {
                        assertThat(personResultDbo).isNull();
                    }

                } else {
                    assertThat(classResultDbo.getPersonResults()).isNull();
                }
            } else {
                assertThat(classResultDbo).isNull();
            }

        } else {
            assertThat(entity.getClassResults()).isNull();
        }

    }

}
