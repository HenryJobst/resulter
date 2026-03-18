package de.jobst.resulter.application.port;

import de.jobst.resulter.adapter.driver.web.dto.OrganisationDto;
import java.util.List;
import java.util.Optional;
import org.jmolecules.architecture.hexagonal.PrimaryPort;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@PrimaryPort
public interface OrganisationQueryService {

    List<OrganisationDto> findAllAsDto();

    Page<OrganisationDto> findAllAsDto(@Nullable String filter, @NonNull Pageable pageable);

    Optional<OrganisationDto> findByIdAsDto(Long id);
}
