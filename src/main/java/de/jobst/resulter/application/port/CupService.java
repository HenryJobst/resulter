package de.jobst.resulter.application.port;

import de.jobst.resulter.domain.*;
import de.jobst.resulter.domain.aggregations.CupDetailed;
import org.jmolecules.architecture.hexagonal.PrimaryPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.Collection;
import java.util.List;

@PrimaryPort
public interface CupService {

    List<Cup> findAll();

    Cup findOrCreate(Cup cup);

    Cup getById(CupId cupId);

    Cup updateCup(CupId id, CupName name, CupType type, Year year, Collection<EventId> eventIds);

    Cup createCup(String name, CupType type, Year year, Collection<EventId> eventIds);

    void deleteCup(CupId cupId);

    Page<Cup> findAll(@Nullable String filterString, @NonNull Pageable pageable);

    CupDetailed getCupDetailed(CupId cupId);

    @Transactional
    List<CupScoreList> calculateScore(CupId id);

}
