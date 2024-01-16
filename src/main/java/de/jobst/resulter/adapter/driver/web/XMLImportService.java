package de.jobst.resulter.adapter.driver.web;

import de.jobst.resulter.application.*;
import de.jobst.resulter.domain.*;
import org.apache.commons.lang3.ObjectUtils;
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

    public XMLImportService(XmlParser xmlParser,
                            EventService eventService,
                            CountryService countryService,
                            OrganisationService organisationService,
                            PersonService personService,
                            ResultListService resultListService) {
        this.xmlParser = xmlParser;
        this.eventService = eventService;
        this.countryService = countryService;
        this.organisationService = organisationService;
        this.personService = personService;
        this.resultListService = resultListService;
    }

    @NonNull
    private static List<PersonRaceResult> getPersonRaceResults(de.jobst.resulter.adapter.driver.web.jaxb.PersonResult personResult) {
        return personResult.getResults()
            .stream()
            .map(personRaceResult -> PersonRaceResult.of(personRaceResult.getRaceNumber().longValue(),
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
                ResultStatus.fromValue(personRaceResult.getStatus().value())))
            .toList();
    }

    public record ImportResult(Event event, Map<String, Country> countryMap, Map<String, Organisation> organisationMap,
                               Map<Person.DomainKey, Person> personMap, ResultList resultLists) {

    }

    ImportResult importFile(InputStream inputStream) throws Exception {
        var resultList = xmlParser.parseXmlFile(inputStream);

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
        Map<String, Country> countriesByCode =
            countries.stream().collect(Collectors.toMap(x -> x.getCode().value(), x -> x));

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
        Map<String, Organisation> organisationByName =
            organisations.stream().collect(Collectors.toMap(x -> x.getName().value(), x -> x));

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
        Map<Person.DomainKey, Person> personByDomainKey =
            persons.stream().collect(Collectors.toMap(Person::getDomainKey, x -> x));

        // Event
        Event event = Event.of(resultList.getEvent().getName(),
            new ArrayList<>(),
            resultList.getEvent()
                .getOrganisers()
                .stream()
                .map(x -> OrganisationId.of(organisationByName.get(x.getName()).getId().value()))
                .toList());
        event = eventService.findOrCreate(event);

        Collection<ClassResult> classResults = resultList.getClassResults().stream().map(classResult -> {
            var clazz = classResult.getClazz();
            return ClassResult.of(clazz.getName(),
                clazz.getShortName(),
                Gender.of(clazz.getSex()),
                getPersonResults(classResult, organisationByName, personByDomainKey));
        }).toList();

        // Result list
        ResultList domainResultList = new ResultList(ResultListId.empty(),
            event.getId(),
            resultList.getCreator(),
            ObjectUtils.isNotEmpty(resultList.getCreateTime()) ?
            resultList.getCreateTime().toInstant().atZone(resultList.getCreateTime().getTimeZone().toZoneId()) :
            null,
            resultList.getStatus(),
            classResults);
        domainResultList = resultListService.findOrCreate(domainResultList);

        return new ImportResult(event, countriesByCode, organisationByName, personByDomainKey, domainResultList);
    }

    private Person.DomainKey createPersonDomainKey(de.jobst.resulter.adapter.driver.web.jaxb.Person p) {
        return new Person.DomainKey(p.getName().getFamily(),
            p.getName().getGiven(),
            ObjectUtils.isNotEmpty(p.getBirthDate()) ?
            LocalDate.ofInstant(p.getBirthDate().toGregorianCalendar().toInstant(), ZoneId.systemDefault()) :
            null);
    }

    @NonNull
    private List<PersonResult> getPersonResults(de.jobst.resulter.adapter.driver.web.jaxb.ClassResult classResult,
                                                Map<String, Organisation> organisationByName,
                                                Map<Person.DomainKey, Person> personByDomainKey) {
        return classResult.getPersonResults()
            .stream()
            .map(personResult -> PersonResult.of(Objects.nonNull(personResult.getPerson()) ?
                                                 personByDomainKey.get(createPersonDomainKey(personResult.getPerson()))
                                                     .getId() :
                                                 null,
                Objects.nonNull(personResult.getOrganisation()) ?
                organisationByName.get(personResult.getOrganisation().getName()).getId() :
                null,
                getPersonRaceResults(personResult)))
            .toList();
    }

}
