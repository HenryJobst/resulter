package de.jobst.resulter.adapter.driven.jpa;

import de.jobst.resulter.domain.*;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.jupiter.api.Test;
import org.springframework.lang.NonNull;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class EventDboMappingTest {

    public static final String A_EVENT_NAME = "A event name";
    public static final long A_EVENT_ID = 19L;

    @NonNull
    private static Event getTestEvent() {
        List<SplitTime> splitTimes = new ArrayList<>();
        String controlCode1 = "134";
        double punchTime1 = 120.0;
        splitTimes.add(SplitTime.of(controlCode1, punchTime1));
        splitTimes.add(SplitTime.of("128", 104.0));
        splitTimes.add(SplitTime.of("134", 34.0));
        Collection<PersonRaceResult> personRaceResults = new ArrayList<>();
        long raceNumber = 1L;
        ZonedDateTime startTime = ZonedDateTime.of(2020, 10, 11, 11, 12, 0, 0, ZoneId.systemDefault());
        ZonedDateTime finishTime = ZonedDateTime.of(2020, 10, 11, 11, 54, 0, 0, ZoneId.systemDefault());
        double punchTime = 1000.0;
        long position = 2L;
        ResultStatus resultStatus = ResultStatus.OK;
        personRaceResults.add(PersonRaceResult.of(
                raceNumber,
                startTime,
                finishTime,
                punchTime,
                position,
                resultStatus,
                Optional.of(splitTimes)));

        Collection<PersonResult> personResults = new ArrayList<>();
        String familyName = "Knopf";
        String givenName = "Jim";
        LocalDate birthDate = LocalDate.of(2000, 11, 1);
        Gender personGender = Gender.M;
        String clubName = "O-Club";
        String clubShortName = "OC";
        personResults.add(PersonResult.of(
                Optional.of(Person.of(
                        FamilyName.of(familyName), GivenName.of(givenName),
                        birthDate,
                        personGender)),
                Optional.of(Organisation.of(clubName, clubShortName)),
                Optional.of(personRaceResults)));

        Collection<ClassResult> classResults = new ArrayList<>();
        String className = "H50- (Herren ab 50)";
        String classShortName = "H50-";
        Gender classGender = Gender.M;
        classResults.add(ClassResult.of(className, classShortName,
                classGender, Optional.of(personResults)));

        return Event.of(A_EVENT_NAME, classResults);

    }

    @Test
    public void databaseEntityToDomainIsMappedCorrectly() {
        EventDbo eventDbo = new EventDbo();
        eventDbo.setId(A_EVENT_ID);
        eventDbo.setName(A_EVENT_NAME);

        Event event = EventDbo.asEvents(EventConfig.full(), List.of(eventDbo)).getFirst();

        assertThat(Objects.requireNonNull(event.getId()).value()).isEqualTo(A_EVENT_ID);
        assertThat(event.getName().value()).isEqualTo(A_EVENT_NAME);
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
        Event event = getTestEvent();

        EventDbo entity = EventDbo.from(event);

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
                assertThat(classResultDbo.getName()).isEqualTo(classResult.classResultName().value());
                assertThat(classResultDbo.getShortName()).isEqualTo(classResult.classResultShortName().value());
                assertThat(classResultDbo.getGender()).isEqualTo(classResult.gender());

                if (classResult.personResults().isPresent()) {
                    assertThat(classResultDbo.getPersonResults()).isNotEmpty();
                    assertThat(classResultDbo.getPersonResults()).hasSize(classResult.personResults()
                            .get()
                            .value()
                            .size());

                    PersonResult
                            personResult =
                            classResult.personResults().get().value().stream().findFirst().orElse(null);
                    PersonResultDbo
                            personResultDbo =
                            classResultDbo.getPersonResults().stream().findFirst().orElse(null);

                    if (ObjectUtils.isNotEmpty(personResult)) {
                        assertThat(personResultDbo).isNotNull();

                        if (personResult.person().isPresent()) {
                            assertThat(personResultDbo).isNotNull();
                            assertThat(personResultDbo.getPerson()).isNotNull();

                            Person person = personResult.person().get();
                            PersonDbo personDbo = personResultDbo.getPerson();

                            assertThat(personDbo.getFamilyName()).isEqualTo(person.getPersonName()
                                    .familyName()
                                    .value());
                            assertThat(personDbo.getGivenName()).isEqualTo(person.getPersonName().givenName().value());
                            assertThat(personDbo.getBirthDate()).isEqualTo(person.getBirthDate().value());
                        } else {
                            assertThat(personResultDbo.getPerson()).isNull();
                        }

                        if (personResult.organisation().isPresent()) {
                            assertThat(personResultDbo).isNotNull();
                            assertThat(personResultDbo.getOrganisation()).isNotNull();

                            Organisation organisation = personResult.organisation().get();
                            OrganisationDbo organisationDbo = personResultDbo.getOrganisation();

                            assertThat(organisationDbo.getName()).isEqualTo(organisation.getName().value());
                            assertThat(organisationDbo.getShortName()).isEqualTo(organisation.getShortName().value());
                        } else {
                            assertThat(personResultDbo.getOrganisation()).isNull();
                        }

                        if (personResult.personRaceResults().isPresent()) {
                            assertThat(personResultDbo.getPersonRaceResults()).isNotEmpty();
                            assertThat(personResultDbo.getPersonRaceResults()).hasSize(personResult.personRaceResults()
                                    .get()
                                    .value()
                                    .size());

                            PersonRaceResult
                                    personRaceResult =
                                    personResult.personRaceResults().get().value().stream().findFirst().orElse(null);
                            PersonRaceResultDbo
                                    personRaceResultDbo =
                                    personResultDbo.getPersonRaceResults().stream().findFirst().orElse(null);

                            if (ObjectUtils.isNotEmpty(personRaceResult)) {
                                assertThat(personRaceResultDbo).isNotNull();

                                assertThat(personRaceResultDbo.getRaceNumber()).isEqualTo(personRaceResult.raceNumber()
                                        .value());
                                assertThat(personRaceResultDbo.getStartTime()).isEqualTo(personRaceResult.startTime()
                                        .value());
                                assertThat(personRaceResultDbo.getFinishTime()).isEqualTo(personRaceResult.finishTime()
                                        .value());
                                assertThat(personRaceResultDbo.getPunchTime()).isEqualTo(personRaceResult.runtime()
                                        .value());
                                assertThat(personRaceResultDbo.getPosition()).isEqualTo(personRaceResult.positon()
                                        .value());
                                assertThat(personRaceResultDbo.getState()).isEqualTo(personRaceResult.state());

                                if (personRaceResult.splitTimes().isPresent()) {
                                    assertThat(personRaceResultDbo.getSplitTimes()).isNotEmpty();
                                    assertThat(personRaceResultDbo.getSplitTimes()).hasSize(personRaceResult.splitTimes()
                                            .get()
                                            .value()
                                            .size());

                                    SplitTime
                                            splitTime =
                                            personRaceResult.splitTimes()
                                                    .get()
                                                    .value()
                                                    .stream()
                                                    .findFirst()
                                                    .orElse(null);
                                    SplitTimeDbo
                                            splitTimeDbo =
                                            personRaceResultDbo.getSplitTimes().stream().findFirst().orElse(null);
                                    if (ObjectUtils.isNotEmpty(splitTime)) {
                                        assertThat(splitTimeDbo).isNotNull();

                                        assertThat(splitTimeDbo.getControlCode()).isEqualTo(splitTime.controlCode()
                                                .value());
                                        assertThat(splitTimeDbo.getPunchTime()).isEqualTo(splitTime.punchTime()
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