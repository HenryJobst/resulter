package de.jobst.resulter.adapter.out.jpa;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EventJpaRepository extends CrudRepository<EventEntity, Long> {
    Optional<EventEntity> findByName(String name);
}
