package de.jobst.resulter.adapter.driven.jdbc;

import java.util.Map;

public record FilterNodeTransformResult(String filterString, Map<String, String> filterMap) {

    FilterNodeTransformResult(String filterString) {
        this(filterString, Map.of());
    }
}
