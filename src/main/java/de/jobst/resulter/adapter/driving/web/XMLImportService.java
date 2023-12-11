package de.jobst.resulter.adapter.driving.web;

import de.jobst.resulter.adapter.driving.web.jaxb.ResultList;
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
import java.time.LocalDateTime;
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

    public XMLImportService(XmlParser xmlParser, EventService eventService,
                            PersonService personService,
                            OrganisationService organisationService) {
        this.xmlParser = xmlParser;
        this.eventService = eventService;
        this.personService = personService;
        this.organisationService = organisationService;
    }

    @NonNull
    private static List<PersonRaceResult> getPersonRaceResults(de.jobst.resulter.adapter.driving.web.jaxb.PersonResult personResult) {
        return personResult.getResults().stream().map(personRaceResult ->
                PersonRaceResult.of(
                        personRaceResult.getRaceNumber().longValue(),
                        LocalDateTime.ofInstant(personRaceResult.getStartTime()
                                .toInstant(), ZoneId.systemDefault()),
                        ObjectUtils.isNotEmpty(personRaceResult.getFinishTime()) ?
                                LocalDateTime.ofInstant(personRaceResult.getFinishTime()
                                                .toInstant(),
                                        ZoneId.systemDefault()) : null,
                        ObjectUtils.isNotEmpty(personRaceResult.getTime()) ?
                                personRaceResult.getTime() :
                                null,
                        Objects.nonNull(personRaceResult.getPosition()) ?
                                personRaceResult.getPosition().longValue() :
                                null,
                        personRaceResult.getStatus().value(),
                        getSplitTimes(personRaceResult)
                )).toList();
    }

    @NonNull
    private static List<SplitTime> getSplitTimes(de.jobst.resulter.adapter.driving.web.jaxb.PersonRaceResult personRaceResult) {
        return personRaceResult.getSplitTimes().stream().map(
                splitTime ->
                        SplitTime.of(
                                splitTime.getControlCode(),
                                splitTime.getTime()
                        )
        ).toList();
    }

    @NonNull private Person getPerson(de.jobst.resulter.adapter.driving.web.jaxb.PersonResult personResult) {
        return personService.findOrCreate(
                Person.of(
                        PersonName.of(
                                personResult.getPerson().getName().getFamily(),
                                personResult.getPerson().getName().getGiven()
                        ),
                        BirthDate.of(
                                ObjectUtils.isNotEmpty(personResult.getPerson()
                                        .getBirthDate()) ?
                                        LocalDate.ofInstant(personResult.getPerson()
                                                        .getBirthDate()
                                                        .toGregorianCalendar()
                                                        .toInstant(),
                                                ZoneId.systemDefault())
                                        : null
                        ),
                        Gender.of(personResult.getPerson().getSex()))
        );
    }

    Event importFile(InputStream inputStream) throws Exception {
        ResultList resultList = xmlParser.parseXmlFile(inputStream);

        Collection<ClassResult> classResults =
                resultList.getClassResults().stream()
                        .map(classResult -> ClassResult.of(
                                classResult.getClazz().getName(),
                                classResult.getClazz().getShortName(),
                                classResult.getClazz().getSex(),
                                getPersonResults(classResult)
                        )).toList();

        return eventService.findOrCreate(
                Event.of(resultList.getEvent().getName(),
                        classResults,
                        resultList.getEvent().getOrganisers().stream().map(
                                organisation -> Organisation.of(organisation.getName(), organisation.getShortName())
                        ).toList()
                ));
    }

    @NonNull
    private List<PersonResult> getPersonResults(de.jobst.resulter.adapter.driving.web.jaxb.ClassResult classResult) {
        return classResult.getPersonResults().stream().map(personResult ->
                PersonResult.of(
                        getPerson(personResult),
                        getOrganisation(personResult),
                        getPersonRaceResults(personResult)
                )).toList();
    }

    @Nullable
    private Organisation getOrganisation(de.jobst.resulter.adapter.driving.web.jaxb.PersonResult personResult) {
        return Objects.nonNull(personResult.getOrganisation()) ?
                organisationService.findOrCreate(
                        Organisation.of(
                                personResult.getOrganisation().getName(),
                                personResult.getOrganisation().getShortName())) :
                null;
    }
}
