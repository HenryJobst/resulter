package de.jobst.resulter.adapter.driven.jpa;

import de.jobst.resulter.application.port.CupRepository;
import de.jobst.resulter.domain.Cup;
import de.jobst.resulter.domain.CupConfig;
import de.jobst.resulter.domain.CupId;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@ConditionalOnProperty(name = "resulter.repository.inmemory", havingValue = "false")
public class CupRepositoryDataJpaAdapter implements CupRepository {

    private final CupJpaRepository cupJpaRepository;

    public CupRepositoryDataJpaAdapter(CupJpaRepository cupJpaRepository) {
        this.cupJpaRepository = cupJpaRepository;
    }

    @Override
    @Transactional
    public Cup save(Cup cup) {
        CupDbo persisted =
                cup.getId().isPersistent() ?
                        cupJpaRepository.findById(cup.getId().value()).orElse(null) :
                        null;
        CupDbo cupDbo = CupDbo.from(cup, persisted);
        CupDbo savedCupEntity = cupJpaRepository.save(cupDbo);
        return CupDbo.asCup(CupConfig.empty(), savedCupEntity);
    }

    @Override
    @Transactional
    public List<Cup> findAll() {
        return cupJpaRepository.findAll().stream()
                .map(it -> CupDbo.asCup(CupConfig.full(), it))
                .sorted()
                .toList();
    }

    @Override
    @Transactional
    public Optional<Cup> findById(CupId cupId) {
        Optional<CupDbo> cupEntity =
                cupJpaRepository.findById(cupId.value());
        return cupEntity.map(it -> CupDbo.asCup(CupConfig.empty(), it));
    }

    @Override
    @Transactional
    public Cup findOrCreate(Cup cup) {
        Optional<CupDbo> cupEntity =
                cupJpaRepository.findByName(cup.getName().value());
        if (cupEntity.isEmpty()) {
            return save(cup);
        }
        CupDbo entity = cupEntity.get();
        return CupDbo.asCup(CupConfig.empty(), entity);
    }
}
