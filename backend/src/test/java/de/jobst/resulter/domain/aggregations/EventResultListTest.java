package de.jobst.resulter.domain.aggregations;

import de.jobst.resulter.domain.*;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EventResultListTest {

    private static Event event(String name) {
        return Event.of(name);
    }

    private static ResultList rl(long raceId) {
        return new ResultList(
                ResultListId.empty(), EventId.of(1L), RaceId.of(raceId),
                null, null, null, null);
    }

    @Test
    void compareTo_ordersByEventFirst() {
        Event e1 = event("A-Lauf");
        Event e2 = event("B-Lauf");
        EventResultList erl1 = new EventResultList(e1, rl(1L));
        EventResultList erl2 = new EventResultList(e2, rl(1L));

        assertThat(erl1.compareTo(erl2)).isLessThan(0);
        assertThat(erl2.compareTo(erl1)).isGreaterThan(0);
    }

    @Test
    void compareTo_sameEvent_ordersByResultList() {
        Event e = event("A-Lauf");
        EventResultList erl1 = new EventResultList(e, rl(1L));
        EventResultList erl2 = new EventResultList(e, rl(2L));

        // RaceId comparison: 1L < 2L
        assertThat(erl1.compareTo(erl2)).isLessThan(0);
    }

    @Test
    void compareTo_equalEntries_returnsZero() {
        Event e = event("A-Lauf");
        EventResultList erl1 = new EventResultList(e, rl(1L));
        EventResultList erl2 = new EventResultList(e, rl(1L));

        assertThat(erl1.compareTo(erl2)).isEqualTo(0);
    }
}
