package de.jobst.resulter.application.port;

/**
 * Class grouping option for selection.
 */
public record ClassGroupOption(
        String className,
        int runnerCount
) {}
