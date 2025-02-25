package de.jobst.resulter.adapter.driver.web;

import de.jobst.resulter.adapter.driver.web.jaxb.OverallResult;
import de.jobst.resulter.application.*;
import de.jobst.resulter.domain.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.util.Pair;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.math.BigInteger;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.time.ZoneOffset.UTC;

@Service
@Slf4j
public class XMLImportService {

    private final XmlParser xmlParser;

    private final EventService eventService;
    private final CountryService countryService;

    private final OrganisationService organisationService;
    private final PersonService personService;
    private final CourseService courseService;
    private final ResultListService resultListService;
    private final SplitTimeListService splitTimeListService;
    private final RaceService raceService;

    public XMLImportService(
            XmlParser xmlParser,
            EventService eventService,
            CountryService countryService,
            OrganisationService organisationService,
            PersonService personService,
            CourseService courseService,
            ResultListService resultListService,
            SplitTimeListService splitTimeListService,
            RaceService raceService) {
        this.xmlParser = xmlParser;
        this.eventService = eventService;
        this.countryService = countryService;
        this.organisationService = organisationService;
        this.personService = personService;
        this.courseService = courseService;
        this.resultListService = resultListService;
        this.splitTimeListService = splitTimeListService;
        this.raceService = raceService;
    }

    private static List<Pair<PersonRaceResult, SplitTimeList>> getPersonRaceResults(
            de.jobst.resulter.adapter.driver.web.jaxb.PersonResult personResult,
            EventId eventId,
            ResultListId resultListId,
            Race race,
            ClassResultShortName classResultShortName,
            PersonId personId) {
        List<de.jobst.resulter.adapter.driver.web.jaxb.PersonRaceResult> results = personResult.getResults();
        results = results.stream()
                .filter(x -> isReallyStarted(x)
                        && x.getRaceNumber().byteValue() == race.getRaceNumber().value())
                .flatMap(personRaceResult -> {
                    OverallResult overallResult = personRaceResult.getOverallResult();
                    if (overallResult != null) {
                        var overallPersonRaceResult = new de.jobst.resulter.adapter.driver.web.jaxb.PersonRaceResult();
                        overallPersonRaceResult.setRaceNumber(BigInteger.valueOf(0));
                        overallPersonRaceResult.setBibNumber(personRaceResult.getBibNumber());
                        overallPersonRaceResult.setStartTime(personRaceResult.getStartTime());
                        overallPersonRaceResult.setFinishTime(personRaceResult.getFinishTime());
                        overallPersonRaceResult.setTime(overallResult.getTime());
                        overallPersonRaceResult.setPosition(overallResult.getPosition());
                        overallPersonRaceResult.setStatus(overallResult.getStatus());
                        overallPersonRaceResult.setTimeBehind(overallResult.getTimeBehind());
                        personRaceResult.setOverallResult(null);
                        return Stream.of(overallPersonRaceResult, personRaceResult);
                    } else {
                        return Stream.of(personRaceResult);
                    }
                })
                .toList();
        return results.stream()
                .filter(XMLImportService::isReallyStarted)
                .filter(x ->
                        x.getRaceNumber().byteValue() == race.getRaceNumber().value()
                                || x.getRaceNumber().byteValue() == 0)
                .map(personRaceResult -> Pair.of(
                        PersonRaceResult.of(
                                classResultShortName.value(),
                                personId.value(),
                                ObjectUtils.isNotEmpty(personRaceResult.getStartTime())
                                        ? personRaceResult
                                                .getStartTime()
                                                .toInstant()
                                                .atZone(personRaceResult
                                                        .getStartTime()
                                                        .getTimeZone()
                                                        .toZoneId())
                                        : null,
                                ObjectUtils.isNotEmpty(personRaceResult.getFinishTime())
                                        ? personRaceResult
                                                .getFinishTime()
                                                .toInstant()
                                                .atZone(personRaceResult
                                                        .getFinishTime()
                                                        .getTimeZone()
                                                        .toZoneId())
                                        : null,
                                ObjectUtils.isNotEmpty(personRaceResult.getTime()) ? personRaceResult.getTime() : null,
                                Objects.nonNull(personRaceResult.getPosition())
                                        ? personRaceResult.getPosition().longValue()
                                        : null,
                                ObjectUtils.isNotEmpty(personRaceResult.getRaceNumber())
                                        ? personRaceResult.getRaceNumber().byteValue()
                                        : (byte) 1,
                                ResultStatus.fromValue(
                                        personRaceResult.getStatus().value())),
                        new SplitTimeList(
                                SplitTimeListId.empty(),
                                eventId,
                                resultListId,
                                classResultShortName,
                                personId,
                                RaceNumber.of(personRaceResult.getRaceNumber().byteValue()),
                                personRaceResult.getSplitTimes().stream()
                                        .map(x -> new SplitTime(
                                                ControlCode.of(x.getControlCode()), PunchTime.of(x.getTime()), null))
                                        .toList())))
                .toList();
    }

    private static boolean isReallyStarted(
            de.jobst.resulter.adapter.driver.web.jaxb.PersonRaceResult personRaceResult) {
        return !personRaceResult.getStatus().value().equals(ResultStatus.NOT_COMPETING.value())
                && !personRaceResult.getStatus().value().equals(ResultStatus.DID_NOT_START.value())
                && !personRaceResult.getStatus().value().equals(ResultStatus.DID_NOT_ENTER.value());
    }

    public static <A, B> Pair<List<A>, List<B>> convertListPairToListsPair(List<Pair<A, B>> listPair) {
        List<A> firstList = listPair.stream().map(Pair::getFirst).collect(Collectors.toList());
        List<B> secondList = listPair.stream().map(Pair::getSecond).collect(Collectors.toList());

        return Pair.of(firstList, secondList);
    }

    @Transactional
    ImportResult importFile(InputStream inputStream) {
        de.jobst.resulter.adapter.driver.web.jaxb.ResultList resultList;
        try {
            resultList = xmlParser.parseXmlFile(inputStream);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Map<String, Country> countriesByCode = importCountries(resultList);

        Map<String, Organisation> organisationByName = importOrganisations(resultList, countriesByCode);

        Map<Person.DomainKey, Person> personByDomainKey = importPersons(resultList);

        Event event = importEvent(resultList, organisationByName);

        List<Race> races = importRaces(resultList, event);

        List<ResultList> resultLists = races.stream()
                .map(race -> {
                    ResultList domainResultList = importResultListHead(event, race, resultList);

                    Map<Course.DomainKey, Course> courseByDomainKey = importCourses(event, resultList);

                    domainResultList = importResultListBody(
                            event,
                            race,
                            domainResultList,
                            resultList,
                            organisationByName,
                            personByDomainKey,
                            courseByDomainKey);
                    return domainResultList;
                })
                .toList();

        return new ImportResult(event, countriesByCode, organisationByName, personByDomainKey, resultLists);
    }

    @NonNull
    private List<Race> importRaces(de.jobst.resulter.adapter.driver.web.jaxb.ResultList resultList, Event event) {
        List<Byte> raceNumbers = resultList.getClassResults().stream()
                .flatMap(x -> x.getPersonResults().stream())
                .flatMap(x -> x.getResults().stream())
                .filter(x -> x.getRaceNumber() != null)
                .map(x -> x.getRaceNumber().byteValue())
                .distinct()
                .toList();

        return raceNumbers.stream()
                .map(raceNumber -> Race.of(
                        event.getId(),
                        resultList.getEvent().getRaces().stream()
                                .filter(x -> x.getRaceNumber().byteValue() == raceNumber)
                                .findAny()
                                .map(de.jobst.resulter.adapter.driver.web.jaxb.Race::getName)
                                .orElse(null),
                        raceNumber))
                .map(raceService::findOrCreate)
                .toList();
    }

    @NonNull
    private ResultList importResultListBody(
            Event event,
            Race race,
            ResultList partialResultList,
            de.jobst.resulter.adapter.driver.web.jaxb.ResultList resultList,
            Map<String, Organisation> organisationByName,
            Map<Person.DomainKey, Person> personByDomainKey,
            Map<Course.DomainKey, Course> courseByDomainKey) {
        @SuppressWarnings("UnnecessaryLocalVariable")
        ResultList finalDomainResultList = partialResultList;

        List<Pair<ClassResult, List<List<SplitTimeList>>>> listOfPairsOfClassResultAndGroupedSplitTimeLists =
                resultList.getClassResults().stream()
                        .map(classResult -> {
                            var clazz = classResult.getClazz();
                            var personResults = convertListPairToListsPair(getPersonResults(
                                    classResult,
                                    event.getId(),
                                    race,
                                    finalDomainResultList.getId(),
                                    ClassResultShortName.of(clazz.getShortName()),
                                    organisationByName,
                                    personByDomainKey));
                            return Pair.of(
                                    ClassResult.of(
                                            clazz.getName(),
                                            clazz.getShortName(),
                                            Gender.of(clazz.getSex()),
                                            personResults.getFirst(),
                                            classResult.getCourses().stream()
                                                            .findAny()
                                                            .isPresent()
                                                    ? courseByDomainKey
                                                            .get(new Course.DomainKey(
                                                                    event.getId(),
                                                                    CourseName.of(classResult
                                                                            .getCourses()
                                                                            .getFirst()
                                                                            .getName())))
                                                            .getId()
                                                    : null),
                                    personResults.getSecond());
                        })
                        .toList();
        var pairOfClassResultListAndGroupedSplitTimeLists =
                convertListPairToListsPair(listOfPairsOfClassResultAndGroupedSplitTimeLists);

        // collect and persist split time lists
        var groupedSplitTimeLists = pairOfClassResultListAndGroupedSplitTimeLists.getSecond();
        Collection<SplitTimeList> splitTimeLists = groupedSplitTimeLists.stream()
                .flatMap(Collection::stream)
                .flatMap(Collection::stream)
                .toList();

        var classResults = pairOfClassResultListAndGroupedSplitTimeLists.getFirst();

        if (splitTimeLists.stream().anyMatch(x -> !x.getSplitTimes().isEmpty())) {
            splitTimeLists = splitTimeListService.findOrCreate(splitTimeLists);

            Map<SplitTimeList.DomainKey, SplitTimeList> splitTimeListByDomainKey =
                    splitTimeLists.stream().collect(Collectors.toMap(SplitTimeList::getDomainKey, x -> x));

            for (ClassResult x : classResults) {
                for (PersonResult y : x.personResults().value()) {
                    for (PersonRaceResult z : y.personRaceResults().value()) {
                        z.setSplitTimeListId(splitTimeListByDomainKey
                                .get(new SplitTimeList.DomainKey(
                                        event.getId(),
                                        finalDomainResultList.getId(),
                                        x.classResultShortName(),
                                        y.personId(),
                                        z.getRaceNumber()))
                                .getId());
                    }
                }
            }
        }
        finalDomainResultList.setClassResults(classResults);
        return resultListService.update(finalDomainResultList);
    }

    private ResultList importResultListHead(
            Event event, Race race, de.jobst.resulter.adapter.driver.web.jaxb.ResultList resultList) {
        // Result list
        ResultList domainResultList = new ResultList(
                ResultListId.empty(),
                event.getId(),
                race.getId(),
                resultList.getCreator(),
                ObjectUtils.isNotEmpty(resultList.getCreateTime())
                        ? resultList
                                .getCreateTime()
                                .toInstant()
                                .atZone(resultList.getCreateTime().getTimeZone().toZoneId())
                        : null,
                resultList.getStatus(),
                null);
        // Create without class results to get a pk
        return resultListService.findOrCreate(domainResultList);
    }

    @NonNull
    private Event importEvent(
            de.jobst.resulter.adapter.driver.web.jaxb.ResultList resultList,
            Map<String, Organisation> organisationByName) {

        ZonedDateTime eventStartDate = resultList.getClassResults().stream()
                .flatMap(x -> x.getPersonResults().stream())
                .map(de.jobst.resulter.adapter.driver.web.jaxb.PersonResult::getResults)
                .flatMap(Collection::stream)
                .map(de.jobst.resulter.adapter.driver.web.jaxb.PersonRaceResult::getStartTime)
                .filter(Objects::nonNull)
                .map(x -> x.toInstant().atZone(UTC))
                .min(Comparator.naturalOrder())
                .orElse(null);
        if (eventStartDate != null) {
            int currentMinute = eventStartDate.getMinute();
            int nextValidMinute = (int) (Math.floor(currentMinute / 15.0) * 15) % 60;
            eventStartDate =
                    eventStartDate.withMinute(nextValidMinute).withSecond(0).withNano(0);
        }

        // Event
        Event event = Event.of(
                resultList.getEvent().getName(),
                eventStartDate,
                resultList.getEvent().getOrganisers().stream()
                        .map(x -> organisationByName.get(x.getName()).getId())
                        .toList());
        event = eventService.findOrCreate(event);
        return event;
    }

    @NonNull
    private Map<Person.DomainKey, Person> importPersons(
            de.jobst.resulter.adapter.driver.web.jaxb.ResultList resultList) {
        Collection<Person> persons = resultList.getClassResults().stream()
                .flatMap(x -> x.getPersonResults().stream())
                .map(de.jobst.resulter.adapter.driver.web.jaxb.PersonResult::getPerson)
                .map(p -> Person.of(
                        PersonName.of(p.getName().getFamily(), p.getName().getGiven()),
                        BirthDate.of(
                                ObjectUtils.isNotEmpty(p.getBirthDate())
                                        ? p.getBirthDate()
                                                .toGregorianCalendar()
                                                .toZonedDateTime()
                                                .toLocalDate()
                                        : null),
                        Gender.of(p.getSex())))
                .collect(Collectors.toSet());
        persons = personService.findOrCreate(persons);
        return persons.stream().distinct().collect(Collectors.toMap(Person::getDomainKey, x -> x));
    }

    @NonNull
    private Map<Course.DomainKey, Course> importCourses(
            Event event, de.jobst.resulter.adapter.driver.web.jaxb.ResultList resultList) {
        Collection<Course> courses = resultList.getClassResults().stream()
                .flatMap(x -> x.getCourses().stream())
                .filter(Objects::nonNull)
                .map(c -> Course.of(
                        event.getId(),
                        CourseName.of(c.getName()),
                        CourseLength.of(c.getLength()),
                        CourseClimb.of(c.getClimb()),
                        NumberOfControls.of(c.getNumberOfControls().intValue())))
                .collect(Collectors.toSet());
        if (courses.isEmpty()) {
            return Map.of();
        }
        courses = this.courseService.findOrCreate(courses);
        return courses.stream().collect(Collectors.toMap(Course::getDomainKey, x -> x));
    }

    @NonNull
    private Map<String, Organisation> importOrganisations(
            de.jobst.resulter.adapter.driver.web.jaxb.ResultList resultList, Map<String, Country> countriesByCode) {
        Collection<Organisation> organisations = Stream.concat(
                        // organisations from event
                        resultList.getEvent().getOrganisers().stream()
                                .filter(Objects::nonNull)
                                .map(o -> Organisation.of(
                                        OrganisationId.empty().value(),
                                        o.getName(),
                                        o.getShortName(),
                                        OrganisationType.OTHER.value(),
                                        (null == o.getCountry() ? null :
                                         countriesByCode.get(o.getCountry().getCode()).getId()),
                                        new ArrayList<>()))
                                .sorted(),
                        // organisations from persons
                        resultList.getClassResults().stream()
                                .flatMap(x -> x.getPersonResults().stream())
                                .map(de.jobst.resulter.adapter.driver.web.jaxb.PersonResult::getOrganisation)
                                .filter(Objects::nonNull)
                                .map(o -> Organisation.of(
                                        OrganisationId.empty().value(),
                                        o.getName(),
                                        o.getShortName(),
                                        OrganisationType.OTHER.value(),
                                        (null == o.getCountry()
                                                ? null
                                                : countriesByCode.get(
                                                        o.getCountry().getCode()).getId()),
                                        new ArrayList<>()))
                                .sorted())
                .collect(Collectors.toSet());

        organisations = organisationService.findOrCreate(organisations);
        return organisations.stream().collect(Collectors.toMap(x -> x.getName().value(), x -> x));
    }

    @NonNull
    private Map<String, Country> importCountries(de.jobst.resulter.adapter.driver.web.jaxb.ResultList resultList) {
        // countries
        Collection<Country> countries = Stream.concat(
                        // countries from event
                        resultList.getEvent().getOrganisers().stream()
                                .filter(Objects::nonNull)
                                .map(o -> o.getCountry() == null
                                        ? null
                                        : Country.of(
                                                o.getCountry().getCode(),
                                                o.getCountry().getValue()))
                                .filter(Objects::nonNull),
                        // countries from persons
                        resultList.getClassResults().stream()
                                .flatMap(x -> x.getPersonResults().stream())
                                .map(de.jobst.resulter.adapter.driver.web.jaxb.PersonResult::getOrganisation)
                                .filter(Objects::nonNull)
                                .map(o -> o.getCountry() == null
                                        ? null
                                        : Country.of(
                                                o.getCountry().getCode(),
                                                o.getCountry().getValue()))
                                .filter(Objects::nonNull))
                .collect(Collectors.toSet());

        countries = this.countryService.findOrCreate(countries);
        return countries.stream().collect(Collectors.toMap(x -> x.getCode().value(), x -> x));
    }

    @NonNull
    private Person.DomainKey createPersonDomainKey(de.jobst.resulter.adapter.driver.web.jaxb.Person p) {
        return new Person.DomainKey(
                PersonName.of(
                        FamilyName.of(p.getName().getFamily()),
                        GivenName.of(p.getName().getGiven())),
                null != p.getBirthDate()
                        ? BirthDate.of(p.getBirthDate()
                                .toGregorianCalendar()
                                .toZonedDateTime()
                                .toLocalDate())
                        : BirthDate.of(null),
                Gender.of(p.getSex()));
    }

    @NonNull
    private List<Pair<PersonResult, List<SplitTimeList>>> getPersonResults(
            de.jobst.resulter.adapter.driver.web.jaxb.ClassResult classResult,
            EventId eventId,
            Race race,
            ResultListId resultListId,
            ClassResultShortName classResultShortName,
            Map<String, Organisation> organisationByName,
            Map<Person.DomainKey, Person> personByDomainKey) {
        return classResult.getPersonResults().stream()
                .map(personResult -> {
                    PersonId personId;
                    if (Objects.nonNull(personResult.getPerson())) {
                        Person.DomainKey personDomainKey = createPersonDomainKey(personResult.getPerson());
                        Person person = personByDomainKey.get(personDomainKey);
                        if (person != null) {
                            personId = person.getId();
                        } else {
                            throw new RuntimeException("Person not found: " + personDomainKey);
                        }
                    } else {
                        throw new RuntimeException("Person not found: " + personResult);
                    }
                    Pair<List<PersonRaceResult>, List<SplitTimeList>> personRaceResults =
                            convertListPairToListsPair(getPersonRaceResults(
                                    personResult, eventId, resultListId, race, classResultShortName, personId));
                    return Pair.of(
                            PersonResult.of(
                                    classResultShortName,
                                    personId,
                                    Objects.nonNull(personResult.getOrganisation())
                                            ? organisationByName
                                                    .get(personResult
                                                            .getOrganisation()
                                                            .getName())
                                                    .getId()
                                            : null,
                                    personRaceResults.getFirst()),
                            personRaceResults.getSecond());
                })
                .toList();
    }

    public record ImportResult(
            Event event,
            Map<String, Country> countryMap,
            Map<String, Organisation> organisationMap,
            Map<Person.DomainKey, Person> personMap,
            List<ResultList> resultLists) {}
}
