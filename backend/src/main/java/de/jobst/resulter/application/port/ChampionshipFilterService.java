package de.jobst.resulter.application.port;

import de.jobst.resulter.domain.EventId;
import de.jobst.resulter.domain.OrganisationId;
import de.jobst.resulter.domain.ResultList;
import org.jmolecules.architecture.hexagonal.PrimaryPort;

import java.util.List;
import java.util.Set;

@PrimaryPort
public interface ChampionshipFilterService {

    /**
     * Returns the distinct class result short names for all result lists of the given event.
     */
    Set<String> findClassShortNames(EventId eventId);

    /**
     * Marks all PersonRaceResults of participants whose club is NOT within the
     * base organisation's tree as NOT_COMPETING (across all races / all ResultLists
     * belonging to the given event).
     *
     * @param excludeClassShortNames classes to skip entirely; empty = process all classes
     */
    void applyChampionshipCleanup(EventId eventId, OrganisationId baseOrgId, Set<String> excludeClassShortNames);

    /**
     * Creates a new Race-0 ResultList for the event where eligible participants
     * are ranked first (state=OK, positions 1..n by runtime) and non-eligible
     * participants follow (state=NOT_COMPETING, positions n+1..m by runtime).
     *
     * @param excludeClassShortNames classes to skip entirely; empty = include all classes
     * @return the newly created ResultList(s)
     */
    List<ResultList> addChampionshipRanking(EventId eventId, OrganisationId baseOrgId, Set<String> excludeClassShortNames);
}
