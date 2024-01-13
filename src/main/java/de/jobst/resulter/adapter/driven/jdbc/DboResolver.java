package de.jobst.resulter.adapter.driven.jdbc;

@FunctionalInterface
public interface DboResolver<DomainEntityIdType, Dbo> {

    Dbo findDboById(DomainEntityIdType id);
}
