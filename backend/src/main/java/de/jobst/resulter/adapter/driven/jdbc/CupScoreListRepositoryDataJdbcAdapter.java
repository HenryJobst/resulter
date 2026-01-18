package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.application.port.CupScoreListRepository;
import de.jobst.resulter.domain.CupId;
import de.jobst.resulter.domain.CupScoreList;
import de.jobst.resulter.domain.EventId;
import de.jobst.resulter.domain.PersonId;
import de.jobst.resulter.domain.ResultListId;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
@ConditionalOnProperty(name = "resulter.repository.inmemory", havingValue = "false")
@Slf4j
public class CupScoreListRepositoryDataJdbcAdapter implements CupScoreListRepository {

    private final CupScoreListJdbcRepository cupScoreListJdbcRepository;
    private final CupScoreListJdbcCustomRepository cupScoreListJdbcCustomRepository;

    public CupScoreListRepositoryDataJdbcAdapter(
            CupScoreListJdbcRepository cupScoreListJdbcRepository,
            CupScoreListJdbcCustomRepository cupScoreListJdbcCustomRepository) {
        this.cupScoreListJdbcRepository = cupScoreListJdbcRepository;
        this.cupScoreListJdbcCustomRepository = cupScoreListJdbcCustomRepository;
    }

    @Override
    public void deleteAllByDomainKey(Set<CupScoreList.DomainKey> cupScoreList) {
        cupScoreListJdbcCustomRepository.deleteAllByDomainKeys(cupScoreList);
    }

    @Override
    public void deleteAllByEventId(EventId eventId) {
        cupScoreListJdbcCustomRepository.deleteAllByEventId(eventId.value());
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
        // 1. Load CupScoreListDbo without cupScores (avoiding N+1 queries)
        List<CupScoreListDbo> cupScoreListDbos =
                cupScoreListJdbcCustomRepository.findByResultListIdWithoutCupScores(resultListId.value());

        if (cupScoreListDbos.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. Batch load cupScores for all lists
        List<Long> listIds =
                cupScoreListDbos.stream().map(CupScoreListDbo::getId).toList();
        List<CupScoreListJdbcCustomRepository.CupScoreWithListId> cupScoresWithListIds =
                cupScoreListJdbcCustomRepository.findCupScoresByListIds(listIds);

        // 3. Populate associations
        Map<Long, Set<CupScoreDbo>> cupScoresByListId = cupScoresWithListIds.stream()
                .collect(Collectors.groupingBy(
                        CupScoreListJdbcCustomRepository.CupScoreWithListId::cupScoreListId,
                        Collectors.mapping(
                                CupScoreListJdbcCustomRepository.CupScoreWithListId::cupScore, Collectors.toSet())));

        cupScoreListDbos.forEach(dbo -> {
            Set<CupScoreDbo> cupScores = cupScoresByListId.getOrDefault(dbo.getId(), Collections.emptySet());
            dbo.setCupScores(cupScores);
        });

        // 4. Convert to domain entities
        return CupScoreListDbo.asCupScoreLists(cupScoreListDbos);
    }

    @Override
    public List<CupScoreList> findAllByResultListIdAndCupId(ResultListId resultListId, CupId cupId) {
        // 1. Load CupScoreListDbo without cupScores (avoiding N+1 queries)
        List<CupScoreListDbo> cupScoreListDbos =
                cupScoreListJdbcCustomRepository.findByResultListIdAndCupIdWithoutCupScores(
                        resultListId.value(), cupId.value());

        if (cupScoreListDbos.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. Batch load cupScores for all lists
        List<Long> listIds =
                cupScoreListDbos.stream().map(CupScoreListDbo::getId).toList();
        List<CupScoreListJdbcCustomRepository.CupScoreWithListId> cupScoresWithListIds =
                cupScoreListJdbcCustomRepository.findCupScoresByListIds(listIds);

        // 3. Populate associations
        Map<Long, Set<CupScoreDbo>> cupScoresByListId = cupScoresWithListIds.stream()
                .collect(Collectors.groupingBy(
                        CupScoreListJdbcCustomRepository.CupScoreWithListId::cupScoreListId,
                        Collectors.mapping(
                                CupScoreListJdbcCustomRepository.CupScoreWithListId::cupScore, Collectors.toSet())));

        cupScoreListDbos.forEach(dbo -> {
            Set<CupScoreDbo> cupScores = cupScoresByListId.getOrDefault(dbo.getId(), Collections.emptySet());
            dbo.setCupScores(cupScores);
        });

        // 4. Convert to domain entities
        return CupScoreListDbo.asCupScoreLists(cupScoreListDbos);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void replacePersonId(PersonId oldPersonId, PersonId newPersonId) {
        if (cupScoreListJdbcRepository.existsByPersonId(oldPersonId.value())) {
            long updatedRows =
                    cupScoreListJdbcRepository.replacePersonIdInCupScore(oldPersonId.value(), newPersonId.value());
            log.debug(
                    "Updated {} rows in cup_score_list with person_id {} to person_id {}",
                    updatedRows,
                    oldPersonId,
                    newPersonId);
        }
    }
}
