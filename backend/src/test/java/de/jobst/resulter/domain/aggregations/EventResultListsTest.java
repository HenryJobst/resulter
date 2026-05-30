package de.jobst.resulter.domain.aggregations;

import de.jobst.resulter.domain.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EventResultListsTest {

    @Test
    void constructor_storesEventAndResultLists() {
        Event event = Event.of("A-Lauf");
        ResultList rl = new ResultList(
                ResultListId.empty(), EventId.of(1L), RaceId.of(1L),
                null, null, null, null);

        EventResultLists erls = new EventResultLists(event, List.of(rl));

        assertThat(erls.event()).isEqualTo(event);
        assertThat(erls.resultLists()).containsExactly(rl);
    }

    @Test
    void constructor_emptyResultLists() {
        Event event = Event.of("B-Lauf");
        EventResultLists erls = new EventResultLists(event, List.of());
        assertThat(erls.resultLists()).isEmpty();
    }
}
