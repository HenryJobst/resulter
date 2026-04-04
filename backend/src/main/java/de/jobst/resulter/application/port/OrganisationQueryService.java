package de.jobst.resulter.application.port;

import java.util.Optional;
import org.jmolecules.architecture.hexagonal.PrimaryPort;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Pageable;

@PrimaryPort
public interface OrganisationQueryService {

    OrganisationBatchResult findAll();

    OrganisationBatchResult findAll(@Nullable String filter, @NonNull Pageable pageable);

    Optional<OrganisationBatchResult> findById(Long id);
}
