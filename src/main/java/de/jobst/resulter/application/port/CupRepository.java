package de.jobst.resulter.application.port;

import de.jobst.resulter.domain.Cup;
import de.jobst.resulter.domain.CupConfig;
import de.jobst.resulter.domain.CupId;
import de.jobst.resulter.domain.Event;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CupRepository {
    Cup save(Cup cup);

    List<Cup> findAll(CupConfig cupConfig);

    Optional<Cup> findById(CupId cupId, CupConfig cupConfig);

    Cup findOrCreate(Cup cup);

    void deleteCup(Cup cup);

    Collection<Cup> findByEvent(Event event);
}
