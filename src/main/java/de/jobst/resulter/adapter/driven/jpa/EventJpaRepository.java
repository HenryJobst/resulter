package de.jobst.resulter.adapter.driven.jpa;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventJpaRepository extends JpaRepository<EventDbo, Long> {
    Optional<EventDbo> findByName(String name);

    @NonNull
    @Override
    @EntityGraph(attributePaths = {"classResults.personResults", "organisations"})
    List<EventDbo> findAll();
}
