package de.jobst.resulter.domain;

import de.jobst.resulter.domain.util.ValueObjectChecks;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.NonNull;

import java.util.Collection;

@Getter
public class Cup implements Comparable<Cup> {

    @NonNull
    @Setter
    private CupId id;
    @NonNull
    private CupName name;
    @NonNull
    private CupType type;

    @NonNull
    @Setter
    private Collection<EventId> eventIds;

    public Cup(@NonNull CupId id, @NonNull CupName name, @NonNull CupType type, @NonNull Collection<EventId> eventIds) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.eventIds = eventIds;
    }

    static public Cup of(long id,
                         @NonNull String cupName,
                         @NonNull CupType type,
                         @NonNull Collection<EventId> eventIds) {
        return new Cup(CupId.of(id), CupName.of(cupName), type, eventIds);
    }

    @Override
    public int compareTo(@NonNull Cup o) {
        return name.compareTo(o.name);
    }

    public void update(CupName name, CupType type, Collection<EventId> eventIds) {
        ValueObjectChecks.requireNotNull(name);
        this.name = name;
        this.type = type;
        this.setEventIds(eventIds);
    }
}
