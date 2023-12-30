package de.jobst.resulter.adapter.driven.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CountryJpaRepository extends JpaRepository<CountryDbo, Long> {
    Optional<CountryDbo> findByName(String name);
}
