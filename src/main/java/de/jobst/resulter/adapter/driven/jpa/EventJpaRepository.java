package de.jobst.resulter.adapter.driven.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EventJpaRepository extends JpaRepository<EventDbo, Long> {
    Optional<EventDbo> findByName(String name);
}
