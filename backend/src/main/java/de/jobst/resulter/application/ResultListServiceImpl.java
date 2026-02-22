package de.jobst.resulter.application;

import de.jobst.resulter.application.certificate.CertificateServiceImpl;
import de.jobst.resulter.application.port.*;
import de.jobst.resulter.domain.*;
import de.jobst.resulter.springapp.config.SpringSecurityAuditorAware;
import org.jspecify.annotations.Nullable;
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

    private final EventCertificateStatRepository eventCertificateStatRepository;

    private final CupScoreListRepository cupScoreListRepository;

    private final SpringSecurityAuditorAware springSecurityAuditorAware;
    private final EventCertificateService eventCertificateService;
    private final MediaFileService mediaFileService;

    public ResultListServiceImpl(
            ResultListRepository resultListRepository,
            CupRepository cupRepository,
            EventRepository eventRepository,
            OrganisationRepository organisationRepository,
            PersonRepository personRepository,
            CertificateService certificateService, EventCertificateStatRepository eventCertificateStatRepository,
            CupScoreListRepository cupScoreListRepository,
            SpringSecurityAuditorAware springSecurityAuditorAware, EventCertificateService eventCertificateService,
            MediaFileService mediaFileService) {
        this.resultListRepository = resultListRepository;
        this.cupRepository = cupRepository;
        this.eventRepository = eventRepository;
        this.organisationRepository = organisationRepository;
        this.personRepository = personRepository;
        this.certificateService = certificateService;
        this.eventCertificateStatRepository = eventCertificateStatRepository;
        this.cupScoreListRepository = cupScoreListRepository;
        this.springSecurityAuditorAware = springSecurityAuditorAware;
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

    @Override
    public Map<EventId, List<ResultList>> findAllByEventIds(Collection<EventId> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Map.of();
        }

        return resultListRepository.findAllByEventIds(eventIds).stream()
                .collect(Collectors.groupingBy(ResultList::getEventId));
    }

    @Transactional
    @Override
    public List<CupScoreList> calculateScore(ResultListId id) {
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
        List<@Nullable CupScoreList> cupScoreLists = cups.stream()
                .map(cup ->
                        resultList.calculate(cup, creator, now, cup.getCupTypeCalculationStrategy(organisationById)))
                .collect(Collectors.toList());

        // Determine deletion scope based on date-based rules (event-wide vs ResultList-specific)
        Collection<ResultList> allEventResultLists = findByEventId(resultList.getEventId());
        boolean deleteEventWide = ResultListScoringService.shouldDeleteEventWide(
                allEventResultLists);

        if (deleteEventWide) {
            // Same-day scenario: delete all cup scores for the event
            cupScoreListRepository.deleteAllByEventId(resultList.getEventId());
        } else {
            // Multi-day scenario: delete only cup scores for this specific ResultList
            cupScoreListRepository.deleteAllByDomainKey(
                cupScoreLists.stream()
                    .filter(Objects::nonNull)
                    .map(CupScoreList::getDomainKey)
                    .collect(Collectors.toSet()));
        }

        return cupScoreListRepository.saveAll(cupScoreLists);
    }

    @Transactional
    @Override
    public CertificateServiceImpl.@Nullable Certificate createCertificate(
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
                .filter(x -> x.personId().equals(person.id()))
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
                EventCertificateStatId.empty().value(), event.getId(), person.id(), Instant.now());

        eventCertificateStatRepository.save(eventCertificateStat);

        return certificate;
    }

    @Override
    public CertificateServiceImpl.Certificate createCertificate(Event event, EventCertificate eventCertificate) {

        return certificateService.createCertificate(event, eventCertificate, mediaFileService);
    }

    @Override
    public List<EventCertificateStat> getCertificateStats(EventId eventId) {
        return eventCertificateStatRepository.findAllByEvent(eventId);
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
