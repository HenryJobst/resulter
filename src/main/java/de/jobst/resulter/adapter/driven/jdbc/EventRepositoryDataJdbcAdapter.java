package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.application.port.EventRepository;
import de.jobst.resulter.domain.Event;
import de.jobst.resulter.domain.EventId;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@ConditionalOnProperty(name = "resulter.repository.inmemory", havingValue = "false")
public class EventRepositoryDataJdbcAdapter implements EventRepository {

    private final EventJdbcRepository eventJdbcRepository;
    private final PersonJdbcRepository personJdbcRepository;
    private final OrganisationJdbcRepository organisationJdbcRepository;
    private final CountryJdbcRepository countryJdbcRepository;

    public EventRepositoryDataJdbcAdapter(EventJdbcRepository eventJdbcRepository,
                                          PersonJdbcRepository personJdbcRepository,
                                          OrganisationJdbcRepository organisationJdbcRepository,
                                          CountryJdbcRepository countryJdbcRepository) {
        this.eventJdbcRepository = eventJdbcRepository;
        this.personJdbcRepository = personJdbcRepository;
        this.organisationJdbcRepository = organisationJdbcRepository;
        this.countryJdbcRepository = countryJdbcRepository;
    }

    @Transactional
    public DboResolver<EventId, EventDbo> getIdResolver() {
        return (EventId id) -> findDboById(id).orElseThrow();
    }

    @Override
    @Transactional
    public Event save(Event event) {
        DboResolvers dboResolvers = DboResolvers.empty();
        dboResolvers.setEventDboResolver(getIdResolver());
        dboResolvers.setPersonDboResolver(id -> personJdbcRepository.findById(id.value()).orElseThrow());
        dboResolvers.setOrganisationDboResolver(id -> organisationJdbcRepository.findById(id.value()).orElseThrow());
        dboResolvers.setCountryDboResolver(id -> countryJdbcRepository.findById(id.value()).orElseThrow());
        EventDbo eventEntity = EventDbo.from(event, dboResolvers);
        /*
        if (Hibernate.isInitialized(eventEntity.getClassResults())) {
            var personsToSave = eventEntity.getClassResults()
                .stream()
                .filter(classResultDbo -> Hibernate.isInitialized(classResultDbo.getPersonResults()))
                .flatMap(classResultDbo -> classResultDbo.getPersonResults().stream())
                .filter(personResultDbo -> personResultDbo.getPerson() != null &&
                                           Hibernate.isInitialized(personResultDbo.getPerson()))
                .map(PersonResultDbo::getPerson)
                .toList();
            personJdbcRepository.saveAll(personsToSave);

            var countriesToSave = eventEntity.getClassResults()
                .stream()
                .filter(classResultDbo -> Hibernate.isInitialized(classResultDbo.getPersonResults()))
                .flatMap(classResultDbo -> classResultDbo.getPersonResults().stream())
                .filter(personResultDbo -> personResultDbo.getOrganisation() != null &&
                                           Hibernate.isInitialized(personResultDbo.getOrganisation()))
                .map(PersonResultDbo::getOrganisation)
                .filter(organisationDbo -> organisationDbo.getCountry() != null &&
                                           Hibernate.isInitialized(organisationDbo.getCountry()))
                .map(OrganisationDbo::getCountry)
                .toList();
            countryJdbcRepository.saveAll(countriesToSave);

            var organisationsToSave = eventEntity.getClassResults()
                .stream()
                .filter(classResultDbo -> Hibernate.isInitialized(classResultDbo.getPersonResults()))
                .flatMap(classResultDbo -> classResultDbo.getPersonResults().stream())
                .filter(personResultDbo -> personResultDbo.getOrganisation() != null &&
                                           Hibernate.isInitialized(personResultDbo.getOrganisation()))
                .map(PersonResultDbo::getOrganisation)
                .toList();
            organisationJdbcRepository.saveAll(organisationsToSave);
        }

         */
        EventDbo savedEventEntity = eventJdbcRepository.save(eventEntity);
        return EventDbo.asEvent(savedEventEntity);
    }

    @Override
    public void deleteEvent(Event event) {
        eventJdbcRepository.deleteById(event.getId().value());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Event> findAll() {
        List<EventDbo> resultList = new ArrayList<>();
        return EventDbo.asEvents(resultList);
    }

    @Transactional(readOnly = true)
    public Optional<EventDbo> findDboById(EventId eventId) {
        return Optional.empty();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Event> findById(EventId eventId) {
        return findDboById(eventId).map(EventDbo::asEvent);
    }

    @Override
    @Transactional
    public Event findOrCreate(Event event) {
        Optional<EventDbo> optionalEventDbo = eventJdbcRepository.findByName(event.getName().value());
        if (optionalEventDbo.isEmpty()) {
            return save(event);
        }
        return EventDbo.asEvent(optionalEventDbo.get());
    }

}
