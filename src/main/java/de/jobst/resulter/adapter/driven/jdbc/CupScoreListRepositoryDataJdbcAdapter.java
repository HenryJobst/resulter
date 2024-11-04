package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.application.port.CupScoreListRepository;
import de.jobst.resulter.domain.CupScoreList;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@ConditionalOnProperty(name = "resulter.repository.inmemory", havingValue = "false")
public class CupScoreListRepositoryDataJdbcAdapter implements CupScoreListRepository {

    private final CupScoreListJdbcRepository cupScoreListJdbcRepository;

    public CupScoreListRepositoryDataJdbcAdapter(CupScoreListJdbcRepository cupScoreListJdbcRepository) {
        this.cupScoreListJdbcRepository = cupScoreListJdbcRepository;
    }

    @Override
    @Transactional
    public List<CupScoreList> saveAll(List<CupScoreList> cupScoreLists) {
        DboResolvers dboResolvers = DboResolvers.empty();
        Iterable<CupScoreListDbo> savedCupScoreListDbos =
            cupScoreListJdbcRepository.saveAll(CupScoreListDbo.from(cupScoreLists, dboResolvers));
        return CupScoreListDbo.asCupScoreLists(savedCupScoreListDbos);
    }
}
