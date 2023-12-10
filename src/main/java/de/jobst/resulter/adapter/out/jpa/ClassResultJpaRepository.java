package de.jobst.resulter.adapter.out.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClassResultJpaRepository extends JpaRepository<ClassResultDbo, Long> {
}
