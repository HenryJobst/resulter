package de.jobst.resulter.adapter.driven.jpa;

import de.jobst.resulter.application.port.CupRepository;
import de.jobst.resulter.domain.Cup;
import de.jobst.resulter.domain.CupConfig;
import de.jobst.resulter.domain.CupId;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;

@Repository
@ConditionalOnProperty(name = "resulter.repository.inmemory", havingValue = "false")
public class CupRepositoryDataJpaAdapter implements CupRepository {

    private final CupJpaRepository cupJpaRepository;
    private final EntityManager entityManager;
    private final EventJpaRepository eventJpaRepository;

    public CupRepositoryDataJpaAdapter(CupJpaRepository cupJpaRepository, EntityManager entityManager,
                                       EventJpaRepository eventJpaRepository) {
        this.cupJpaRepository = cupJpaRepository;
        this.entityManager = entityManager;
        this.eventJpaRepository = eventJpaRepository;
    }

    @Transactional
    public DboResolver<CupId, CupDbo> getIdResolver() {
        return (CupId id) -> findDboById(id, CupConfig.full()).orElse(null);
    }

    @Override
    @Transactional
    public Cup save(Cup cup) {

        DboResolvers dboResolvers = DboResolvers.empty();
        dboResolvers.setCupDboDboResolver(
                id -> cupJpaRepository.findById(id.value()).orElseThrow());
        dboResolvers.setEventDboResolver(
                id -> eventJpaRepository.findById(id.value()).orElseThrow());

        CupDbo cupDbo = CupDbo.from(cup, null, dboResolvers);
        CupDbo savedCupEntity = cupJpaRepository.save(cupDbo);
        return CupDbo.asCup(CupConfig.empty(), savedCupEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cup> findAll(CupConfig cupConfig) {
        EntityGraph<?> entityGraph = getEntityGraph(cupConfig);

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<CupDbo> query = cb.createQuery(CupDbo.class);
        query.select(query.from(CupDbo.class));

        TypedQuery<CupDbo> typedQuery = entityManager.createQuery(query);
        typedQuery.setHint("jakarta.persistence.loadgraph", entityGraph);

        List<CupDbo> resultList = typedQuery.getResultList();
        return CupDbo.asCups(cupConfig, resultList);
    }

    private EntityGraph<CupDbo> getEntityGraph(CupConfig cupConfig) {
        EntityGraph<CupDbo> entityGraph = entityManager.createEntityGraph(CupDbo.class);

        if (!cupConfig.shallowLoads().contains(CupConfig.ShallowCupLoads.EVENTS)) {
            entityGraph.addSubgraph(CupDbo_.events);
        }
        return entityGraph;
    }

    @Transactional(readOnly = true)
    public Optional<CupDbo> findDboById(CupId cupId, CupConfig cupConfig) {
        @SuppressWarnings("SqlSourceToSinkFlow")
        TypedQuery<CupDbo> query = entityManager.createQuery(
                MessageFormat.format("SELECT e FROM {0} e WHERE e.{1} = :id",
                        CupDbo_.class_.getName(),
                        CupDbo_.id.getName()),
                CupDbo.class);
        query.setParameter("id", cupId.value());
        query.setHint("jakarta.persistence.loadgraph", getEntityGraph(cupConfig));

        return query.getResultStream().findFirst();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Cup> findById(CupId cupId, CupConfig cupConfig) {
        return findDboById(cupId, cupConfig).map(it -> CupDbo.asCup(cupConfig, it));
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
