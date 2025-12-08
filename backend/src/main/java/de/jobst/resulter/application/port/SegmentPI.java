package de.jobst.resulter.application.port;

import de.jobst.resulter.domain.analysis.PerformanceIndex;

public record SegmentPI(int legNumber, String fromControl, String toControl, double runnerTime, double referenceTime,
                 PerformanceIndex pi) {}
