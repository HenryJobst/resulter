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
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Service
public class XMLImportService {

    private final XmlParser xmlParser;

    private final EventService eventService;
    private final PersonService personService;
    private final OrganisationService organisationService;
    private final CountryService countryService;

    public XMLImportService(XmlParser xmlParser, EventService eventService,
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
        return personResult.getResults().stream().map(personRaceResult ->
                PersonRaceResult.of(
                        personRaceResult.getRaceNumber().longValue(),
                        ObjectUtils.isNotEmpty(personRaceResult.getStartTime()) ?
                                personRaceResult.getStartTime().toInstant().atZone(
                                        personRaceResult.getStartTime().getTimeZone().toZoneId()) : null,
                        ObjectUtils.isNotEmpty(personRaceResult.getFinishTime()) ?
                                personRaceResult.getFinishTime()
                                        .toInstant()
                                        .atZone(personRaceResult.getFinishTime().getTimeZone().toZoneId()) : null,
                        ObjectUtils.isNotEmpty(personRaceResult.getTime()) ?
                                personRaceResult.getTime() :
                                null,
                        Objects.nonNull(personRaceResult.getPosition()) ?
                                personRaceResult.getPosition().longValue() :
                                null,
                        ResultStatus.fromValue(personRaceResult.getStatus().value()),
                        getSplitTimes(personRaceResult)
                )).toList();
    }

    @NonNull
    private static List<SplitTime> getSplitTimes(de.jobst.resulter.adapter.driver.web.jaxb.PersonRaceResult personRaceResult) {
        return personRaceResult.getSplitTimes().stream().map(
                splitTime ->
                        SplitTime.of(
                                splitTime.getControlCode(),
                                splitTime.getTime()
                        )
        ).toList();
    }

    @NonNull
    private Person getPerson(de.jobst.resulter.adapter.driver.web.jaxb.PersonResult personResult) {
        de.jobst.resulter.adapter.driver.web.jaxb.Person person = personResult.getPerson();
        return personService.findOrCreate(
                Person.of(
                        PersonName.of(
                                person.getName().getFamily(),
                                person.getName().getGiven()
                        ),
                        BirthDate.of(
                                ObjectUtils.isNotEmpty(person
                                        .getBirthDate()) ?
                                        LocalDate.ofInstant(person
                                                        .getBirthDate()
                                                        .toGregorianCalendar()
                                                        .toInstant(),
                                                ZoneId.systemDefault())
                                        : null
                        ),
                        Gender.of(person.getSex()))
        );
    }

    Event importFile(InputStream inputStream) throws Exception {
        ResultList resultList = xmlParser.parseXmlFile(inputStream);

        Collection<ClassResult> classResults =
                resultList.getClassResults().stream()
                        .map(classResult -> {
                            Class clazz = classResult.getClazz();
                            return ClassResult.of(
                                    clazz.getName(),
                                    clazz.getShortName(),
                                    Gender.of(clazz.getSex()),
                                    getPersonResults(classResult)
                            );
                        }).toList();

        return eventService.findOrCreate(
                Event.of(resultList.getEvent().getName(),
                        classResults,
                        resultList.getEvent().getOrganisers().stream().map(
                                o -> Organisation.of(o.getName(), o.getShortName(),
                                        o.getCountry() == null ? null :
                                                Country.of(o.getCountry().getCode(),
                                                        o.getCountry().getValue()))
                        ).toList()
                )
        );
    }

    @NonNull
    private List<PersonResult> getPersonResults(de.jobst.resulter.adapter.driver.web.jaxb.ClassResult classResult) {
        return classResult.getPersonResults().stream().map(personResult ->
                PersonResult.of(
                        getPerson(personResult),
                        getOrganisation(personResult),
                        getPersonRaceResults(personResult)
                )).toList();
    }

    @Nullable
    private Organisation getOrganisation(de.jobst.resulter.adapter.driver.web.jaxb.PersonResult personResult) {
        de.jobst.resulter.adapter.driver.web.jaxb.Organisation organisation = personResult.getOrganisation();
        return Objects.nonNull(organisation) ?
                organisationService.findOrCreate(
                        Organisation.of(
                                organisation.getName(),
                                organisation.getShortName(),
                                organisation.getCountry() != null ?
                                        countryService.findOrCreate(
                                                Country.of(organisation.getCountry().getCode(),
                                                        organisation.getCountry().getValue())) : null
                        )
                ) :
                null;
    }
}
