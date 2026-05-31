package de.jobst.resulter.adapter.driver.web.mapper;

import de.jobst.resulter.domain.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SimpleMapperTest {

    // -------------------------------------------------------------------------
    // OrganisationTypeMapper
    // -------------------------------------------------------------------------

    @Test
    void organisationTypeMapper_toDto_mapsValue() {
        var dto = OrganisationTypeMapper.toDto(OrganisationType.CLUB);
        assertThat(dto.id()).isEqualTo("Club");
    }

    // -------------------------------------------------------------------------
    // CountryMapper
    // -------------------------------------------------------------------------

    @Test
    void countryMapper_toKeyDto_withId_usesId() {
        Country country = Country.of(5L, "DE", "Deutschland");
        var dto = CountryMapper.toKeyDto(country);
        assertThat(dto.id()).isEqualTo(5L);
        assertThat(dto.name()).isEqualTo("Deutschland");
    }

    @Test
    void countryMapper_toKeyDto_withEmptyId_usesZero() {
        Country country = Country.of(null, "AT", "Österreich");
        var dto = CountryMapper.toKeyDto(country);
        assertThat(dto.id()).isEqualTo(0L);
        assertThat(dto.name()).isEqualTo("Österreich");
    }

    // -------------------------------------------------------------------------
    // CourseMapper
    // -------------------------------------------------------------------------

    @Test
    void courseMapper_toDto_mapsAllFields() {
        Course course = Course.of(EventId.of(1L), "Kurz", 3.2, 120.0, 15);
        var dto = CourseMapper.toDto(course);
        assertThat(dto.name()).isEqualTo("Kurz");
        assertThat(dto.length()).isEqualTo(3.2);
        assertThat(dto.climb()).isEqualTo(120.0);
        assertThat(dto.controls()).isEqualTo(15);
    }

    @Test
    void courseMapper_toDtos_returnsListOfDtos() {
        Course c1 = Course.of(EventId.of(1L), "Lang", 8.0, 300.0, 25);
        Course c2 = Course.of(EventId.of(1L), "Mittel", 5.0, 200.0, 18);
        var dtos = CourseMapper.toDtos(List.of(c1, c2));
        assertThat(dtos).hasSize(2);
        assertThat(dtos.get(0).name()).isEqualTo("Lang");
        assertThat(dtos.get(1).name()).isEqualTo("Mittel");
    }

    // -------------------------------------------------------------------------
    // PersonKeyMapper — leere PersonId → 0
    // -------------------------------------------------------------------------

    @Test
    void personKeyMapper_toDto_withEmptyPersonId_usesZero() {
        Person person = Person.of(null, "Schmidt", "Anna", null, Gender.F);
        var dto = PersonKeyMapper.toDto(person);
        assertThat(dto.id()).isEqualTo(0L);
        assertThat(dto.familyName()).isEqualTo("Schmidt");
        assertThat(dto.givenName()).isEqualTo("Anna");
    }
}
