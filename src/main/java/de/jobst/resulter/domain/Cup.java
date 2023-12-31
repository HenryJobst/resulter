package de.jobst.resulter.domain;

import de.jobst.resulter.domain.util.ShallowLoadProxy;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Collection;

@Getter
public class Cup {
    @NonNull
    @Setter
    private CupId id;
    @NonNull
    private final CupName name;
    @NonNull
    private final CupType type;
    @NonNull
    private final ShallowLoadProxy<Events> events;

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
}
