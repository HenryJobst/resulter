package de.jobst.resulter.adapter.driven.jdbc;

import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class JdbcUtilTest {

    // -------------------------------------------------------------------------
    // DboResolvers
    // -------------------------------------------------------------------------

    @Test
    void dboResolvers_empty_returnsEmptyInstance() {
        DboResolvers resolvers = DboResolvers.empty();
        assertThat(resolvers).isNotNull();
        assertThat(resolvers.getCupDboDboResolver()).isNull();
        assertThat(resolvers.getEventDboResolver()).isNull();
    }

    // -------------------------------------------------------------------------
    // DBO-Klassen (package-private Konstruktoren)
    // -------------------------------------------------------------------------

    @Test
    void cupEventDbo_idIsSet() {
        CupEventDbo dbo = new CupEventDbo(42L);
        assertThat(dbo.getId()).isNotNull();
        assertThat(dbo.getId().getId()).isEqualTo(42L);
    }

    @Test
    void eventOrganisationDbo_idIsSet() {
        EventOrganisationDbo dbo = new EventOrganisationDbo(7L);
        assertThat(dbo.getId()).isNotNull();
        assertThat(dbo.getId().getId()).isEqualTo(7L);
    }

    @Test
    void organisationOrganisationDbo_idIsSet() {
        OrganisationOrganisationDbo dbo = new OrganisationOrganisationDbo(15L);
        assertThat(dbo.getId()).isNotNull();
        assertThat(dbo.getId().getId()).isEqualTo(15L);
    }

    // -------------------------------------------------------------------------
    // Timestamp-Converter
    // -------------------------------------------------------------------------

    @Test
    void localDateToTimestampConverter_convertsCorrectly() {
        LocalDateToTimestampConverter converter = new LocalDateToTimestampConverter();
        LocalDate date = LocalDate.of(2025, 5, 31);
        Timestamp result = converter.convert(date);
        assertThat(result).isNotNull();
        assertThat(result.toLocalDateTime().toLocalDate()).isEqualTo(date);
    }

    @Test
    void timestampToLocalDateConverter_convertsCorrectly() {
        TimestampToLocalDateConverter converter = new TimestampToLocalDateConverter();
        LocalDate original = LocalDate.of(2025, 3, 15);
        Timestamp ts = Timestamp.valueOf(original.atStartOfDay());
        assertThat(converter.convert(ts)).isEqualTo(original);
    }

    @Test
    void offsetDateTimeToTimestampConverter_convertsCorrectly() {
        OffsetDateTimeToTimestampConverter converter = new OffsetDateTimeToTimestampConverter();
        OffsetDateTime odt = OffsetDateTime.of(2025, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
        Timestamp result = converter.convert(odt);
        assertThat(result).isNotNull();
        assertThat(result.toInstant()).isEqualTo(odt.toInstant());
    }

    @Test
    void timestampToOffsetDateTimeConverter_convertsCorrectly() {
        TimestampToOffsetDateTimeConverter converter = new TimestampToOffsetDateTimeConverter();
        Timestamp ts = Timestamp.valueOf("2025-06-01 10:00:00");
        OffsetDateTime result = converter.convert(ts);
        assertThat(result).isNotNull();
        assertThat(result.getOffset()).isEqualTo(ZoneOffset.UTC);
    }

    @Test
    void timestampToTimestampConverter_returnsIdentity() {
        TimestampToTimestampConverter converter = new TimestampToTimestampConverter();
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        assertThat(converter.convert(ts)).isSameAs(ts);
    }
}
