package de.jobst.resulter.application.port;

import de.jobst.resulter.domain.EventId;
import de.jobst.resulter.domain.OrganisationId;
import de.jobst.resulter.domain.ResultList;
import org.jmolecules.architecture.hexagonal.PrimaryPort;

import java.util.List;

@PrimaryPort
public interface ChampionshipFilterService {

    /**
     * Marks all PersonRaceResults of participants whose club is NOT within the
     * base organisation's tree as NOT_COMPETING (across all races / all ResultLists
     * belonging to the given event).
     */
    void applyChampionshipCleanup(EventId eventId, OrganisationId baseOrgId);

    /**
     * Creates a new Race-0 ResultList for the event where eligible participants
     * are ranked first (state=OK, positions 1..n by runtime) and non-eligible
     * participants follow (state=NOT_COMPETING, positions n+1..m by runtime).
     *
     * If a Race-0 ResultList already exists, a new additional ResultList is created.
     *
     * @return the newly created ResultList(s)
     */
    List<ResultList> addChampionshipRanking(EventId eventId, OrganisationId baseOrgId);
}
