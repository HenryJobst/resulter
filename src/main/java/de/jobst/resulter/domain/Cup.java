package de.jobst.resulter.domain;

import de.jobst.resulter.domain.util.ValueObjectChecks;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.NonNull;

import java.time.Year;
import java.util.Collection;

@Setter
@Getter
public class Cup implements Comparable<Cup> {

    @NonNull
    private CupId id;
    @NonNull
    private CupName name;
    @NonNull
    private CupType type;
    @NonNull
    private Year year;
    @NonNull
    private Collection<Event> events;

    public Cup(@NonNull CupId id, @NonNull CupName name, @NonNull CupType type, @NonNull Year year,
               @NonNull Collection<Event> events) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.year = year;
        this.events = events;
    }

    static public Cup of(long id,
                         @NonNull String cupName,
                         @NonNull CupType type,
                         @NonNull Year year,
                         @NonNull Collection<Event> events) {
        return new Cup(CupId.of(id), CupName.of(cupName), type, year, events);
    }

    @Override
    public int compareTo(@NonNull Cup o) {
        return name.compareTo(o.name);
    }

    public void update(CupName name, CupType type, Year year, Collection<Event> events) {
        ValueObjectChecks.requireNotNull(name);
        setName(name);
        setType(type);
        setYear(year);
        setEvents(events);
    }
}
