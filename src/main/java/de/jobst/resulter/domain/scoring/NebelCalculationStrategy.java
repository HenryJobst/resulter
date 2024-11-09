package de.jobst.resulter.domain.scoring;

import de.jobst.resulter.domain.*;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NebelCalculationStrategy implements CupTypeCalculationStrategy {

    private final Map<OrganisationId, Organisation> organisationById;
    private final Organisation nebelOrganisation;

    Set<String> classesToSkip = Set.of("BK", "BL", "Beg", "Trim", "Beginner", "OffK", "OffL", "D/H-12 Be");

    public NebelCalculationStrategy(Map<OrganisationId, Organisation> organisationById) {
        this.organisationById = organisationById;
        nebelOrganisation = organisationById.values()
            .stream()
            .filter(x -> x.containsOrganisationWithShortName(CupType.NOR.value()))
            .findFirst()
            .orElse(null);
    }

    @Override
    public boolean valid(ClassResult classResult) {
        return classesToSkip.stream().noneMatch(it -> classResult.classResultShortName().value().equals(it));
    }

    @Override
    public boolean valid(PersonResult personResult) {
        return true; // all organisations are excepted
        /*
        Optional<Organisation> optionalOrganisation = organisationById.containsKey(personResult.organisationId()) ?
                                                      Optional.of(organisationById.get(personResult.organisationId())) :
                                                      Optional.empty();
        return optionalOrganisation.isPresent() && nebelOrganisation != null &&
               nebelOrganisation.containsOrganisationWithId(optionalOrganisation.get().getId());
        */
    }

    @Override
    public List<CupScore> calculate(Cup cup, List<PersonRaceResult> personRaceResults,
                                    Map<PersonId, OrganisationId> organisationByPerson) {
        if (personRaceResults.isEmpty()) {
            return List.of();
        }

        PunchTime fastestTime = personRaceResults.getFirst().getRuntime();
        Set<OrganisationId> organisationWithScore = new HashSet<>();
        var personRaceResultsWithScore = personRaceResults.stream().filter(x -> {
            OrganisationId organisationId = organisationByPerson.get(x.getPersonId());
            if (organisationId == null) {
                // competitors without organisation are always in her own organisation
                return true;
            }
            if (!organisationWithScore.contains(organisationId)) {
                organisationWithScore.add(organisationId);
                return true;
            }
            return false;
        }).toList();

        return personRaceResultsWithScore.stream().map(x -> calculateScore(x, fastestTime)).toList();
    }

    private CupScore calculateScore(PersonRaceResult personRaceResult, PunchTime fastestTime) {
        return CupScore.of(personRaceResult.getPersonId(),
            personRaceResult.getClassResultShortName(),
            NORCalculationStrategy.calculateNorPoints(fastestTime.value(), personRaceResult.getRuntime().value()));
    }
}
