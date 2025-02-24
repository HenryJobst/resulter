package de.jobst.resulter.domain.aggregations;

import de.jobst.resulter.domain.Event;
import de.jobst.resulter.domain.ResultList;

import java.util.Collection;

public record EventResultLists(Event event, Collection<ResultList> resultLists) {}
