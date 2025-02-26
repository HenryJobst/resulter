package de.jobst.resulter.application;

import de.jobst.resulter.adapter.driver.web.dto.CupScoreListDto;
import de.jobst.resulter.adapter.driver.web.dto.EventCertificateDto;
import de.jobst.resulter.adapter.driver.web.dto.EventCertificateStatDto;
import de.jobst.resulter.application.port.CertificateService;
import de.jobst.resulter.application.certificate.CertificateServiceImpl;
import de.jobst.resulter.application.port.*;
import de.jobst.resulter.domain.*;
import de.jobst.resulter.domain.util.ResourceNotFoundException;
import de.jobst.resulter.springapp.config.SpringSecurityAuditorAware;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ResultListServiceImpl implements ResultListService {

    private final ResultListRepository resultListRepository;
    private final CupRepository cupRepository;

    private final EventRepository eventRepository;

    private final OrganisationRepository organisationRepository;

    private final PersonRepository personRepository;

    private final CertificateService certificateService;

    private final MediaFileRepository mediaFileRepository;

    private final EventCertificateStatRepository eventCertificateStatRepository;

    private final CupScoreListRepository cupScoreListRepository;

    private final SpringSecurityAuditorAware springSecurityAuditorAware;
    private final EventService eventService;
    private final PersonService personService;
    private final EventCertificateService eventCertificateService;
    private final MediaFileService mediaFileService;

    public ResultListServiceImpl(
            ResultListRepository resultListRepository,
            CupRepository cupRepository,
            EventRepository eventRepository,
            OrganisationRepository organisationRepository,
            PersonRepository personRepository,
            CertificateService certificateService,
            MediaFileRepository mediaFileRepository,
            EventCertificateStatRepository eventCertificateStatRepository,
            CupScoreListRepository cupScoreListRepository,
            SpringSecurityAuditorAware springSecurityAuditorAware,
            EventService eventService,
            PersonService personService,
            EventCertificateService eventCertificateService,
            MediaFileService mediaFileService) {
        this.resultListRepository = resultListRepository;
        this.cupRepository = cupRepository;
        this.eventRepository = eventRepository;
        this.organisationRepository = organisationRepository;
        this.personRepository = personRepository;
        this.certificateService = certificateService;
        this.mediaFileRepository = mediaFileRepository;
        this.eventCertificateStatRepository = eventCertificateStatRepository;
        this.cupScoreListRepository = cupScoreListRepository;
        this.springSecurityAuditorAware = springSecurityAuditorAware;
        this.eventService = eventService;
        this.personService = personService;
        this.eventCertificateService = eventCertificateService;
        this.mediaFileService = mediaFileService;
    }

    @Override
    public ResultList findOrCreate(ResultList resultList) {
        return resultListRepository.findOrCreate(resultList);
    }

    @Override
    public Optional<ResultList> findById(ResultListId resultListId) {
        return resultListRepository.findById(resultListId);
    }

    @Override
    public List<ResultList> findAll() {
        return resultListRepository.findAll();
    }

    @Override
    public ResultList update(ResultList resultList) {
        return resultListRepository.update(resultList);
    }

    @Override
    public Collection<ResultList> findByEventId(EventId id) {
        return resultListRepository.findByEventId(id);
    }

    @Transactional
    @Override
    public List<CupScoreListDto> calculateScore(ResultListId id) {
        Optional<ResultList> resultListOptional = findById(id);
        if (resultListOptional.isEmpty()
                || resultListOptional.get().getClassResults() == null
                || resultListOptional.get().getClassResults().isEmpty()) {
            // no result list for id
            return List.of();
        }
        ResultList resultList = resultListOptional.get();
        Collection<Cup> cups = cupRepository.findByEvent(resultList.getEventId());
        if (cups.isEmpty()) {
            // no cups for this event
            return List.of();
        }
        Set<OrganisationId> referencedOrganisationIds = resultList.getReferencedOrganisationIds();
        Map<OrganisationId, Organisation> organisationById =
                organisationRepository.loadOrganisationTree(referencedOrganisationIds);
        String creator = springSecurityAuditorAware.getCurrentAuditor().orElse(SpringSecurityAuditorAware.UNKNOWN);
        ZonedDateTime now = ZonedDateTime.now();
        List<CupScoreList> cupScoreLists = cups.stream()
                .map(cup ->
                        resultList.calculate(cup, creator, now, cup.getCupTypeCalculationStrategy(organisationById)))
                .collect(Collectors.toList());
        cupScoreListRepository.deleteAllByDomainKey(
                cupScoreLists.stream().map(CupScoreList::getDomainKey).collect(Collectors.toSet()));
        return cupScoreListRepository.saveAll(cupScoreLists).stream()
                .map(CupScoreListDto::from)
                .toList();
    }

    @Transactional
    @Override
    public CertificateServiceImpl.Certificate createCertificate(
        ResultListId resultListId, ClassResultShortName classResultShortName, PersonId personId) {
        ResultList resultList = resultListRepository.findByResultListIdAndClassResultShortNameAndPersonId(
                resultListId, classResultShortName, personId);
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

        Optional<PersonResult> personResult = resultList.getClassResults().stream()
                .flatMap(classResult -> classResult.personResults().value().stream())
                .filter(x -> x.personId().equals(person.getId()))
                .findFirst();

        if (personResult.isEmpty()) {
            // no person result
            return null;
        }
        Optional<Organisation> organisation =
                organisationRepository.findById(personResult.get().organisationId());

        Optional<PersonRaceResult> personRaceResult =
                personResult.get().personRaceResults().value().stream().findFirst();
        if (personRaceResult.isEmpty()) {
            return null;
        }

        CertificateServiceImpl.Certificate certificate = certificateService.createCertificate(
                person,
                organisation.orElse(null),
                event,
                eventCertificateService.getById(Objects.requireNonNull(event.getCertificate())),
                personRaceResult.get(),
                mediaFileService);

        EventCertificateStat eventCertificateStat = EventCertificateStat.of(
                EventCertificateStatId.empty().value(), event.getId(), person.getId(), Instant.now());

        eventCertificateStatRepository.save(eventCertificateStat);

        return certificate;
    }

    @Override
    public CertificateServiceImpl.Certificate createCertificate(EventId eventId, EventCertificateDto eventCertificateDto) {

        Event event =
                eventRepository.findById(eventId).orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        MediaFile blankCertificate = eventCertificateDto.blankCertificate() == null
                ? null
                : mediaFileRepository
                        .findById(MediaFileId.of(
                                eventCertificateDto.blankCertificate().id()))
                        .orElse(null);
        EventCertificate eventCertificate = EventCertificate.of(
                EventCertificateId.empty().value(),
                eventCertificateDto.name(),
                event.getId(),
                eventCertificateDto.layoutDescription(),
                blankCertificate != null ? blankCertificate.getId() : null,
                eventCertificateDto.primary());

        return certificateService.createCertificate(event, eventCertificate, mediaFileService);
    }

    @Override
    public List<EventCertificateStatDto> getCertificateStats(EventId eventId) {
        return eventCertificateStatRepository.findAllByEvent(eventId).stream()
                .map(x -> EventCertificateStatDto.from(x, eventService, personService))
                .toList();
    }

    @Override
    public void deleteEventCertificateStat(EventCertificateStatId id) {
        eventCertificateStatRepository.deleteById(id);
    }

    @Override
    public List<CupScoreList> getCupScoreLists(ResultListId resultListId) {
        return cupScoreListRepository.findAllByResultListId(resultListId);
    }

    @Override
    public List<CupScoreList> getCupScoreLists(ResultListId resultListId, CupId cupId) {
        return cupScoreListRepository.findAllByResultListIdAndCupId(resultListId, cupId);
    }
}
