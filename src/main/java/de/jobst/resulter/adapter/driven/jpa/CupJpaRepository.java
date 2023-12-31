package de.jobst.resulter.adapter.driven.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CupJpaRepository extends JpaRepository<CupDbo, Long> {
    Optional<CupDbo> findByName(String name);
}
