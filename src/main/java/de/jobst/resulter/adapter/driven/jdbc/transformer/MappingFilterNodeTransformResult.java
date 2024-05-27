package de.jobst.resulter.adapter.driven.jdbc.transformer;

import java.util.Map;

public record MappingFilterNodeTransformResult(String filterString, Map<String, ValueWithMatcher> filterMap) {

    MappingFilterNodeTransformResult(String filterString) {
        this(filterString, Map.of());
    }
}
