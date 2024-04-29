package de.jobst.resulter.application;

import de.jobst.resulter.adapter.driver.web.dto.EventCertificateDto;
import de.jobst.resulter.application.certificate.CertificateService;
import de.jobst.resulter.application.port.*;
import de.jobst.resulter.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Instant;
import java.util.*;

@Service
public class ResultListService {

    private final ResultListRepository resultListRepository;
    private final CupRepository cupRepository;

    private final EventRepository eventRepository;

    private final OrganisationRepository organisationRepository;

    private final PersonRepository personRepository;

    private final CertificateService certificateService;

    private final MediaFileRepository mediaFileRepository;

    private final EventCertificateStatRepository eventCertificateStatRepository;

    public ResultListService(ResultListRepository resultListRepository,
                             CupRepository cupRepository,
                             EventRepository eventRepository,
                             OrganisationRepository organisationRepository,
                             PersonRepository personRepository,
                             CertificateService certificateService,
                             MediaFileRepository mediaFileRepository,
                             EventCertificateStatRepository eventCertificateStatRepository) {
        this.resultListRepository = resultListRepository;
        this.cupRepository = cupRepository;
        this.eventRepository = eventRepository;
        this.organisationRepository = organisationRepository;
        this.personRepository = personRepository;
        this.certificateService = certificateService;
        this.mediaFileRepository = mediaFileRepository;
        this.eventCertificateStatRepository = eventCertificateStatRepository;
    }

    public ResultList findOrCreate(ResultList resultList) {
        return resultListRepository.findOrCreate(resultList);
    }

    public Optional<ResultList> findById(ResultListId resultListId) {
        return resultListRepository.findById(resultListId);
    }

    public List<ResultList> findAll() {
        return resultListRepository.findAll();
    }

    public ResultList update(ResultList resultList) {
        return resultListRepository.update(resultList);
    }

    public Collection<ResultList> findByEventId(EventId id) {
        return resultListRepository.findByEventId(id);
    }

    @Transactional
    public ResultList calculateScore(ResultListId id) {
        Optional<ResultList> optionalResultList = resultListRepository.findById(id);
        if (optionalResultList.isEmpty()) {
            // no result list for id
            return null;
        }
        ResultList resultList = optionalResultList.get();
        Collection<Cup> cups = cupRepository.findByEvent(resultList.getEventId());
        if (cups.isEmpty()) {
            // no cups for this event
            return null;
        }

        Optional<Event> optionalEvent = eventRepository.findById(resultList.getEventId());
        if (optionalEvent.isEmpty()) {
            // no event
            return null;
        }
        Event event = optionalEvent.get();
        Map<OrganisationId, Organisation> organisationById =
            organisationRepository.loadOrganisationTree(resultList.getReferencedOrganisationIds());
        cups.forEach(resultList::calculate);
        return resultListRepository.save(resultList);
    }

    @Transactional
    public CertificateService.Certificate createCertificate(ResultListId resultListId,
                                                            ClassResultShortName classResultShortName,
                                                            PersonId personId) throws IOException {
        ResultList resultList = resultListRepository.findByResultListIdAndClassResultShortNameAndPersonId(resultListId,
            classResultShortName,
            personId);
        if (resultList == null || resultList.getClassResults() == null) {
            // no result list
            return null;
        }
        Optional<Person> optionalPerson = personRepository.findById(personId);
        if (optionalPerson.isEmpty()) {
            // no person
            return null;
        }
        Person person = optionalPerson.get();
        Optional<Event> optionalEvent = eventRepository.findById(resultList.getEventId());
        if (optionalEvent.isEmpty()) {
            // no event
            return null;
        }
        Event event = optionalEvent.get();

        Optional<PersonResult> personResult = resultList.getClassResults()
            .stream()
            .flatMap(classResult -> classResult.personResults().value().stream())
            .filter(x -> x.personId().equals(person.getId()))
            .findFirst();

        if (personResult.isEmpty()) {
            // no person result
            return null;
        }
        Optional<Organisation> organisation = organisationRepository.findById(personResult.get().organisationId());

        Optional<PersonRaceResult> personRaceResult =
            personResult.get().personRaceResults().value().stream().findFirst();
        if (personRaceResult.isEmpty()) {
            return null;
        }

        CertificateService.Certificate certificate = certificateService.createCertificate(person,
            organisation,
            event,
            Objects.requireNonNull(event.getCertificate()),
            personRaceResult.get());

        EventCertificateStat eventCertificateStat =
            EventCertificateStat.of(EventCertificateStatId.empty().value(), event, person, Instant.now());

        eventCertificateStatRepository.save(eventCertificateStat);

        return certificate;
    }

    public CertificateService.Certificate createCertificate(EventId eventId, EventCertificateDto eventCertificateDto)
        throws IOException {

        Optional<Event> optionalEvent = eventRepository.findById(eventId);
        if (optionalEvent.isEmpty()) {
            // no event
            return null;
        }
        Event event = optionalEvent.get();
        EventCertificate eventCertificate = EventCertificate.of(EventCertificateId.empty().value(),
            eventCertificateDto.name(),
            event,
            eventCertificateDto.layoutDescription(),
            mediaFileRepository.findById(MediaFileId.of(eventCertificateDto.blankCertificate().id())).orElse(null),
            eventCertificateDto.primary());

        return certificateService.createCertificate(event, eventCertificate);
    }

    public long countCertificates(EventId eventId) {
        return eventCertificateStatRepository.findAllByEvent(eventId).size();
    }
}
