package de.jobst.resulter.adapter.driven.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CupJpaRepository extends JpaRepository<CupDbo, Long> {
    Optional<CupDbo> findByName(String name);

    @Query("SELECT c FROM CupDbo c JOIN c.events e WHERE e.id = :eventId")
    List<CupDbo> findByEventId(@Param("eventId") Long eventId);
}
