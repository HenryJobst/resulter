package de.jobst.resulter.domain.aggregations;

import de.jobst.resulter.domain.Event;
import de.jobst.resulter.domain.ResultList;

public record EventResultList(Event event, ResultList resultList) implements Comparable<EventResultList> {

    @Override
    public int compareTo(EventResultList o) {
        int val = event.compareTo(o.event());
        if (val == 0) {
            val = resultList.compareTo(o.resultList());
        }
        return val;
    }
}
