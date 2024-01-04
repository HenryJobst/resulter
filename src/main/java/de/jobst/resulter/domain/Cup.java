package de.jobst.resulter.domain;

import de.jobst.resulter.domain.util.ShallowLoadProxy;
import de.jobst.resulter.domain.util.ValueObjectChecks;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

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
    private ShallowLoadProxy<Events> events;

    public Cup(@NonNull CupId id, @NonNull CupName name,
               @NonNull CupType type,
               @NonNull ShallowLoadProxy<Events> events
    ) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.events = events;
    }

    static public Cup of(long id,
                         @NonNull String cupName,
                         @NonNull CupType type,
                         @Nullable Collection<Event> events
    ) {
        return new Cup(CupId.of(id),
                CupName.of(cupName),
                type,
                (events != null) ? ShallowLoadProxy.of(Events.of(events)) : ShallowLoadProxy.empty()
        );
    }

    @Override
    public int compareTo(@NonNull Cup o) {
        return name.compareTo(o.name);
    }

    public void update(CupName name, CupType type, Events events) {
        ValueObjectChecks.requireNotNull(name);
        this.name = name;
        this.type = type;
        this.setEvents(ShallowLoadProxy.of(events));
    }
}
