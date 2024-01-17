package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.application.port.CupRepository;
import de.jobst.resulter.domain.Cup;
import de.jobst.resulter.domain.CupId;
import de.jobst.resulter.domain.EventId;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@ConditionalOnProperty(name = "resulter.repository.inmemory", havingValue = "false")
public class CupRepositoryDataJdbcAdapter implements CupRepository {

    private final CupJdbcRepository cupJpaRepository;
    private final EventJdbcRepository eventJpaRepository;

    public CupRepositoryDataJdbcAdapter(CupJdbcRepository cupJpaRepository, EventJdbcRepository eventJpaRepository) {
        this.cupJpaRepository = cupJpaRepository;
        this.eventJpaRepository = eventJpaRepository;
    }

    @Transactional
    public DboResolver<CupId, CupDbo> getIdResolver() {
        return (CupId id) -> findDboById(id).orElse(null);
    }

    @Override
    @Transactional
    public Cup save(Cup cup) {

        DboResolvers dboResolvers = DboResolvers.empty();
        dboResolvers.setCupDboDboResolver(id -> cupJpaRepository.findById(id.value()).orElseThrow());
        dboResolvers.setEventDboResolver(id -> eventJpaRepository.findById(id.value()).orElseThrow());

        CupDbo cupDbo = CupDbo.from(cup, dboResolvers);
        CupDbo savedCupEntity = cupJpaRepository.save(cupDbo);
        return CupDbo.asCup(savedCupEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cup> findAll() {
        return CupDbo.asCups(this.cupJpaRepository.findAll());
    }

    @Transactional(readOnly = true)
    public Optional<CupDbo> findDboById(CupId cupId) {
        return Optional.empty();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Cup> findById(CupId cupId) {
        return findDboById(cupId).map(CupDbo::asCup);
    }

    @Override
    @Transactional
    public Cup findOrCreate(Cup cup) {
        Optional<CupDbo> cupEntity = cupJpaRepository.findByName(cup.getName().value());
        if (cupEntity.isEmpty()) {
            return save(cup);
        }
        CupDbo entity = cupEntity.get();
        return CupDbo.asCup(entity);
    }

    @Override
    public void deleteCup(Cup cup) {
        cupJpaRepository.deleteById(cup.getId().value());
    }

    @Override
    @Transactional
    public List<Cup> findByEvent(EventId eventId) {
        List<CupDbo> cups = cupJpaRepository.findByEventId(eventId.value());
        return CupDbo.asCups(cups);
    }
}
