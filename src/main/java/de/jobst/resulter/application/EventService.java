package de.jobst.resulter.application;

import de.jobst.resulter.application.port.EventRepository;
import de.jobst.resulter.application.port.OrganisationRepository;
import de.jobst.resulter.application.port.PersonRepository;
import de.jobst.resulter.domain.ClassResult;
import de.jobst.resulter.domain.Event;
import de.jobst.resulter.domain.EventId;
import de.jobst.resulter.domain.PersonResult;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class EventService {

    public final PersonRepository personRepository;
    public final OrganisationRepository organisationRepository;
    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository,
                        PersonRepository personRepository,
                        OrganisationRepository organisationRepository) {
        this.eventRepository = eventRepository;
        this.personRepository = personRepository;
        this.organisationRepository = organisationRepository;
    }

    @Transactional
    public Event findOrCreate(Event event) {
        Collection<ClassResult> classResults =
                Objects.requireNonNull(event.getClassResults()).value().stream().map(x ->
                        ClassResult.of(x.classResultName().value(), x.classResultShortName().value(), x.gender(),
                                x.personResults().value().stream().map(y ->
                                        PersonResult.of(
                                                personRepository.findOrCreate(y.person()),
                                                ObjectUtils.isNotEmpty(y.organisation()) ?
                                                        organisationRepository.findOrCreate(y.organisation()) :
                                                        y.organisation(),
                                                y.personRaceResults().value())).toList())
                ).toList();
        return eventRepository.findOrCreate(Event.of(ObjectUtils.isNotEmpty(event.getId()) ? event.getId().value() : 0L,
                event.getName().value(), classResults));
    }

    @Transactional
    public Optional<Event> findById(EventId eventId) {
        return eventRepository.findById(eventId);
    }

    @Transactional
    public List<Event> findAll() {
        return eventRepository.findAll();
    }

    @Transactional
    public Event updateEvent(Event event) {
        return eventRepository.save(event);
    }
}
