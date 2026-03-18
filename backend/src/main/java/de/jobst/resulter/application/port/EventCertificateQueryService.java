package de.jobst.resulter.application.port;

import de.jobst.resulter.domain.EventId;
import java.util.Optional;
import org.jmolecules.architecture.hexagonal.PrimaryPort;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Pageable;

@PrimaryPort
public interface EventCertificateQueryService {

    EventCertificateBatchResult findAll();

    EventCertificateBatchResult findAll(@Nullable String filter, @NonNull Pageable pageable);

    Optional<EventCertificateBatchResult> findById(Long id);

    EventCertificateStatBatchResult getCertificateStats(EventId eventId);
}
