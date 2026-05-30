package de.jobst.resulter.domain;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class OrganisationTest {

    // -------------------------------------------------------------------------
    // containsOrganisationWithShortName — direkte Übereinstimmung
    // -------------------------------------------------------------------------

    @Test
    void containsOrganisationWithShortName_selfMatch_returnsTrue() {
        Organisation org = Organisation.of("Kaulsdorfer OLV", "KOLV");
        assertThat(org.containsOrganisationWithShortName("KOLV", Map.of())).isTrue();
    }

    @Test
    void containsOrganisationWithShortName_noMatch_returnsFalse() {
        Organisation org = Organisation.of("Kaulsdorfer OLV", "KOLV");
        assertThat(org.containsOrganisationWithShortName("OTHER", Map.of())).isFalse();
    }

    // -------------------------------------------------------------------------
    // containsOrganisationWithShortName — Unterorganisationen (rekursiv)
    // -------------------------------------------------------------------------

    @Test
    void containsOrganisationWithShortName_directChildMatch_returnsTrue() {
        OrganisationId childId = OrganisationId.of(2L);
        Organisation parent = Organisation.of(1L, "Region", "REG", "Other", null, List.of(childId));
        Organisation child = Organisation.of(2L, "Child Club", "CC", "Other", null, List.of());

        assertThat(parent.containsOrganisationWithShortName("CC", Map.of(childId, child))).isTrue();
    }

    @Test
    void containsOrganisationWithShortName_grandChildMatch_returnsTrue() {
        OrganisationId childId = OrganisationId.of(2L);
        OrganisationId grandChildId = OrganisationId.of(3L);

        Organisation parent = Organisation.of(1L, "Top", "TOP", "Other", null, List.of(childId));
        Organisation child = Organisation.of(2L, "Mid", "MID", "Other", null, List.of(grandChildId));
        Organisation grandChild = Organisation.of(3L, "Leaf", "LEAF", "Other", null, List.of());

        Map<OrganisationId, Organisation> map = Map.of(childId, child, grandChildId, grandChild);

        assertThat(parent.containsOrganisationWithShortName("LEAF", map)).isTrue();
    }

    @Test
    void containsOrganisationWithShortName_childMissingFromMap_returnsFalse() {
        OrganisationId childId = OrganisationId.of(2L);
        Organisation parent = Organisation.of(1L, "Parent", "PAR", "Other", null, List.of(childId));

        // childId ist nicht in der Map → false
        assertThat(parent.containsOrganisationWithShortName("CC", Map.of())).isFalse();
    }

    // -------------------------------------------------------------------------
    // containsOrganisationWithId — direkte und rekursive Suche
    // -------------------------------------------------------------------------

    @Test
    void containsOrganisationWithId_selfMatch_returnsTrue() {
        Organisation org = Organisation.of(5L, "Verein", "VER", "Other", null, List.of());
        assertThat(org.containsOrganisationWithId(OrganisationId.of(5L), Map.of())).isTrue();
    }

    @Test
    void containsOrganisationWithId_noMatch_returnsFalse() {
        Organisation org = Organisation.of(5L, "Verein", "VER", "Other", null, List.of());
        assertThat(org.containsOrganisationWithId(OrganisationId.of(99L), Map.of())).isFalse();
    }

    @Test
    void containsOrganisationWithId_childMatch_returnsTrue() {
        OrganisationId childId = OrganisationId.of(10L);
        Organisation parent = Organisation.of(1L, "Parent", "PAR", "Other", null, List.of(childId));
        Organisation child = Organisation.of(10L, "Child", "CHI", "Other", null, List.of());

        assertThat(parent.containsOrganisationWithId(childId, Map.of(childId, child))).isTrue();
    }

    @Test
    void containsOrganisationWithId_childMissingFromMap_returnsFalse() {
        OrganisationId childId = OrganisationId.of(10L);
        Organisation parent = Organisation.of(1L, "Parent", "PAR", "Other", null, List.of(childId));

        assertThat(parent.containsOrganisationWithId(childId, Map.of())).isFalse();
    }

    // -------------------------------------------------------------------------
    // equals und hashCode
    // -------------------------------------------------------------------------

    @Test
    void equals_sameIdNameShortName_returnsTrue() {
        Organisation org1 = Organisation.of(1L, "Verein A", "VA");
        Organisation org2 = Organisation.of(1L, "Verein A", "VA");

        assertThat(org1).isEqualTo(org2);
        assertThat(org1.hashCode()).isEqualTo(org2.hashCode());
    }

    @Test
    void equals_differentId_returnsFalse() {
        Organisation org1 = Organisation.of(1L, "Verein A", "VA");
        Organisation org2 = Organisation.of(2L, "Verein A", "VA");

        assertThat(org1).isNotEqualTo(org2);
    }

    @Test
    void equals_differentName_returnsFalse() {
        Organisation org1 = Organisation.of(1L, "Verein A", "VA");
        Organisation org2 = Organisation.of(1L, "Verein B", "VA");

        assertThat(org1).isNotEqualTo(org2);
    }

    // -------------------------------------------------------------------------
    // equals — Sonderfälle
    // -------------------------------------------------------------------------

    @Test
    void equals_sameReference_returnsTrue() {
        Organisation org = Organisation.of("Verein", "VER");
        assertThat(org.equals(org)).isTrue();
    }

    @Test
    void equals_null_returnsFalse() {
        Organisation org = Organisation.of("Verein", "VER");
        assertThat(org.equals(null)).isFalse();
    }

    @Test
    void equals_differentType_returnsFalse() {
        Organisation org = Organisation.of("Verein", "VER");
        assertThat(org.equals("not an organisation")).isFalse();
    }

    // -------------------------------------------------------------------------
    // of() — weitere Fabrikmethoden
    // -------------------------------------------------------------------------

    @Test
    void of_withNullId_usesEmptyOrganisationId() {
        Organisation org = Organisation.of(null, "Verein", "VER");
        assertThat(org.getId()).isEqualTo(OrganisationId.empty());
    }

    @Test
    void of_withCountry_setsCountry() {
        CountryId country = CountryId.of(1L);
        Organisation org = Organisation.of("Verein", "VER", country);

        assertThat(org.getCountry()).isEqualTo(country);
    }

    @Test
    void of_withValueObjects_setsAllFields() {
        OrganisationName name = OrganisationName.of("Testverein");
        OrganisationShortName shortName = OrganisationShortName.of("TV");
        OrganisationType type = OrganisationType.fromValue("Club");

        Organisation org = Organisation.of(name, shortName, type, null, List.of());

        assertThat(org.getName()).isEqualTo(name);
        assertThat(org.getShortName()).isEqualTo(shortName);
        assertThat(org.getType()).isEqualTo(type);
        assertThat(org.getId()).isEqualTo(OrganisationId.empty());
    }

    // -------------------------------------------------------------------------
    // compareTo — Sortierung nach type, dann name
    // -------------------------------------------------------------------------

    @Test
    void compareTo_sameType_ordersByName() {
        Organisation a = Organisation.of("Aachen OLV", "AOL");
        Organisation b = Organisation.of("Berlin OLV", "BOL");

        assertThat(a.compareTo(b)).isLessThan(0);
        assertThat(b.compareTo(a)).isGreaterThan(0);
        assertThat(a.compareTo(a)).isEqualTo(0);
    }

    @Test
    void compareTo_differentType_ordersByType() {
        // CLUB ("Club") vs OTHER ("Other") — alphabetisch C < O
        Organisation club = Organisation.of(1L, "Verein", "VER", "Club", null, List.of());
        Organisation other = Organisation.of(2L, "Verein", "VER", "Other", null, List.of());

        assertThat(club.compareTo(other)).isLessThan(0);
        assertThat(other.compareTo(club)).isGreaterThan(0);
    }

    @Test
    void compareTo_sameTypeAndName_bothHaveCountry_ordersByCountry() {
        CountryId country1 = CountryId.of(1L);
        CountryId country2 = CountryId.of(2L);

        Organisation org1 = Organisation.of(1L, "Verein", "VER", "Other", country1, List.of());
        Organisation org2 = Organisation.of(2L, "Verein", "VER", "Other", country2, List.of());

        assertThat(org1.compareTo(org2)).isLessThan(0);
        assertThat(org2.compareTo(org1)).isGreaterThan(0);
    }

    @Test
    void compareTo_sameTypeAndName_thisHasCountryOtherNot_returnsPositive() {
        CountryId country = CountryId.of(1L);

        Organisation withCountry    = Organisation.of(1L, "Verein", "VER", "Other", country, List.of());
        Organisation withoutCountry = Organisation.of(2L, "Verein", "VER", "Other", null,    List.of());

        assertThat(withCountry.compareTo(withoutCountry)).isGreaterThan(0);
    }

    @Test
    void compareTo_sameTypeAndName_thisHasNoCountryOtherHas_returnsNegative() {
        CountryId country = CountryId.of(1L);

        Organisation withoutCountry = Organisation.of(1L, "Verein", "VER", "Other", null,    List.of());
        Organisation withCountry    = Organisation.of(2L, "Verein", "VER", "Other", country, List.of());

        assertThat(withoutCountry.compareTo(withCountry)).isLessThan(0);
    }
}
