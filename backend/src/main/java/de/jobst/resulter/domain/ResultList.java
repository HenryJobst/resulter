package de.jobst.resulter.domain;

import de.jobst.resulter.domain.scoring.CupTypeCalculationStrategy;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Association;
import org.jmolecules.ddd.annotation.Identity;
import org.jspecify.annotations.Nullable;

@AggregateRoot
@Getter
public class ResultList implements Comparable<ResultList> {

    public record DomainKey(
            Long eventId,
            Long raceId,
            @Nullable String creator,
            @Nullable ZonedDateTime createTime) {}

    public DomainKey getDomainKey() {
        return new DomainKey(
                eventId.value(),
                raceId.value(),
                creator,
                createTime);
    }

    @Association
    private final EventId eventId;

    @Association
    private final RaceId raceId;

    @Nullable
    private final String creator;

    @Nullable
    private final ZonedDateTime createTime;

    @Nullable
    private final String status;

    @Identity
    @Setter
    private ResultListId id;

    @Nullable
    @Setter
    private Collection<ClassResult> classResults;

    public ResultList(
            ResultListId id,
            EventId eventId,
            RaceId raceId,
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
    public int compareTo(ResultList o) {
        int var = 0;

        // order by race number
        if (this.getClassResults() != null && o.getClassResults() != null) {
            var = this.getRaceNumber().compareTo(o.getRaceNumber());
        }

        if (var == 0) {
            var = Objects.compare(this.createTime, o.createTime,
                (zonedDateTime, other) -> zonedDateTime != null ? zonedDateTime.compareTo(other) : 0);
        }

        if (var == 0) {
            var = this.raceId.compareTo(o.raceId);
        }
        return var;
    }

    public RaceNumber getRaceNumber() {
        if (getClassResults() == null) {
            return RaceNumber.empty();
        }
        return getClassResults().stream()
                .flatMap(x -> x.personResults().value().stream())
                .flatMap(x -> x.personRaceResults().value().stream())
                .findFirst()
                .orElseThrow()
                .getRaceNumber();
    }

    public @Nullable CupScoreList calculate(
            Cup cup, String creator, ZonedDateTime createTime, CupTypeCalculationStrategy cupTypeCalculationStrategy) {

        if (invalid(cup)) {
            return null;
        }

        List<CupScore> cupScores = calculate(cup, cupTypeCalculationStrategy);
        return new CupScoreList(CupScoreListId.empty(), cup.getId(), id, cupScores, creator, createTime);
    }

    private boolean invalid(Cup cup) {
        // event is not in given cup
        return cup.getEventIds().stream()
                .filter(it -> it.equals(this.eventId))
                .findAny()
                .isEmpty();
    }

    private List<CupScore> calculate(Cup cup, CupTypeCalculationStrategy cupTypeCalculationStrategy) {
        assert getClassResults() != null;

        // Gruppieren nach harmonisiertem ClassResultShortName
        Map<ClassResultShortName, List<ClassResult>> groupedByHarmonizedShortName = getClassResults().stream()
                .collect(Collectors.groupingBy(classResult ->
                        cupTypeCalculationStrategy.harmonizeClassResultShortName(classResult.classResultShortName())));

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
                    return new ClassResult(
                            firstClassResult.classResultName(),
                            harmonizedShortName,
                            firstClassResult.gender(),
                            PersonResults.of(aggregatedPersonResults),
                            firstClassResult.courseId());
                })
                .toList();

        return harmonizedClassResults.stream()
                .filter(cupTypeCalculationStrategy::valid)
                .map(it -> it.calculate(cup, cupTypeCalculationStrategy))
                .flatMap(Collection::stream)
                .toList();
    }

    public Set<OrganisationId> getReferencedOrganisationIds() {
        assert getClassResults() != null;
        return getClassResults().stream()
                .flatMap(x -> x.personResults().value().stream())
                .map(PersonResult::organisationId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }
}
