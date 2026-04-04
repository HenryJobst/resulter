package de.jobst.resulter.adapter.driven.jdbc;

import java.util.Collection;
import java.util.Set;

public interface SplitTimeListJdbcRepositoryCustom {

    Collection<SplitTimeListDbo> findByResultListIdOptimized(Long resultListId);
    Set<Long> existsByResultListIds(Collection<Long> resultListIds);
}
