package de.jobst.resulter.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.jobst.resulter.application.port.CountryRepository;
import de.jobst.resulter.application.port.OrganisationRepository;
import de.jobst.resulter.domain.CountryId;
import de.jobst.resulter.domain.Organisation;
import de.jobst.resulter.domain.OrganisationName;
import de.jobst.resulter.domain.OrganisationShortName;
import de.jobst.resulter.domain.OrganisationType;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OrganisationServiceImplTest {

    private OrganisationRepository organisationRepository;
    private CountryRepository countryRepository;
    private OrganisationServiceImpl service;

    @BeforeEach
    void setUp() {
        organisationRepository = mock(OrganisationRepository.class);
        countryRepository = mock(CountryRepository.class);
        service = new OrganisationServiceImpl(organisationRepository, countryRepository);
        when(organisationRepository.findByIds(any())).thenReturn(List.of());
        when(organisationRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    }

    private Organisation create(String name, String shortName, CountryId countryId) {
        return service.createOrganisation(
                OrganisationName.of(name),
                OrganisationShortName.of(shortName),
                OrganisationType.NATIONAL_REGION,
                countryId,
                List.of());
    }

    @Test
    void createOrganisation_withoutCountry_isAllowed() {
        assertThat(create("Berlin", "BLN", null)).isNotNull();
    }

    @Test
    void createOrganisation_withUnknownCountry_returnsNull() {
        when(countryRepository.findById(any())).thenReturn(Optional.empty());
        assertThat(create("Berlin", "BLN", CountryId.of(99L))).isNull();
    }

    @Test
    void createOrganisation_rejectsEmptyNames() {
        assertThatThrownBy(() -> create("", "BLN", null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> create("Berlin", " ", null)).isInstanceOf(IllegalArgumentException.class);
    }
}
