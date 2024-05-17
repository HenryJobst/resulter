package de.jobst.resulter.domain;

import de.jobst.resulter.domain.scoring.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class ResultList implements Comparable<ResultList> {

    @NonNull
    private final EventId eventId;
    @NonNull
    private final RaceId raceId;
    @Nullable
    private final String creator;
    @Nullable
    private final ZonedDateTime createTime;
    @Nullable
    private final String status;
    @NonNull
    @Setter
    private ResultListId id;
    @Nullable
    @Setter
    private Collection<ClassResult> classResults;

    public ResultList(@NonNull ResultListId id,
                      @NonNull EventId eventId,
                      @NonNull RaceId raceId,
                      @Nullable String creator,
                      @Nullable ZonedDateTime createTime,
                      @Nullable String status,
                      @Nullable Collection<ClassResult> classResults) {
        this.id = id;
        this.eventId = eventId;
        this.raceId = raceId;
        this.creator = creator;
        this.createTime = createTime;
        this.status = status;
        this.classResults = classResults;
    }

    @Override
    public int compareTo(@NonNull ResultList o) {
        int var = Objects.compare(this.createTime, o.createTime, ZonedDateTime::compareTo);
        if (var == 0) {
            var = this.raceId.compareTo(o.raceId);
        }
        if (var == 0) {
            // order by race number
            if (this.getClassResults() != null && o.getClassResults() != null) {
                var = this.getClassResults()
                    .stream()
                    .flatMap(x -> x.personResults().value().stream())
                    .flatMap(x -> x.personRaceResults().value().stream())
                    .findFirst()
                    .orElseThrow()
                    .getRaceNumber()
                    .compareTo(o.getClassResults()
                        .stream()
                        .flatMap(x -> x.personResults().value().stream())
                        .flatMap(x -> x.personRaceResults().value().stream())
                        .findFirst()
                        .orElseThrow()
                        .getRaceNumber());
            }
        }
        return var;
    }

    public void calculate(Cup cup) {

        if (invalid(cup)) {
            return;
        }

        CupTypeCalculationStrategy cupTypeCalculationStrategy = null;
        switch (cup.getType()) {
            case CupType.NOR -> cupTypeCalculationStrategy = new NORCalculationStrategy();
            case CupType.KRISTALL -> cupTypeCalculationStrategy = new KristallCalculationStrategy();
            case CupType.NEBEL -> cupTypeCalculationStrategy = new NebelCalculationStrategy();
            case CupType.ADD -> cupTypeCalculationStrategy = new AddCalculationStrategy();
        }

        if (cupTypeCalculationStrategy != null) {
            calculate(cupTypeCalculationStrategy);
        }
    }

    private boolean invalid(Cup cup) {
        // event is not in given cup
        return cup.getEventIds().stream().filter(it -> it.equals(this.eventId)).findAny().isEmpty();
    }

    private void calculate(CupTypeCalculationStrategy cupTypeCalculationStrategy) {
        /*
        getClassResults().value()
            .stream()
            .filter(cupTypeCalculationStrategy::valid)
            .forEach(it -> it.calculate(cupTypeCalculationStrategy));
         */
    }

    @NonNull
    public Set<OrganisationId> getReferencedOrganisationIds() {
        assert getClassResults() != null;
        return getClassResults().stream()
            .flatMap(x -> x.personResults().value().stream())
            .map(PersonResult::organisationId)
            .collect(Collectors.toSet());
    }
}
