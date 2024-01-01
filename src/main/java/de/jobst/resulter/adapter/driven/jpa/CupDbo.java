package de.jobst.resulter.adapter.driven.jpa;

import de.jobst.resulter.domain.Cup;
import de.jobst.resulter.domain.CupConfig;
import de.jobst.resulter.domain.CupId;
import de.jobst.resulter.domain.CupType;
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
    @JoinTable(
            name = "CUP_EVENT",
            joinColumns = @JoinColumn(name = "CUP_ID"),
            inverseJoinColumns = @JoinColumn(name = "EVENT_ID"))
    private Set<EventDbo> events = new HashSet<>();

    @Column(name = "TYPE")
    @Enumerated(value = EnumType.STRING)
    private CupType type;

    public static CupDbo from(@NonNull Cup cup, DboResolvers dboResolvers) {
        CupDbo cupDbo;
        CupDbo persistedCupDbo;
        if (cup.getId().value() != CupId.empty().value()) {
            cupDbo = dboResolvers.cupDboDboResolver().findDboById(cup.getId());
            persistedCupDbo = cupDbo;
        } else {
            cupDbo = new CupDbo();
            persistedCupDbo = null;
        }

        cupDbo.setName(cup.getName().value());

        if (cup.getEvents().isLoaded()) {
            cupDbo.setEvents(Objects.requireNonNull(cup.getEvents().get())
                    .value().stream().map(it -> {
                        EventDbo persistedEventDbo =
                                persistedCupDbo != null ?
                                        (persistedCupDbo.getEvents()
                                                .stream()
                                                .filter(x -> x.getId() == it.getId().value())
                                                .findFirst()
                                                .orElse(null))
                                        : null;
                        return EventDbo.from(it, (id) -> persistedEventDbo, dboResolvers);
                    }).collect(Collectors.toSet()));
        } else if (persistedCupDbo != null) {
            cupDbo.setEvents(persistedCupDbo.getEvents());
        } else if (cup.getId().isPersistent()) {
            throw new IllegalArgumentException();
        }

        if (ObjectUtils.isNotEmpty(cup.getType())) {
            cupDbo.setType(cup.getType());
        }
        return cupDbo;
    }

    static public List<Cup> asCups(@NonNull CupConfig cupConfig, @NonNull List<CupDbo> cupDbos) {
        return cupDbos.stream()
                .map(it -> Cup.of(
                        it.id,
                        it.name,
                        it.type,
                        cupConfig.shallowLoads().contains(CupConfig.ShallowCupLoads.EVENTS) ? null :
                                it.events.stream()
                                        .map(x -> ObjectUtils.isNotEmpty(x) ?
                                                EventDbo.asEvent(cupConfig.eventConfig(), x) :
                                                null)
                                        .toList())
                )
                .toList();
    }

    static public Cup asCup(@NonNull CupConfig cupConfig, @NonNull CupDbo cupDbo) {
        return asCups(cupConfig, List.of(cupDbo)).getFirst();
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