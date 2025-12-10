package de.jobst.resulter.application.analysis;

import org.jspecify.annotations.Nullable;

public record SegmentKey(@Nullable String className, String fromControl, String toControl) {}
