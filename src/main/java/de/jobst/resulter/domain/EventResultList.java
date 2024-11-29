package de.jobst.resulter.domain;

import org.springframework.lang.NonNull;

public record EventResultList(Event event, ResultList resultList) implements Comparable<EventResultList> {

    @Override
    public int compareTo(@NonNull EventResultList o) {
        int val = event.compareTo(o.event());
        if (val == 0) {
            val = resultList.compareTo(o.resultList());
        }
        return val;
    }
}
