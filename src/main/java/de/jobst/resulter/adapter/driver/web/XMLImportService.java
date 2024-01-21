package de.jobst.resulter.adapter.driver.web;

import de.jobst.resulter.application.*;
import de.jobst.resulter.domain.*;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.util.Pair;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.ZoneId;
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
    private final ResultListService resultListService;
    private final SplitTimeListService splitTimeListService;

    public XMLImportService(XmlParser xmlParser,
                            EventService eventService,
                            CountryService countryService,
                            OrganisationService organisationService,
                            PersonService personService,
                            ResultListService resultListService,
                            SplitTimeListService splitTimeListService) {
        this.xmlParser = xmlParser;
        this.eventService = eventService;
        this.countryService = countryService;
        this.organisationService = organisationService;
        this.personService = personService;
        this.resultListService = resultListService;
        this.splitTimeListService = splitTimeListService;
    }

    private static List<Pair<PersonRaceResult, SplitTimeList>> getPersonRaceResults(de.jobst.resulter.adapter.driver.web.jaxb.PersonResult personResult,
                                                                                    EventId eventId,
                                                                                    ResultListId resultListId,
                                                                                    ClassResultShortName classResultShortName,
                                                                                    PersonId personId) {
        return personResult.getResults()
            .stream()
            .map(personRaceResult -> Pair.of(PersonRaceResult.of(personRaceResult.getRaceNumber().longValue(),
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
                    RaceNumber.of(personRaceResult.getRaceNumber().longValue()),
                    personRaceResult.getSplitTimes()
                        .stream()
                        .map(x -> new SplitTime(ControlCode.of(x.getControlCode()), PunchTime.of(x.getTime())))
                        .toList())))
            .toList();
    }

    public static <A, B> Pair<List<A>, List<B>> convertListPairToListsPair(List<Pair<A, B>> listPair) {
        List<A> firstList = listPair.stream().map(Pair::getFirst).collect(Collectors.toList());
        List<B> secondList = listPair.stream().map(Pair::getSecond).collect(Collectors.toList());

        return Pair.of(firstList, secondList);
    }

    ImportResult importFile(InputStream inputStream) throws Exception {
        var resultList = xmlParser.parseXmlFile(inputStream);

        Map<String, Country> countriesByCode = importCountries(resultList);

        Map<String, Organisation> organisationByName = importOrganisations(resultList, countriesByCode);

        Map<Person.DomainKey, Person> personByDomainKey = importPersons(resultList);

        Event event = importEvent(resultList, organisationByName);


        ResultList domainResultList = importResultListHead(event, resultList);

        domainResultList =
            importResultListBody(event, domainResultList, resultList, organisationByName, personByDomainKey);

        return new ImportResult(event, countriesByCode, organisationByName, personByDomainKey, domainResultList);
    }

    private ResultList importResultListBody(Event event,
                                            ResultList domainResultList,
                                            de.jobst.resulter.adapter.driver.web.jaxb.ResultList resultList,
                                            Map<String, Organisation> organisationByName,
                                            Map<Person.DomainKey, Person> personByDomainKey) {
        ResultList finalDomainResultList = domainResultList;
        List<Pair<ClassResult, List<List<SplitTimeList>>>> classResultsTemp =
            resultList.getClassResults().stream().map(classResult -> {
                var clazz = classResult.getClazz();
                var personResults = convertListPairToListsPair(getPersonResults(classResult,
                    event.getId(),
                    finalDomainResultList.getId(),
                    ClassResultShortName.of(clazz.getShortName()),
                    organisationByName,
                    personByDomainKey));
                return Pair.of(ClassResult.of(clazz.getName(),
                    clazz.getShortName(),
                    Gender.of(clazz.getSex()),
                    personResults.getFirst()), personResults.getSecond());
            }).toList();
        var classResults = convertListPairToListsPair(classResultsTemp);

        // collect and persist split time lists
        Collection<SplitTimeList> splitTimeLists =
            classResults.getSecond().stream().flatMap(Collection::stream).flatMap(Collection::stream).toList();
        splitTimeLists = splitTimeListService.findOrCreate(splitTimeLists);
        Map<SplitTimeList.DomainKey, SplitTimeList> splitTimeListByDomainKey =
            splitTimeLists.stream().collect(Collectors.toMap(SplitTimeList::getDomainKey, x -> x));

        classResults.getFirst()
            .forEach(x -> x.personResults()
                .value()
                .forEach(y -> y.personRaceResults()
                    .value()
                    .forEach(z -> z.setSplitTimeListId(splitTimeListByDomainKey.get(new SplitTimeList.DomainKey(event.getId(),
                        finalDomainResultList.getId(),
                        x.classResultShortName(),
                        y.personId(),
                        z.getRaceNumber())).getId()))));

        domainResultList.setClassResults(classResults.getFirst());
        domainResultList = resultListService.update(domainResultList);
        return domainResultList;
    }

    private ResultList importResultListHead(Event event,
                                            de.jobst.resulter.adapter.driver.web.jaxb.ResultList resultList) {
        // Result list
        ResultList domainResultList = new ResultList(ResultListId.empty(),
            event.getId(),
            resultList.getCreator(),
            ObjectUtils.isNotEmpty(resultList.getCreateTime()) ?
            resultList.getCreateTime().toInstant().atZone(resultList.getCreateTime().getTimeZone().toZoneId()) :
            null,
            resultList.getStatus(),
            null);
        // Create without class results to get a pk
        domainResultList = resultListService.findOrCreate(domainResultList);
        return domainResultList;
    }

    private Event importEvent(de.jobst.resulter.adapter.driver.web.jaxb.ResultList resultList,
                              Map<String, Organisation> organisationByName) {
        // Event
        Event event = Event.of(resultList.getEvent().getName(),
            new ArrayList<>(),
            resultList.getEvent()
                .getOrganisers()
                .stream()
                .map(x -> OrganisationId.of(organisationByName.get(x.getName()).getId().value()))
                .toList());
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
                             LocalDate.ofInstant(p.getBirthDate().toGregorianCalendar().toInstant(),
                                 ZoneId.systemDefault()) :
                             null),
                Gender.of(p.getSex())))
            .collect(Collectors.toSet());
        persons = personService.findOrCreate(persons);
        return persons.stream().collect(Collectors.toMap(Person::getDomainKey, x -> x));
    }

    @NonNull
    private Map<String, Organisation> importOrganisations(de.jobst.resulter.adapter.driver.web.jaxb.ResultList resultList,
                                                          Map<String, Country> countriesByCode) {
        Collection<Organisation> organisations = Stream.concat(
            // organisations from event
            resultList.getEvent()
                .getOrganisers()
                .stream()
                .map(o -> Organisation.of(OrganisationId.empty().value(),
                    o.getName(),
                    o.getShortName(),
                    OrganisationType.OTHER.value(),
                    (o.getCountry() == null ?
                     CountryId.empty() :
                     countriesByCode.get(o.getCountry().getCode()).getId()),
                    new ArrayList<>())),
            // organisations from persons
            resultList.getClassResults()
                .stream()
                .flatMap(x -> x.getPersonResults().stream())
                .map(de.jobst.resulter.adapter.driver.web.jaxb.PersonResult::getOrganisation)
                .map(o -> Organisation.of(OrganisationId.empty().value(),
                    o.getName(),
                    o.getShortName(),
                    OrganisationType.OTHER.value(),
                    (o.getCountry() == null ?
                     CountryId.empty() :
                     countriesByCode.get(o.getCountry().getCode()).getId()),
                    new ArrayList<>()))).collect(Collectors.toSet());

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
                .map(o -> o.getCountry() == null ?
                          null :
                          Country.of(o.getCountry().getCode(), o.getCountry().getValue())),
            // countries from persons
            resultList.getClassResults()
                .stream()
                .flatMap(x -> x.getPersonResults().stream())
                .map(de.jobst.resulter.adapter.driver.web.jaxb.PersonResult::getOrganisation)
                .map(o -> o.getCountry() == null ?
                          null :
                          Country.of(o.getCountry().getCode(), o.getCountry().getValue()))).collect(Collectors.toSet());

        countries = this.countryService.findOrCreate(countries);
        return countries.stream().collect(Collectors.toMap(x -> x.getCode().value(), x -> x));
    }

    private Person.DomainKey createPersonDomainKey(de.jobst.resulter.adapter.driver.web.jaxb.Person p) {
        return new Person.DomainKey(p.getName().getFamily(),
            p.getName().getGiven(),
            ObjectUtils.isNotEmpty(p.getBirthDate()) ?
            LocalDate.ofInstant(p.getBirthDate().toGregorianCalendar().toInstant(), ZoneId.systemDefault()) :
            null);
    }

    @NonNull
    private List<Pair<PersonResult, List<SplitTimeList>>> getPersonResults(de.jobst.resulter.adapter.driver.web.jaxb.ClassResult classResult,
                                                                           EventId eventId,
                                                                           ResultListId resultListId,
                                                                           ClassResultShortName classResultShortName,
                                                                           Map<String, Organisation> organisationByName,
                                                                           Map<Person.DomainKey, Person> personByDomainKey) {
        return classResult.getPersonResults().stream().map(personResult -> {
            PersonId personId = Objects.nonNull(personResult.getPerson()) ?
                                personByDomainKey.get(createPersonDomainKey(personResult.getPerson())).getId() :
                                null;
            Pair<List<PersonRaceResult>, List<SplitTimeList>> personRaceResults =
                convertListPairToListsPair(getPersonRaceResults(personResult,
                    eventId,
                    resultListId,
                    classResultShortName,
                    personId));
            return Pair.of(PersonResult.of(personId,
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
