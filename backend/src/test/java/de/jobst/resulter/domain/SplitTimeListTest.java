package de.jobst.resulter.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SplitTimeListTest {

    private static SplitTimeList.DomainKey key(long personId, String cls, byte raceNum, long resultListId, long eventId) {
        return new SplitTimeList.DomainKey(
                EventId.of(eventId),
                ResultListId.of(resultListId),
                ClassResultShortName.of(cls),
                PersonId.of(personId),
                RaceNumber.of(raceNum)
        );
    }

    @Test
    void getDomainKey_returnsCorrectFields() {
        SplitTimeList stl = new SplitTimeList(
                SplitTimeListId.empty(),
                EventId.of(1L),
                ResultListId.of(2L),
                ClassResultShortName.of("H21"),
                PersonId.of(10L),
                RaceNumber.of((byte) 1),
                List.of()
        );

        SplitTimeList.DomainKey dk = stl.getDomainKey();
        assertThat(dk.personId()).isEqualTo(PersonId.of(10L));
        assertThat(dk.classResultShortName()).isEqualTo(ClassResultShortName.of("H21"));
    }

    @Test
    void domainKey_compareTo_ordersByPersonIdFirst() {
        SplitTimeList.DomainKey a = key(1L, "H21", (byte) 1, 1L, 1L);
        SplitTimeList.DomainKey b = key(2L, "H21", (byte) 1, 1L, 1L);

        assertThat(a.compareTo(b)).isLessThan(0);
        assertThat(b.compareTo(a)).isGreaterThan(0);
    }

    @Test
    void domainKey_compareTo_samePersonId_ordersByClassName() {
        SplitTimeList.DomainKey d21 = key(1L, "D21", (byte) 1, 1L, 1L);
        SplitTimeList.DomainKey h21 = key(1L, "H21", (byte) 1, 1L, 1L);

        assertThat(d21.compareTo(h21)).isLessThan(0); // D < H
    }

    @Test
    void domainKey_compareTo_samePersonAndClass_ordersByRaceNumber() {
        SplitTimeList.DomainKey r1 = key(1L, "H21", (byte) 1, 1L, 1L);
        SplitTimeList.DomainKey r2 = key(1L, "H21", (byte) 2, 1L, 1L);

        assertThat(r1.compareTo(r2)).isLessThan(0);
    }

    @Test
    void domainKey_compareTo_equalKeys_returnsZero() {
        SplitTimeList.DomainKey a = key(1L, "H21", (byte) 1, 1L, 1L);
        SplitTimeList.DomainKey b = key(1L, "H21", (byte) 1, 1L, 1L);

        assertThat(a.compareTo(b)).isEqualTo(0);
    }
}
