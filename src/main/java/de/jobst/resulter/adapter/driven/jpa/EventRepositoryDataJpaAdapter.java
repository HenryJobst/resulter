package de.jobst.resulter.adapter.driven.jpa;

import de.jobst.resulter.application.port.EventRepository;
import de.jobst.resulter.domain.Event;
import de.jobst.resulter.domain.EventConfig;
import de.jobst.resulter.domain.EventId;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Subgraph;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import org.hibernate.Hibernate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;

@Repository
@ConditionalOnProperty(name = "resulter.repository.inmemory", havingValue = "false")
public class EventRepositoryDataJpaAdapter implements EventRepository {

    private final EventJpaRepository eventJpaRepository;
    private final EntityManager entityManager;
    private final PersonJpaRepository personJpaRepository;
    private final OrganisationJpaRepository organisationJpaRepository;

    public EventRepositoryDataJpaAdapter(EventJpaRepository eventJpaRepository, EntityManager entityManager,
                                         PersonJpaRepository personJpaRepository,
                                         OrganisationJpaRepository organisationJpaRepository) {
        this.eventJpaRepository = eventJpaRepository;
        this.entityManager = entityManager;
        this.personJpaRepository = personJpaRepository;
        this.organisationJpaRepository = organisationJpaRepository;
    }

    @Override
    @Transactional
    public Event save(Event event) {
        EventDbo persisted =
                event.getId().isPersistent() ? eventJpaRepository.findById(event.getId().value()).orElse(null) : null;
        EventDbo eventEntity = EventDbo.from(event, persisted);
        if (Hibernate.isInitialized(eventEntity.getClassResults())) {
            var personsToSave =
                    eventEntity.getClassResults()
                            .stream()
                            .filter(classResultDbo -> Hibernate.isInitialized(classResultDbo.getPersonResults()))
                            .flatMap(classResultDbo -> classResultDbo.getPersonResults().stream())
                            .filter(personResultDbo -> personResultDbo.getPerson() != null &&
                                    Hibernate.isInitialized(personResultDbo.getPerson()))
                            .map(PersonResultDbo::getPerson)
                            .toList();
            personJpaRepository.saveAll(personsToSave);
            var organisationsToSave =
                    eventEntity.getClassResults()
                            .stream()
                            .filter(classResultDbo -> Hibernate.isInitialized(classResultDbo.getPersonResults()))
                            .flatMap(classResultDbo -> classResultDbo.getPersonResults().stream())
                            .filter(personResultDbo -> personResultDbo.getOrganisation() != null &&
                                    Hibernate.isInitialized(personResultDbo.getOrganisation()))
                            .map(PersonResultDbo::getOrganisation)
                            .toList();
            organisationJpaRepository.saveAll(organisationsToSave);
        }
        EventDbo savedEventEntity = eventJpaRepository.save(eventEntity);
        return EventDbo.asEvent(EventConfig.fromEvent(event), savedEventEntity);
    }

    @Override
    public void deleteEvent(Event event) {
        eventJpaRepository.deleteById(event.getId().value());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Event> findAll(EventConfig eventConfig) {
        EntityGraph<?> entityGraph = getEntityGraph(eventConfig);

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<EventDbo> query = cb.createQuery(EventDbo.class);
        query.select(query.from(EventDbo.class));

        TypedQuery<EventDbo> typedQuery = entityManager.createQuery(query);
        typedQuery.setHint("jakarta.persistence.loadgraph", entityGraph);

        List<EventDbo> resultList = typedQuery.getResultList();
        return EventDbo.asEvents(eventConfig, resultList);
    }

    private EntityGraph<EventDbo> getEntityGraph(EventConfig eventConfig) {
        EntityGraph<EventDbo> entityGraph = entityManager.createEntityGraph(EventDbo.class);

        if (!eventConfig.shallowLoads().contains(EventConfig.ShallowEventLoads.EVENT_ORGANISATIONS)) {
            entityGraph.addSubgraph(EventDbo_.organisations);
        }

        if (!eventConfig.shallowLoads().contains(EventConfig.ShallowEventLoads.CLASS_RESULTS)) {
            Subgraph<ClassResultDbo> classResultSubgraph = entityGraph.addSubgraph(EventDbo_.classResults.getName());
            if (!eventConfig.shallowLoads().contains(EventConfig.ShallowEventLoads.PERSON_RESULTS)) {
                Subgraph<PersonResultDbo>
                        personResultSubgraph =
                        classResultSubgraph.addSubgraph(ClassResultDbo_.personResults.getName());
                if (!eventConfig.shallowLoads().contains(EventConfig.ShallowEventLoads.PERSON_RACE_RESULTS)) {
                    Subgraph<PersonRaceResultDbo>
                            personRaceResultSubgraph =
                            personResultSubgraph.addSubgraph(PersonResultDbo_.personRaceResults.getName());
                    if (!eventConfig.shallowLoads().contains(EventConfig.ShallowEventLoads.SPLIT_TIMES)) {
                        personRaceResultSubgraph.addSubgraph(PersonRaceResultDbo_.splitTimes);
                    }
                }
                if (!eventConfig.shallowLoads().contains(EventConfig.ShallowEventLoads.PERSONS)) {
                    personResultSubgraph.addAttributeNodes(PersonResultDbo_.person.getName());
                }
                if (!eventConfig.shallowLoads().contains(EventConfig.ShallowEventLoads.ORGANISATIONS)) {
                    personResultSubgraph.addAttributeNodes(PersonResultDbo_.organisation.getName());
                }
            }
        }
        return entityGraph;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Event> findById(EventId eventId, EventConfig eventConfig) {
        @SuppressWarnings("SqlSourceToSinkFlow")
        TypedQuery<EventDbo> query = entityManager.createQuery(
                MessageFormat.format("SELECT e FROM {0} e WHERE e.{1} = :id",
                        EventDbo_.class_.getName(),
                        EventDbo_.id.getName()),
                EventDbo.class);
        query.setParameter("id", eventId.value());
        query.setHint("jakarta.persistence.loadgraph", getEntityGraph(eventConfig));

        return query.getResultStream().findFirst().map(it -> EventDbo.asEvent(eventConfig, it));
    }

    @Override
    @Transactional
    public Event findOrCreate(Event event) {
        Optional<EventDbo> optionalEventDbo =
                eventJpaRepository.findByName(event.getName().value());
        if (optionalEventDbo.isEmpty()) {
            return save(event);
        }
        return EventDbo.asEvent(EventConfig.fromEvent(event), optionalEventDbo.get());
    }

}
