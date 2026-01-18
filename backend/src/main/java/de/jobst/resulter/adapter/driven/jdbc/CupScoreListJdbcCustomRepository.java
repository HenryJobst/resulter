package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.domain.CupScoreList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Custom repository fragment for optimized CupScoreList queries.
 * Provides methods that avoid N+1 queries by not loading MappedCollections.
 */
public interface CupScoreListJdbcCustomRepository {

    void deleteAllByDomainKeys(Set<CupScoreList.DomainKey> domainKeys);

    void deleteAllByEventId(Long eventId);

    /**
     * Find cup score lists by result list ID without loading the MappedCollection (cupScores).
     * Uses JdbcClient with custom RowMapper to avoid Spring Data JDBC auto-loading.
     * Use this for batch loading to avoid N+1 queries.
     *
     * @param resultListId Result list ID to filter by
     * @return List of CupScoreListDbo objects without loaded cupScores
     */
    List<CupScoreListDbo> findByResultListIdWithoutCupScores(Long resultListId);

    /**
     * Find cup score lists by result list ID and cup ID without loading the MappedCollection (cupScores).
     * Uses JdbcClient with custom RowMapper to avoid Spring Data JDBC auto-loading.
     * Use this for batch loading to avoid N+1 queries.
     *
     * @param resultListId Result list ID to filter by
     * @param cupId Cup ID to filter by
     * @return List of CupScoreListDbo objects without loaded cupScores
     */
    List<CupScoreListDbo> findByResultListIdAndCupIdWithoutCupScores(Long resultListId, Long cupId);

    /**
     * Batch load cup scores for multiple cup score lists.
     * Returns a map of cup_score_list_id to list of CupScoreDbo.
     *
     * @param cupScoreListIds Collection of cup score list IDs to load scores for
     * @return List of CupScoreDbo objects with their parent list ID
     */
    List<CupScoreWithListId> findCupScoresByListIds(Collection<Long> cupScoreListIds);

    /**
     * Record to hold a CupScoreDbo with its parent cup_score_list_id.
     */
    record CupScoreWithListId(Long cupScoreListId, CupScoreDbo cupScore) {}
}
