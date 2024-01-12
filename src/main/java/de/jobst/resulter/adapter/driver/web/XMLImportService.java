package de.jobst.resulter.adapter.driver.web;

import de.jobst.resulter.adapter.driver.web.jaxb.Class;
import de.jobst.resulter.adapter.driver.web.jaxb.ResultList;
import de.jobst.resulter.application.CountryService;
import de.jobst.resulter.application.EventService;
import de.jobst.resulter.application.OrganisationService;
import de.jobst.resulter.application.PersonService;
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
    private final PersonService personService;
    private final OrganisationService organisationService;
    private final CountryService countryService;

    public XMLImportService(XmlParser xmlParser,
                            EventService eventService,
                            PersonService personService,
                            OrganisationService organisationService,
                            CountryService countryService) {
        this.xmlParser = xmlParser;
        this.eventService = eventService;
        this.personService = personService;
        this.organisationService = organisationService;
        this.countryService = countryService;
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
                ResultStatus.fromValue(personRaceResult.getStatus().value()),
                getSplitTimes(personRaceResult)))
            .toList();
    }

    @NonNull
    private static List<SplitTime> getSplitTimes(de.jobst.resulter.adapter.driver.web.jaxb.PersonRaceResult personRaceResult) {
        return personRaceResult.getSplitTimes()
            .stream()
            .map(splitTime -> SplitTime.of(splitTime.getControlCode(), splitTime.getTime()))
            .toList();
    }

    @NonNull
    private Person getPerson(de.jobst.resulter.adapter.driver.web.jaxb.PersonResult personResult) {
        de.jobst.resulter.adapter.driver.web.jaxb.Person person = personResult.getPerson();
        return personService.findOrCreate(Person.of(PersonName.of(person.getName().getFamily(),
                person.getName().getGiven()),
            BirthDate.of(ObjectUtils.isNotEmpty(person.getBirthDate()) ?
                         LocalDate.ofInstant(person.getBirthDate().toGregorianCalendar().toInstant(),
                             ZoneId.systemDefault()) :
                         null),
            Gender.of(person.getSex())));
    }

    Event importFile(InputStream inputStream) throws Exception {
        ResultList resultList = xmlParser.parseXmlFile(inputStream);

        // countries
        Collection<Country> countries = resultList.getEvent()
            .getOrganisers()
            .stream()
            .map(o -> o.getCountry() == null ? null : Country.of(o.getCountry().getCode(), o.getCountry().getValue()))
            .toList();
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
                    (o.getCountry() == null ? null : countriesByCode.get(o.getCountry().getCode()).getId()),
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
                    (o.getCountry() == null ? null : countriesByCode.get(o.getCountry().getCode()).getId()),
                    new ArrayList<>()))).toList();

        organisations = organisationService.findOrCreate(organisations);
        Map<String, Organisation> organisationByName =
            organisations.stream().collect(Collectors.toMap(x -> x.getName().value(), x -> x));

        // Result list
        Collection<ClassResult> classResults = resultList.getClassResults().stream().map(classResult -> {
            Class clazz = classResult.getClazz();
            return ClassResult.of(clazz.getName(),
                clazz.getShortName(),
                Gender.of(clazz.getSex()),
                getPersonResults(classResult, organisationByName));
        }).toList();

        return eventService.findOrCreate(Event.of(resultList.getEvent().getName(),
            classResults,
            resultList.getEvent()
                .getOrganisers()
                .stream()
                .map(x -> OrganisationId.of(organisationByName.get(x.getName()).getId().value()))
                .toList()));
    }

    @NonNull
    private List<PersonResult> getPersonResults(de.jobst.resulter.adapter.driver.web.jaxb.ClassResult classResult,
                                                Map<String, Organisation> organisationByName) {
        return classResult.getPersonResults()
            .stream()
            .map(personResult -> PersonResult.of(getPerson(personResult),
                Objects.nonNull(personResult.getOrganisation()) ?
                organisationByName.get(personResult.getOrganisation().getName()).getId() :
                null,
                getPersonRaceResults(personResult)))
            .toList();
    }

}
