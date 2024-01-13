package de.jobst.resulter.adapter.driven.jpa;

import de.jobst.resulter.domain.Cup;
import de.jobst.resulter.domain.CupId;
import de.jobst.resulter.domain.CupType;
import de.jobst.resulter.domain.EventId;
import jakarta.persistence.*;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.lang.NonNull;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings({"LombokSetterMayBeUsed", "LombokGetterMayBeUsed", "unused"})
@Entity
@Table(name = "CUP")
public class CupDbo {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "entity_generator_cup")
    @SequenceGenerator(name = "entity_generator_cup", sequenceName = "SEQ_CUP_ID", allocationSize = 1)
    @Column(name = "ID", nullable = false, unique = true)
    private Long id;

    @Column(name = "NAME", nullable = false, unique = true)
    private String name;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "CUP_EVENT", joinColumns = @JoinColumn(name = "CUP_ID"),
               inverseJoinColumns = @JoinColumn(name = "EVENT_ID"))
    private Set<EventDbo> events = new HashSet<>();

    @Column(name = "TYPE")
    @Enumerated(value = EnumType.STRING)
    private CupType type;

    public static CupDbo from(@NonNull Cup cup, @NonNull DboResolvers dboResolvers) {
        CupDbo cupDbo;
        CupDbo persistedCupDbo;
        if (cup.getId().value() != CupId.empty().value()) {
            cupDbo = dboResolvers.getCupDboDboResolver().findDboById(cup.getId());
        } else {
            cupDbo = new CupDbo();
        }

        cupDbo.setName(cup.getName().value());

        cupDbo.setEvents(Objects.requireNonNull(cup.getEventIds()
            .stream()
            .map(it -> dboResolvers.getEventDboResolver().findDboById(it))
            .collect(Collectors.toSet())));

        if (ObjectUtils.isNotEmpty(cup.getType())) {
            cupDbo.setType(cup.getType());
        }
        return cupDbo;
    }

    static public List<Cup> asCups(@NonNull List<CupDbo> cupDbos) {
        return cupDbos.stream()
            .map(it -> Cup.of(it.id, it.name, it.type, it.events.stream().map(x -> EventId.of(x.getId())).toList()))
            .toList();
    }

    static public Cup asCup(@NonNull CupDbo cupDbo) {
        return asCups(List.of(cupDbo)).getFirst();
    }

    public long getId() {
        return id != null ? id : CupId.empty().value();
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<EventDbo> getEvents() {
        return events;
    }

    public void setEvents(Set<EventDbo> events) {
        this.events = events;
    }

    public CupType getType() {
        return type;
    }

    public void setType(CupType type) {
        this.type = type;
    }
}
