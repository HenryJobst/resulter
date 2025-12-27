package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.analysis.RunnerSplit;

/**
 * DTO for runner split times.
 * Note: Person name is not included to reduce data duplication -
 * frontend should fetch person details separately via getPersonsForResultList endpoint.
 */
public record RunnerSplitDto(
        Long personId,
        String classResultShortName,
        Integer position,
        String splitTime,
        String timeBehind,
        Double splitTimeSeconds,
        boolean reversed
) {

    public static RunnerSplitDto from(RunnerSplit split) {
        return new RunnerSplitDto(
                split.personId().value(),
                split.classResultShortName(),
                split.position(),
                formatTime(split.splitTimeSeconds()),
                formatTimeBehind(split.timeBehindLeader()),
                split.splitTimeSeconds(),
                split.reversed()
        );
    }

    private static String formatTime(Double seconds) {
        if (seconds == null) {
            return "";
        }
        long sec = seconds.longValue();
        long minutes = sec / 60;
        long remainingSeconds = sec % 60;
        return "%d:%02d".formatted(minutes, remainingSeconds);
    }

    private static String formatTimeBehind(Double seconds) {
        if (seconds == null || seconds == 0.0) {
            return "";
        }
        long sec = seconds.longValue();
        long minutes = sec / 60;
        long remainingSeconds = sec % 60;
        return "+%d:%02d".formatted(minutes, remainingSeconds);
    }
}
