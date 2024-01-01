package de.jobst.resulter.adapter.driven.jpa;

@FunctionalInterface
public interface DboResolver<DomainEntityIdType, Dbo> {
    Dbo findDboById(DomainEntityIdType id);
}
