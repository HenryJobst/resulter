package de.jobst.resulter.domain;

public record TestEventResult(Event event, Country country, Organisation organisation, Person person,
                              ResultList resultList) {}
