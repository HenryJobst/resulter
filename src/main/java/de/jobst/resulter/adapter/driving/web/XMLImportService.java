package de.jobst.resulter.adapter.driving.web;

import de.jobst.resulter.adapter.driving.web.jaxb.ResultList;
import de.jobst.resulter.application.EventService;
import de.jobst.resulter.domain.*;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Objects;

@Service
public class XMLImportService {

    private final XmlParser xmlParser;

    private final EventService eventService;

    public XMLImportService(XmlParser xmlParser, EventService eventService) {
        this.xmlParser = xmlParser;
        this.eventService = eventService;
    }

    Event importFile(InputStream inputStream) throws Exception {
        ResultList resultList = xmlParser.parseXmlFile(inputStream);

        Collection<ClassResult> classResults =
                resultList.getClassResults().stream()
                        .map(classResult -> ClassResult.of(
                                classResult.getClazz().getName(),
                                classResult.getClazz().getShortName(),
                                classResult.getClazz().getSex(),
                                classResult.getPersonResults().stream().map(personResult ->
                                        PersonResult.of(
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
                                                        Gender.of(personResult.getPerson().getSex())
                                                ),
                                                Objects.nonNull(personResult.getOrganisation()) ?
                                                        Organisation.of(
                                                                personResult.getOrganisation().getName(),
                                                                personResult.getOrganisation().getShortName()) :
                                                        null,
                                                personResult.getResults().stream().map(personRaceResult ->
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
                                                                personRaceResult.getSplitTimes().stream().map(
                                                                        splitTime ->
                                                                                SplitTime.of(
                                                                                        splitTime.getControlCode(),
                                                                                        splitTime.getTime()
                                                                                )
                                                                ).toList()
                                                        )).toList()
                                        )).toList()
                        )).toList();

        return eventService.findOrCreate(
                Event.of(resultList.getEvent().getName(),
                        classResults,
                        resultList.getEvent().getOrganisers().stream().map(
                                organisation -> Organisation.of(organisation.getName(), organisation.getShortName())
                        ).toList()
                ));
    }
}
