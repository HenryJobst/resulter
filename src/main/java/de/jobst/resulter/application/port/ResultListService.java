package de.jobst.resulter.application.port;

import de.jobst.resulter.adapter.driver.web.dto.CupScoreListDto;
import de.jobst.resulter.adapter.driver.web.dto.EventCertificateDto;
import de.jobst.resulter.adapter.driver.web.dto.EventCertificateStatDto;
import de.jobst.resulter.application.certificate.CertificateServiceImpl;
import de.jobst.resulter.domain.*;
import org.jmolecules.architecture.hexagonal.PrimaryPort;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@PrimaryPort
public interface ResultListService {

    ResultList findOrCreate(ResultList resultList);

    Optional<ResultList> findById(ResultListId resultListId);

    List<ResultList> findAll();

    ResultList update(ResultList resultList);

    Collection<ResultList> findByEventId(EventId id);

    @Transactional
    List<CupScoreListDto> calculateScore(ResultListId id);

    @Transactional
    CertificateServiceImpl.Certificate createCertificate(
            ResultListId resultListId, ClassResultShortName classResultShortName, PersonId personId);

    CertificateServiceImpl.Certificate createCertificate(EventId eventId, EventCertificateDto eventCertificateDto);

    List<EventCertificateStatDto> getCertificateStats(EventId eventId);

    void deleteEventCertificateStat(EventCertificateStatId id);

    List<CupScoreList> getCupScoreLists(ResultListId resultListId);

    List<CupScoreList> getCupScoreLists(ResultListId resultListId, CupId cupId);
}
