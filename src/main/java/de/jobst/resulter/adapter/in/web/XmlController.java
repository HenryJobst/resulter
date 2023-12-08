package de.jobst.resulter.adapter.in.web;

import de.jobst.resulter.adapter.in.web.jaxb.ResultList;
import de.jobst.resulter.application.EventService;
import de.jobst.resulter.domain.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Objects;

@Controller
@Slf4j
public class XmlController {

    public static final String FILE = "file";
    private final XmlParser xmlParser;

    private final EventService eventService;

    @Autowired
    public XmlController(XmlParser xmlParser, EventService eventService) {
        this.xmlParser = xmlParser;
        this.eventService = eventService;
    }

    @PostMapping("/upload")
    @ResponseBody
    public ResponseEntity<ResultList> handleFileUpload(@RequestParam(FILE) MultipartFile file) {
        try {
            ResultList resultList = xmlParser.parseXmlFile(file.getInputStream());

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
                                                            )
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

            Event event = eventService.findOrCreate(
                    Event.of(resultList.getEvent().getName(),
                            classResults,
                            resultList.getEvent().getOrganisers().stream().map(
                                    organisation -> Organisation.of(organisation.getName(), organisation.getShortName())
                            ).toList()
                    ));

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage());
            if (Objects.nonNull(e.getCause())) {
                log.error(e.getCause().getMessage());
            }
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
