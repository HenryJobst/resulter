package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.domain.CupScoreList;

import java.util.Set;

public interface CupScoreListJdbcCustomRepository {

    void deleteAllByDomainKeys(Set<CupScoreList.DomainKey> domainKeys);
}
