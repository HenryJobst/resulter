package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.application.port.CupScoreListRepository;
import de.jobst.resulter.domain.CupId;
import de.jobst.resulter.domain.CupScoreList;
import de.jobst.resulter.domain.PersonId;
import de.jobst.resulter.domain.ResultListId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Repository
@ConditionalOnProperty(name = "resulter.repository.inmemory", havingValue = "false")
@Slf4j
public class CupScoreListRepositoryDataJdbcAdapter implements CupScoreListRepository {

    private final CupScoreListJdbcRepository cupScoreListJdbcRepository;
    private final CupScoreListJdbcCustomRepository cupScoreListJdbcCustomRepository;

    public CupScoreListRepositoryDataJdbcAdapter(CupScoreListJdbcRepository cupScoreListJdbcRepository,
                                                 CupScoreListJdbcCustomRepository cupScoreListJdbcCustomRepository) {
        this.cupScoreListJdbcRepository = cupScoreListJdbcRepository;
        this.cupScoreListJdbcCustomRepository = cupScoreListJdbcCustomRepository;
    }

    @Override
    public void deleteAllByDomainKey(Set<CupScoreList.DomainKey> cupScoreList) {
        cupScoreListJdbcCustomRepository.deleteAllByDomainKeys(cupScoreList);
    }

    @Override
    @Transactional
    public List<CupScoreList> saveAll(List<CupScoreList> cupScoreLists) {
        DboResolvers dboResolvers = DboResolvers.empty();
        List<CupScoreListDbo> cupScoreListDbos = CupScoreListDbo.from(cupScoreLists, dboResolvers);
        Iterable<CupScoreListDbo> savedCupScoreListDbos = cupScoreListJdbcRepository.saveAll(cupScoreListDbos);
        return CupScoreListDbo.asCupScoreLists(savedCupScoreListDbos);
    }

    @Override
    public List<CupScoreList> findAllByResultListId(ResultListId resultListId) {
        return CupScoreListDbo.asCupScoreLists(cupScoreListJdbcRepository.findByResultListId(AggregateReference.to(
            resultListId.value())));
    }

    @Override
    public List<CupScoreList> findAllByResultListIdAndCupId(ResultListId resultListId, CupId cupId) {
        return CupScoreListDbo.asCupScoreLists(cupScoreListJdbcRepository.findByResultListIdAndCupId(AggregateReference.to(
            resultListId.value()), AggregateReference.to(cupId.value())));
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void replacePersonId(PersonId oldPersonId, PersonId newPersonId) {
        if (cupScoreListJdbcRepository.existsByPersonId(oldPersonId.value())) {
            long updatedRows =
                cupScoreListJdbcRepository.replacePersonIdInCupScore(oldPersonId.value(), newPersonId.value());
            log.debug("Updated {} rows in cup_score_list with person_id {} to person_id {}",
                updatedRows,
                oldPersonId,
                newPersonId);
        }
    }
}
