package de.jobst.resulter.domain;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.springframework.lang.NonNull;

public class EventTestDataGenerator {

    public static final String A_EVENT_NAME = "A event name";

    @NonNull
    public static TestEventResult getTestEvent() {
        String className = "H50- (Herren ab 50)";
        String classShortName = "H50-";
        Gender classGender = Gender.M;
        List<SplitTime> splitTimes = new ArrayList<>();
        String controlCode1 = "134";
        double punchTime1 = 120.0;
        splitTimes.add(SplitTime.of(controlCode1, punchTime1));
        splitTimes.add(SplitTime.of("128", 104.0));
        splitTimes.add(SplitTime.of("132", 34.0));
        Collection<PersonRaceResult> personRaceResults = new ArrayList<>();
        long raceNumber = 1L;
        ZonedDateTime startTime = ZonedDateTime.of(2020, 10, 11, 11, 12, 0, 0, ZoneId.systemDefault());
        ZonedDateTime finishTime = ZonedDateTime.of(2020, 10, 11, 11, 54, 0, 0, ZoneId.systemDefault());
        double punchTime = 1000.0;
        long position = 2L;
        ResultStatus resultStatus = ResultStatus.OK;
        personRaceResults.add(PersonRaceResult.of(
                classShortName, 1L, startTime, finishTime, punchTime, position, (byte) 1, resultStatus));

        Collection<PersonResult> personResults = new ArrayList<>();
        String familyName = "Knopf";
        String givenName = "Jim";
        LocalDate birthDate = LocalDate.of(2000, 11, 1);
        Gender personGender = Gender.M;

        String clubName = "O-Club";
        String clubShortName = "OC";
        Country country = Country.of(CountryId.empty().value(), "NED", "NED");
        Organisation organisation = Organisation.of(clubName, clubShortName, country.getId());

        Person person = Person.of(FamilyName.of(familyName), GivenName.of(givenName), birthDate, personGender);
        personResults.add(PersonResult.of(
                ClassResultShortName.of(classShortName), person.getId(), organisation.getId(), personRaceResults));

        Collection<ClassResult> classResults = new ArrayList<>();
        classResults.add(ClassResult.of(className, classShortName, classGender, personResults, CourseId.empty()));

        ResultList resultList = new ResultList(
                ResultListId.empty(),
                EventId.empty(),
                RaceId.empty(),
                "test",
                ZonedDateTime.now(),
                "completed",
                classResults);

        return new TestEventResult(Event.of(A_EVENT_NAME), country, organisation, person, resultList);
    }
}
