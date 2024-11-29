package de.jobst.resulter.domain;

import java.util.Collection;

public record EventResultLists(Event event, Collection<ResultList> resultLists) {}
