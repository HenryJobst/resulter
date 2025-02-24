package de.jobst.resulter.domain;

import de.jobst.resulter.domain.scoring.CupTypeCalculationStrategy;
import lombok.Getter;
import lombok.Setter;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Identity;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@AggregateRoot
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
    @Identity
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
        int var = 0;

        // order by race number
        if (this.getClassResults() != null && o.getClassResults() != null) {
            var = this.getRaceNumber().compareTo(o.getRaceNumber());
        }

        if (var == 0) {
            var = Objects.compare(this.createTime, o.createTime, ZonedDateTime::compareTo);
        }

        if (var == 0) {
            var = this.raceId.compareTo(o.raceId);
        }
        return var;
    }

    @NonNull
    public RaceNumber getRaceNumber() {
        if (getClassResults() == null) {
            return RaceNumber.empty();
        }
        return getClassResults()
            .stream()
            .flatMap(x -> x.personResults().value().stream())
            .flatMap(x -> x.personRaceResults().value().stream())
            .findFirst()
            .orElseThrow()
            .getRaceNumber();
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

        // Gruppieren nach harmonisiertem ClassResultShortName
        Map<ClassResultShortName, List<ClassResult>> groupedByHarmonizedShortName = getClassResults().stream()
            .collect(Collectors.groupingBy(classResult -> cupTypeCalculationStrategy.harmonizeClassResultShortName(classResult.classResultShortName())));

        // Erstellen einer neuen Collection mit zusammengefassten ClassResults
        Collection<ClassResult> harmonizedClassResults = groupedByHarmonizedShortName.entrySet().stream()
            .map(entry -> {
                ClassResultShortName harmonizedShortName = entry.getKey();
                List<ClassResult> groupedClassResults = entry.getValue();

                // Zusammenfassen der PersonResults
                List<PersonResult> aggregatedPersonResults = groupedClassResults.stream()
                    .flatMap(classResult -> classResult.personResults().value().stream())
                    .collect(Collectors.toList());

                // Erstellen eines neuen ClassResult mit den zusammengefassten PersonResults
                ClassResult firstClassResult = groupedClassResults.getFirst();
                return new ClassResult(firstClassResult.classResultName(), harmonizedShortName, firstClassResult.gender(), PersonResults.of(aggregatedPersonResults), firstClassResult.courseId());
            })
            .toList();

        return harmonizedClassResults.stream()
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
