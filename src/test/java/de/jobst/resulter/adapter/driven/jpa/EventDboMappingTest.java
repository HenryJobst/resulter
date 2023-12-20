package de.jobst.resulter.adapter.driven.jpa;

import de.jobst.resulter.domain.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class EventDboMappingTest {

    public static final String A_EVENT_NAME = "A event name";
    public static final long A_EVENT_ID = 19L;

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

        Event event = Event.of(A_EVENT_NAME, classResults);

        EventDbo entity = EventDbo.from(event);

        assertThat(entity.getName()).isEqualTo(A_EVENT_NAME);
        assertThat(entity.getClassResults()).hasSize(classResults.size());
        assertThat(entity.getClassResults().stream().findFirst()).isPresent();
        ClassResultDbo classResultDbo = entity.getClassResults().stream().findFirst().get();
        assertThat(classResultDbo.getName()).isEqualTo(className);
        assertThat(classResultDbo.getShortName()).isEqualTo(classShortName);
        assertThat(classResultDbo.getGender()).isEqualTo(classGender);
        assertThat(classResultDbo.getPersonResults()).hasSize(personResults.size());
        PersonResultDbo personResultDbo = classResultDbo.getPersonResults().stream().findFirst().get();
        assertThat(personResultDbo.getPerson().getFamilyName()).isEqualTo(familyName);
        assertThat(personResultDbo.getPerson().getGivenName()).isEqualTo(givenName);
        assertThat(personResultDbo.getPerson().getBirthDate()).isEqualTo(birthDate);
        assertThat(personResultDbo.getOrganisation()).isNotNull();
        assertThat(personResultDbo.getOrganisation().getName()).isEqualTo(clubName);
        assertThat(personResultDbo.getOrganisation().getShortName()).isEqualTo(clubShortName);
        assertThat(personResultDbo.getPersonRaceResults()).hasSize(personRaceResults.size());
        PersonRaceResultDbo personRaceResult = personResultDbo.getPersonRaceResults().stream().findFirst().get();
        assertThat(personRaceResult.getRaceNumber()).isEqualTo(raceNumber);
        assertThat(personRaceResult.getStartTime()).isEqualTo(startTime);
        assertThat(personRaceResult.getFinishTime()).isEqualTo(finishTime);
        assertThat(personRaceResult.getPunchTime()).isEqualTo(punchTime);
        assertThat(personRaceResult.getPosition()).isEqualTo(position);
        assertThat(personRaceResult.getState()).isEqualTo(resultStatus);
        assertThat(personRaceResult.getSplitTimes()).hasSize(splitTimes.size());
        SplitTimeDbo splitTime1 = personRaceResult.getSplitTimes().stream().findFirst().get();
        assertThat(splitTime1.getControlCode()).isEqualTo(controlCode1);
        assertThat(splitTime1.getPunchTime()).isEqualTo(punchTime1);


    }

}