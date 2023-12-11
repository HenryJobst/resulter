package de.jobst.resulter.adapter.driven.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganisationJpaRepository extends JpaRepository<OrganisationDbo, Long> {
    Optional<OrganisationDbo> findByName(String name);
}
