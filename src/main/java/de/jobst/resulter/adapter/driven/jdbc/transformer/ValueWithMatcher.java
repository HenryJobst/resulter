package de.jobst.resulter.adapter.driven.jdbc.transformer;

import org.springframework.data.domain.ExampleMatcher;

public record ValueWithMatcher(String value, ExampleMatcher.StringMatcher matcher) {}
