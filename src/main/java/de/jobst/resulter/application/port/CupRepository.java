package de.jobst.resulter.application.port;

import de.jobst.resulter.domain.Cup;
import de.jobst.resulter.domain.CupId;

import java.util.List;
import java.util.Optional;

public interface CupRepository {
    Cup save(Cup cup);

    List<Cup> findAll();

    Optional<Cup> findById(CupId cupId);

    Cup findOrCreate(Cup cup);
}
