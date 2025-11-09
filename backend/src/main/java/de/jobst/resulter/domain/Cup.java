package de.jobst.resulter.domain;

import de.jobst.resulter.domain.scoring.*;
import de.jobst.resulter.domain.util.ValueObjectChecks;
import jakarta.annotation.Nullable;
import java.time.Year;
import java.util.Collection;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Association;
import org.jmolecules.ddd.annotation.Identity;
import org.springframework.lang.NonNull;

@AggregateRoot
@Setter
@Getter
public class Cup implements Comparable<Cup> {

    @Identity
    @NonNull
    private CupId id;

    @NonNull
    private CupName name;

    @NonNull
    private CupType type;

    @NonNull
    private Year year;

    @Association
    @NonNull
    private Collection<EventId> eventIds;

    public Cup(
            @NonNull CupId id,
            @NonNull CupName name,
            @NonNull CupType type,
            @NonNull Year year,
            @NonNull Collection<EventId> eventIds) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.year = year;
        this.eventIds = eventIds;
    }

    public static Cup of(
            long id,
            @NonNull String cupName,
            @NonNull CupType type,
            @NonNull Year year,
            @NonNull Collection<EventId> events) {
        return new Cup(CupId.of(id), CupName.of(cupName), type, year, events);
    }

    @Override
    public int compareTo(@NonNull Cup o) {
        return name.compareTo(o.name);
    }

    public Cup update(CupName name, CupType type, Year year, Collection<EventId> events) {
        ValueObjectChecks.requireNotNull(name);
        setName(name);
        setType(type);
        setYear(year);
        setEventIds(events);
        return this;
    }

    public CupTypeCalculationStrategy getCupTypeCalculationStrategy(
            @Nullable Map<OrganisationId, Organisation> organisationById) {
        return switch (getType()) {
            case CupType.NOR -> new NORCalculationStrategy(organisationById);
            case CupType.KJ -> new KJCalculationStrategy(organisationById);
            case CupType.KRISTALL -> new KristallCalculationStrategy(organisationById);
            case CupType.NEBEL -> new NebelCalculationStrategy(organisationById);
            case CupType.ADD -> new AddCalculationStrategy();
        };
    }
}
