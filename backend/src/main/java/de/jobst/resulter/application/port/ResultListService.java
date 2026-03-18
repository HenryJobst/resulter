package de.jobst.resulter.application.port;

import de.jobst.resulter.domain.*;
import org.jmolecules.architecture.hexagonal.PrimaryPort;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@PrimaryPort
public interface ResultListService {

    ResultList findOrCreate(ResultList resultList);

    Optional<ResultList> findById(ResultListId resultListId);

    List<ResultList> findAll();

    ResultList update(ResultList resultList);

    Collection<ResultList> findByEventId(EventId id);
    Map<EventId, List<ResultList>> findAllByEventIds(Collection<EventId> eventIds);

    Set<ResultListId> findResultListIdsWithSplitTimes(Collection<ResultListId> resultListIds);

    @Transactional
    List<CupScoreList> calculateScore(ResultListId id);

    @Transactional
    CertificateService.Certificate createCertificate(
            ResultListId resultListId, ClassResultShortName classResultShortName, PersonId personId);

    CertificateService.Certificate createCertificate(Event event, EventCertificate eventCertificate);

    List<EventCertificateStat> getCertificateStats(EventId eventId);

    void deleteEventCertificateStat(EventCertificateStatId id);

    List<CupScoreList> getCupScoreLists(ResultListId resultListId);

    List<CupScoreList> getCupScoreLists(ResultListId resultListId, CupId cupId);

    Map<ResultListId, List<CupScoreList>> getCupScoreListsByResultListIds(Collection<ResultListId> resultListIds, CupId cupId);
}
