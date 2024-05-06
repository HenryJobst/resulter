package de.jobst.resulter.adapter.driver.web;

import de.jobst.resulter.application.*;
import de.jobst.resulter.domain.*;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.util.Pair;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
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

    public XMLImportService(XmlParser xmlParser,
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

    private static List<Pair<PersonRaceResult, SplitTimeList>> getPersonRaceResults(de.jobst.resulter.adapter.driver.web.jaxb.PersonResult personResult,
                                                                                    EventId eventId,
                                                                                    ResultListId resultListId,
                                                                                    ClassResultShortName classResultShortName,
                                                                                    PersonId personId) {
        return personResult.getResults()
            .stream()
            .map(personRaceResult -> Pair.of(PersonRaceResult.of(classResultShortName.value(),
                    personId.value(),
                    ObjectUtils.isNotEmpty(personRaceResult.getStartTime()) ?
                    personRaceResult.getStartTime()
                        .toInstant()
                        .atZone(personRaceResult.getStartTime().getTimeZone().toZoneId()) :
                    null,
                    ObjectUtils.isNotEmpty(personRaceResult.getFinishTime()) ?
                    personRaceResult.getFinishTime()
                        .toInstant()
                        .atZone(personRaceResult.getFinishTime().getTimeZone().toZoneId()) :
                    null,
                    ObjectUtils.isNotEmpty(personRaceResult.getTime()) ? personRaceResult.getTime() : null,
                    Objects.nonNull(personRaceResult.getPosition()) ? personRaceResult.getPosition().longValue() : null,
                    ResultStatus.fromValue(personRaceResult.getStatus().value())),
                new SplitTimeList(SplitTimeListId.empty(),
                    eventId,
                    resultListId,
                    classResultShortName,
                    personId,
                    personRaceResult.getSplitTimes()
                        .stream()
                        .map(x -> new SplitTime(ControlCode.of(x.getControlCode()), PunchTime.of(x.getTime()), null))
                        .toList())))
            .toList();
    }

    public static <A, B> Pair<List<A>, List<B>> convertListPairToListsPair(List<Pair<A, B>> listPair) {
        List<A> firstList = listPair.stream().map(Pair::getFirst).collect(Collectors.toList());
        List<B> secondList = listPair.stream().map(Pair::getSecond).collect(Collectors.toList());

        return Pair.of(firstList, secondList);
    }

    @Transactional
    ImportResult importFile(InputStream inputStream) throws Exception {
        var resultList = xmlParser.parseXmlFile(inputStream);

        Map<String, Country> countriesByCode = importCountries(resultList);

        Map<String, Organisation> organisationByName = importOrganisations(resultList, countriesByCode);

        Map<Person.DomainKey, Person> personByDomainKey = importPersons(resultList);

        Event event = importEvent(resultList, organisationByName);

        Race race = importRace(resultList, event);

        ResultList domainResultList = importResultListHead(event, race, resultList);

        Map<Course.DomainKey, Course> courseByDomainKey = importCourses(event, resultList);

        domainResultList = importResultListBody(event,
            race.getId(),
            domainResultList,
            resultList,
            organisationByName,
            personByDomainKey,
            courseByDomainKey);

        return new ImportResult(event, countriesByCode, organisationByName, personByDomainKey, domainResultList);
    }

    @NonNull
    private Race importRace(de.jobst.resulter.adapter.driver.web.jaxb.ResultList resultList, Event event) {
        var raceOfResult = resultList.getEvent().getRaces().stream().findAny().orElse(null);
        var race = Race.of(event.getId(),
            raceOfResult != null ? raceOfResult.getName() : null,
            raceOfResult != null ? raceOfResult.getRaceNumber().byteValue() : RaceNumber.empty().value());
        return raceService.findOrCreate(race);
    }

    @NonNull
    private ResultList importResultListBody(Event event,
                                            RaceId raceId,
                                            ResultList partialResultList,
                                            de.jobst.resulter.adapter.driver.web.jaxb.ResultList resultList,
                                            Map<String, Organisation> organisationByName,
                                            Map<Person.DomainKey, Person> personByDomainKey,
                                            Map<Course.DomainKey, Course> courseByDomainKey) {
        @SuppressWarnings("UnnecessaryLocalVariable") ResultList finalDomainResultList = partialResultList;

        List<Pair<ClassResult, List<List<SplitTimeList>>>> listOfPairsOfClassResultAndGroupedSplitTimeLists =
            resultList.getClassResults().stream().map(classResult -> {
                var clazz = classResult.getClazz();
                var personResults = convertListPairToListsPair(getPersonResults(classResult,
                    event.getId(),
                    raceId,
                    finalDomainResultList.getId(),
                    ClassResultShortName.of(clazz.getShortName()),
                    organisationByName,
                    personByDomainKey));
                return Pair.of(ClassResult.of(clazz.getName(),
                    clazz.getShortName(),
                    Gender.of(clazz.getSex()),
                    personResults.getFirst(),
                    classResult.getCourses().stream().findAny().isPresent() ?
                    courseByDomainKey.get(new Course.DomainKey(event.getId(),
                        CourseName.of(classResult.getCourses().getFirst().getName()))).getId() :
                    null), personResults.getSecond());
            }).toList();
        var pairOfClassResultListAndGroupedSplitTimeLists =
            convertListPairToListsPair(listOfPairsOfClassResultAndGroupedSplitTimeLists);

        // collect and persist split time lists
        var groupedSplitTimeLists = pairOfClassResultListAndGroupedSplitTimeLists.getSecond();
        Collection<SplitTimeList> splitTimeLists =
            groupedSplitTimeLists.stream().flatMap(Collection::stream).flatMap(Collection::stream).toList();

        var classResults = pairOfClassResultListAndGroupedSplitTimeLists.getFirst();

        if (splitTimeLists.stream().anyMatch(x -> !x.getSplitTimes().isEmpty())) {
            splitTimeLists = splitTimeListService.findOrCreate(splitTimeLists);

            Map<SplitTimeList.DomainKey, SplitTimeList> splitTimeListByDomainKey =
                splitTimeLists.stream().collect(Collectors.toMap(SplitTimeList::getDomainKey, x -> x));

            for (ClassResult x : classResults) {
                for (PersonResult y : x.personResults().value()) {
                    for (PersonRaceResult z : y.personRaceResults().value()) {
                        z.setSplitTimeListId(splitTimeListByDomainKey.get(new SplitTimeList.DomainKey(event.getId(),
                            finalDomainResultList.getId(),
                            x.classResultShortName(),
                            y.personId())).getId());
                    }
                }
            }
        }
        finalDomainResultList.setClassResults(classResults);
        return resultListService.update(finalDomainResultList);
    }

    private ResultList importResultListHead(Event event,
                                            Race race,
                                            de.jobst.resulter.adapter.driver.web.jaxb.ResultList resultList) {
        // Result list
        ResultList domainResultList = new ResultList(ResultListId.empty(),
            event.getId(),
            race.getId(),
            resultList.getCreator(),
            ObjectUtils.isNotEmpty(resultList.getCreateTime()) ?
            resultList.getCreateTime().toInstant().atZone(resultList.getCreateTime().getTimeZone().toZoneId()) :
            null,
            resultList.getStatus(),
            null);
        // Create without class results to get a pk
        return resultListService.findOrCreate(domainResultList);
    }

    @NonNull
    private Event importEvent(de.jobst.resulter.adapter.driver.web.jaxb.ResultList resultList,
                              Map<String, Organisation> organisationByName) {
        // Event
        Event event = Event.of(resultList.getEvent().getName(),
            resultList.getEvent().getOrganisers().stream().map(x -> organisationByName.get(x.getName())).toList());
        event = eventService.findOrCreate(event);
        return event;
    }

    @NonNull
    private Map<Person.DomainKey, Person> importPersons(de.jobst.resulter.adapter.driver.web.jaxb.ResultList resultList) {
        Collection<Person> persons = resultList.getClassResults()
            .stream()
            .flatMap(x -> x.getPersonResults().stream())
            .map(de.jobst.resulter.adapter.driver.web.jaxb.PersonResult::getPerson)
            .map(p -> Person.of(PersonName.of(p.getName().getFamily(), p.getName().getGiven()),
                BirthDate.of(ObjectUtils.isNotEmpty(p.getBirthDate()) ?
                             p.getBirthDate().toGregorianCalendar().toZonedDateTime().toLocalDate() :
                             null),
                Gender.of(p.getSex())))
            .collect(Collectors.toSet());
        persons = personService.findOrCreate(persons);
        return persons.stream().collect(Collectors.toMap(Person::getDomainKey, x -> x));
    }

    @NonNull
    private Map<Course.DomainKey, Course> importCourses(Event event,
                                                        de.jobst.resulter.adapter.driver.web.jaxb.ResultList resultList) {
        Collection<Course> courses = resultList.getClassResults()
            .stream()
            .flatMap(x -> x.getCourses().stream())
            .filter(Objects::nonNull)
            .map(c -> Course.of(event.getId(),
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
    private Map<String, Organisation> importOrganisations(de.jobst.resulter.adapter.driver.web.jaxb.ResultList resultList,
                                                          Map<String, Country> countriesByCode) {
        Collection<Organisation> organisations = Stream.concat(
            // organisations from event
            resultList.getEvent()
                .getOrganisers()
                .stream()
                .filter(Objects::nonNull)
                .map(o -> Organisation.of(OrganisationId.empty().value(),
                    o.getName(),
                    o.getShortName(),
                    OrganisationType.OTHER.value(),
                    (null == o.getCountry() ? null : countriesByCode.get(o.getCountry().getCode())),
                    new ArrayList<>()))
                .sorted(),
            // organisations from persons
            resultList.getClassResults()
                .stream()
                .flatMap(x -> x.getPersonResults().stream())
                .map(de.jobst.resulter.adapter.driver.web.jaxb.PersonResult::getOrganisation)
                .filter(Objects::nonNull)
                .map(o -> Organisation.of(OrganisationId.empty().value(),
                    o.getName(),
                    o.getShortName(),
                    OrganisationType.OTHER.value(),
                    (null == o.getCountry() ? null : countriesByCode.get(o.getCountry().getCode())),
                    new ArrayList<>()))
                .sorted()).collect(Collectors.toSet());

        organisations = organisationService.findOrCreate(organisations);
        return organisations.stream().collect(Collectors.toMap(x -> x.getName().value(), x -> x));
    }

    @NonNull
    private Map<String, Country> importCountries(de.jobst.resulter.adapter.driver.web.jaxb.ResultList resultList) {
        // countries
        Collection<Country> countries = Stream.concat(
            // countries from event
            resultList.getEvent()
                .getOrganisers()
                .stream()
                .filter(Objects::nonNull)
                .map(o -> o.getCountry() == null ?
                          null :
                          Country.of(o.getCountry().getCode(), o.getCountry().getValue()))
                .filter(Objects::nonNull),
            // countries from persons
            resultList.getClassResults()
                .stream()
                .flatMap(x -> x.getPersonResults().stream())
                .map(de.jobst.resulter.adapter.driver.web.jaxb.PersonResult::getOrganisation)
                .filter(Objects::nonNull)
                .map(o -> o.getCountry() == null ?
                          null :
                          Country.of(o.getCountry().getCode(), o.getCountry().getValue()))
                .filter(Objects::nonNull)).collect(Collectors.toSet());

        countries = this.countryService.findOrCreate(countries);
        return countries.stream().collect(Collectors.toMap(x -> x.getCode().value(), x -> x));
    }

    @NonNull
    private Person.DomainKey createPersonDomainKey(de.jobst.resulter.adapter.driver.web.jaxb.Person p) {
        return new Person.DomainKey(PersonName.of(FamilyName.of(p.getName().getFamily()),
            GivenName.of(p.getName().getGiven())),
            null != p.getBirthDate() ?
            BirthDate.of(p.getBirthDate().toGregorianCalendar().toZonedDateTime().toLocalDate()) :
            BirthDate.of(null));
    }

    @NonNull
    private List<Pair<PersonResult, List<SplitTimeList>>> getPersonResults(de.jobst.resulter.adapter.driver.web.jaxb.ClassResult classResult,
                                                                           EventId eventId,
                                                                           RaceId raceId,
                                                                           ResultListId resultListId,
                                                                           ClassResultShortName classResultShortName,
                                                                           Map<String, Organisation> organisationByName,
                                                                           Map<Person.DomainKey, Person> personByDomainKey) {
        return classResult.getPersonResults().stream().map(personResult -> {
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
                convertListPairToListsPair(getPersonRaceResults(personResult,
                    eventId,
                    resultListId,
                    classResultShortName,
                    personId));
            return Pair.of(PersonResult.of(classResultShortName,
                personId,
                Objects.nonNull(personResult.getOrganisation()) ?
                organisationByName.get(personResult.getOrganisation().getName()).getId() :
                null,
                personRaceResults.getFirst()), personRaceResults.getSecond());
        }).toList();
    }

    public record ImportResult(Event event, Map<String, Country> countryMap, Map<String, Organisation> organisationMap,
                               Map<Person.DomainKey, Person> personMap, ResultList resultLists) {

    }


}
