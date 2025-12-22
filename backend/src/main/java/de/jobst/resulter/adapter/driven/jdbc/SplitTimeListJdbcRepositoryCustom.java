package de.jobst.resulter.adapter.driven.jdbc;

import java.util.Collection;

public interface SplitTimeListJdbcRepositoryCustom {

    Collection<SplitTimeListDbo> findByResultListIdOptimized(Long resultListId);
}

