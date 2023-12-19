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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;

@Repository
@ConditionalOnProperty(name = "resulter.repository.inmemory", havingValue = "false")
public class EventRepositoryDataJpaAdapter implements EventRepository {

    private final EventJpaRepository eventJpaRepository;
    private final EntityManager entityManager;

    public EventRepositoryDataJpaAdapter(EventJpaRepository eventJpaRepository, EntityManager entityManager) {
        this.eventJpaRepository = eventJpaRepository;
        this.entityManager = entityManager;
    }

    @Override
    public Event save(Event event) {
        EventDbo eventEntity = EventDbo.from(event);
        EventDbo savedEventEntity = eventJpaRepository.save(eventEntity);
        return EventDbo.asEvents(EventConfig.fromEvent(event), List.of(savedEventEntity)).getFirst();
    }

    @Override
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

        if (!eventConfig.shallowLoads().contains(EventConfig.ShallowLoads.EVENT_ORGANISATIONS)) {
            entityGraph.addSubgraph(EventDbo_.organisations);
        }

        if (!eventConfig.shallowLoads().contains(EventConfig.ShallowLoads.CLASS_RESULTS)) {
            Subgraph<ClassResultDbo> classResultSubgraph = entityGraph.addSubgraph(EventDbo_.classResults.getName());
            if (!eventConfig.shallowLoads().contains(EventConfig.ShallowLoads.PERSON_RESULTS)) {
                Subgraph<PersonResultDbo>
                        personResultSubgraph =
                        classResultSubgraph.addSubgraph(ClassResultDbo_.personResults.getName());
                if (!eventConfig.shallowLoads().contains(EventConfig.ShallowLoads.PERSON_RACE_RESULTS)) {
                    Subgraph<PersonRaceResultDbo>
                            personRaceResultSubgraph =
                            personResultSubgraph.addSubgraph(PersonResultDbo_.personRaceResults.getName());
                    if (!eventConfig.shallowLoads().contains(EventConfig.ShallowLoads.SPLIT_TIMES)) {
                        personRaceResultSubgraph.addSubgraph(PersonRaceResultDbo_.splitTimes);
                    }
                }
                if (!eventConfig.shallowLoads().contains(EventConfig.ShallowLoads.PERSONS)) {
                    personResultSubgraph.addAttributeNodes(PersonResultDbo_.person.getName());
                }
                if (!eventConfig.shallowLoads().contains(EventConfig.ShallowLoads.ORGANISATIONS)) {
                    personResultSubgraph.addAttributeNodes(PersonResultDbo_.organisation.getName());
                }
            }
        }
        return entityGraph;
    }

    @Override
    public Optional<Event> findById(EventId eventId, EventConfig eventConfig) {
        TypedQuery<EventDbo> query = entityManager.createQuery(
                MessageFormat.format("SELECT e FROM {0} e WHERE e.{1} = :id",
                        EventDbo_.class_.getName(),
                        EventDbo_.id.getName()),
                EventDbo.class);
        query.setParameter("id", eventId.value());
        query.setHint("jakarta.persistence.loadgraph", getEntityGraph(eventConfig));

        return query.getResultStream().findFirst().map(it -> EventDbo.asEvents(eventConfig, List.of(it)).getFirst());
    }

    @Override
    public Event findOrCreate(Event event) {
        Optional<EventDbo> optionalEventDbo =
                eventJpaRepository.findByName(event.getName().value());
        if (optionalEventDbo.isEmpty()) {
            optionalEventDbo = Optional.of(EventDbo.from(save(event)));
        }
        EventDbo eventDbo = optionalEventDbo.get();
        return EventDbo.asEvents(EventConfig.fromEvent(event), List.of(eventDbo)).getFirst();
    }
}
