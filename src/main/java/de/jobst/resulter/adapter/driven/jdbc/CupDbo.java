package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.domain.*;
import lombok.*;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.domain.Sort;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.NonNull;

import java.time.Year;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__(@PersistenceCreator))
@Table(name = "cup")
public class CupDbo {

    @Id
    @With
    @Column("id")
    private Long id;

    @Column("name")
    private String name;

    @MappedCollection(idColumn = "cup_id")
    private Set<CupEventDbo> events = new HashSet<>();

    @Column("type")
    private CupType type;

    @Column("year")
    private Integer year;

    public CupDbo(String name) {
        this.id = null;
        this.name = name;
        this.year = Year.now().getValue();
    }

    public static CupDbo from(@NonNull Cup cup, @NonNull DboResolvers dboResolvers) {
        CupDbo cupDbo;
        if (cup.getId().value() != CupId.empty().value()) {
            cupDbo = dboResolvers.getCupDboDboResolver().findDboById(cup.getId());
            cupDbo.setName(cup.getName().value());
        } else {
            cupDbo = new CupDbo(cup.getName().value());
        }

        cupDbo.setEvents(cup.getEvents().stream()
                .map(it -> new CupEventDbo(it.value()))
                .collect(Collectors.toSet()));

        if (ObjectUtils.isNotEmpty(cup.getType())) {
            cupDbo.setType(cup.getType());
        } else {
            cupDbo.setType(null);
        }
        if (ObjectUtils.isNotEmpty(cup.getYear())) {
            cupDbo.setYear(cup.getYear().getValue());
        }
        return cupDbo;
    }

    public static List<Cup> asCups(@NonNull Iterable<CupDbo> cupDbos) {
        return StreamSupport.stream(cupDbos.spliterator(), true)
                .map(it -> Cup.of(
                        it.id,
                        it.name,
                        it.type,
                        Year.of(it.year),
                        it.events.stream()
                                .map(x -> EventId.of(x.id.getId()))
                                .toList()))
                .toList();
    }

    public static Cup asCup(@NonNull CupDbo cupDbo) {
        return asCups(List.of(cupDbo)).getFirst();
    }

    public static String mapOrdersDomainToDbo(Sort.Order order) {
        return switch (order.getProperty()) {
            case "id.value" -> "id";
            case "name.value" -> "name";
            default -> order.getProperty();
        };
    }

    public static String mapOrdersDboToDomain(Sort.Order order) {
        return switch (order.getProperty()) {
            case "id" -> "id.value";
            case "name" -> "name.value";
            default -> order.getProperty();
        };
    }
}
