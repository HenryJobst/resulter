package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.domain.Cup;
import de.jobst.resulter.domain.CupId;
import de.jobst.resulter.domain.CupType;
import de.jobst.resulter.domain.EventId;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.With;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.NonNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__(@PersistenceCreator))
@Table(name = "CUP")
public class CupDbo {

    @Id
    @With
    private Long id;

    private String name;

    @MappedCollection(idColumn = "EVENT_ID")
    private Set<CupEventDbo> events = new HashSet<>();

    private CupType type;

    public CupDbo(String name) {
        this.id = null;
        this.name = name;
    }

    public static CupDbo from(@NonNull Cup cup, @NonNull DboResolvers dboResolvers) {
        CupDbo cupDbo;
        if (cup.getId().value() != CupId.empty().value()) {
            cupDbo = dboResolvers.getCupDboDboResolver().findDboById(cup.getId());
            cupDbo.setName(cup.getName().value());
        } else {
            cupDbo = new CupDbo(cup.getName().value());
        }

        cupDbo.setEvents(cup.getEventIds().stream().map(it -> new CupEventDbo(it.value())).collect(Collectors.toSet()));

        if (ObjectUtils.isNotEmpty(cup.getType())) {
            cupDbo.setType(cup.getType());
        } else {
            cupDbo.setType(null);
        }
        return cupDbo;
    }

    static public List<Cup> asCups(@NonNull List<CupDbo> cupDbos) {
        return cupDbos.stream()
            .map(it -> Cup.of(it.id, it.name, it.type, it.events.stream().map(x -> EventId.of(x.id.getId())).toList()))
            .toList();
    }

    static public Cup asCup(@NonNull CupDbo cupDbo) {
        return asCups(List.of(cupDbo)).getFirst();
    }
}
