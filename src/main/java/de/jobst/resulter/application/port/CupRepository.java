package de.jobst.resulter.application.port;

import de.jobst.resulter.domain.Cup;
import de.jobst.resulter.domain.CupId;
import de.jobst.resulter.domain.EventId;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.jmolecules.ddd.annotation.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@Repository
public interface CupRepository {

    Cup save(Cup cup);

    List<Cup> findAll();

    Optional<Cup> findById(CupId cupId);

    Cup findOrCreate(Cup cup);

    void deleteCup(Cup cup);

    Collection<Cup> findByEvent(EventId eventId);

    Page<Cup> findAll(@Nullable String filterString, @NonNull Pageable pageable);
}
