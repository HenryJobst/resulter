package de.jobst.resulter.domain.scoring;

import de.jobst.resulter.domain.*;

import java.util.*;

public class KJCalculationStrategy implements CupTypeCalculationStrategy {

    private final Set<String> validClasses = Set.of(
        "D10", "D12", "D14", "D16", "D18",
        "H10", "H12", "H14", "H16", "H18");

    private final Collection<OrganisationId> validClubs = new HashSet<>();

    public KJCalculationStrategy(Map<OrganisationId, Organisation> organisationById) {
        if (organisationById != null) {
            for (Map.Entry<OrganisationId, Organisation> entry : organisationById.entrySet()) {
                if (entry == null) {
                    continue;
                }

                if (entry.getValue().getShortName().value().equals("KJ")) {
                    Collection<OrganisationId> nationalRegions = entry.getValue().getChildOrganisations();
                    for (OrganisationId nationalRegion : nationalRegions) {
                        validClubs.addAll(organisationById.get(nationalRegion).getChildOrganisations());
                    }

                }

            }

        }

    }

    @Override
    public boolean valid(ClassResult classResult) {
        return validClasses.contains(classResult.classResultShortName().value());
    }

    @Override
    public boolean valid(PersonResult personResult) {
        return validClubs.contains(personResult.organisationId());
    }

    private record SortedResults(List<PersonRaceResult> resultsWithStatusOk, List<CupScore> otherResults) {}

    @Override
    public List<CupScore> calculate(Cup cup,
                                    List<PersonRaceResult> personRaceResults,
                                    Map<PersonId, OrganisationId> organisationByPerson) {
        if (personRaceResults.isEmpty()) {
            return List.of();
        }

        SortedResults sortedResults = sortResults(personRaceResults, organisationByPerson);
        List<PersonRaceResult> resultsWithStatusOk = sortedResults.resultsWithStatusOk;
        List<CupScore> cupScores = new ArrayList<>(sortedResults.otherResults);

        return calculateScore(personRaceResults, organisationByPerson, resultsWithStatusOk, cupScores);
    }

    private SortedResults sortResults(List<PersonRaceResult> personRaceResults,
                                      Map<PersonId, OrganisationId> organisationByPerson) {
        List<CupScore> cupScores = new ArrayList<>();
        List<PersonRaceResult> resultsWithStatusOk = new ArrayList<>();
        Set<ResultStatus> onePointStatus = Set.of(
            ResultStatus.MISSING_PUNCH, ResultStatus.DID_NOT_FINISH, ResultStatus.DISQUALIFIED);

        // filter by Status OK and give 1 Point otherwise
        for (PersonRaceResult raceResult : personRaceResults) {
            if (onePointStatus.contains(raceResult.getState())) {
                PersonId personId = raceResult.getPersonId();
                cupScores.add(CupScore.of(
                    personId,
                    organisationByPerson.get(personId),
                    raceResult.getClassResultShortName(),
                    1));
            } else {
                resultsWithStatusOk.add(raceResult);
            }

        }

        resultsWithStatusOk.sort(Comparator.comparing(PersonRaceResult::getPosition));
        return new SortedResults(resultsWithStatusOk, cupScores);
    }

    private List<CupScore> calculateScore(List<PersonRaceResult> personRaceResults,
                                               Map<PersonId, OrganisationId> organisationByPerson,
                                               List<PersonRaceResult> resultsWithStatusOk,
                                               List<CupScore> cupScores) {
        int defaultPoints = personRaceResults.size();
        int bonusPoints = 3;
        int lastPoints = defaultPoints;
        Position lastPosition = null;

        for (PersonRaceResult personRaceResult : resultsWithStatusOk) {
            int points;
            if (Objects.equals(lastPosition, personRaceResult.getPosition())) {
                points = lastPoints;
            } else {
                points = defaultPoints + bonusPoints;
                lastPoints = points;
                lastPosition = personRaceResult.getPosition();
            }

            PersonId personId = personRaceResult.getPersonId();
            cupScores.add(CupScore.of(
                personId,
                organisationByPerson.get(personId),
                personRaceResult.getClassResultShortName(),
                points));

            defaultPoints--;
            if (bonusPoints > 0) {
                bonusPoints--;
            }

        }

        return cupScores;
    }

    @Override
    public ClassResultShortName harmonizeClassResultShortName(ClassResultShortName classResultShortName) {
        String shortName = classResultShortName.value();
        shortName = shortName.strip();
        shortName = shortName.replace("-", "");
        return ClassResultShortName.of(shortName);
    }
}
