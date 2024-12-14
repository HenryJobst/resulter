package de.jobst.resulter.domain;

import de.jobst.resulter.domain.scoring.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.time.ZonedDateTime;
import java.util.*;
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

    public CupScoreList calculate(Cup cup, String creator,
                                  ZonedDateTime createTime, CupTypeCalculationStrategy cupTypeCalculationStrategy) {

        if (invalid(cup)) {
            return null;
        }

        List<CupScore> cupScores = calculate(cup, cupTypeCalculationStrategy);
        return new CupScoreList(CupScoreListId.empty(), cup.getId(), id, cupScores, creator, createTime);
    }

    private boolean invalid(Cup cup) {
        // event is not in given cup
        return cup.getEvents().stream().filter(it -> it.getId().equals(this.eventId)).findAny().isEmpty();
    }

    private List<CupScore> calculate(Cup cup, CupTypeCalculationStrategy cupTypeCalculationStrategy) {
        assert getClassResults() != null;
        return getClassResults().stream()
            .filter(cupTypeCalculationStrategy::valid)
            .map(it -> it.calculate(cup, cupTypeCalculationStrategy))
            .flatMap(Collection::stream)
            .toList();
    }

    @NonNull
    public Set<OrganisationId> getReferencedOrganisationIds() {
        assert getClassResults() != null;
        return getClassResults().stream()
            .flatMap(x -> x.personResults().value().stream())
            .map(PersonResult::organisationId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    }
}
