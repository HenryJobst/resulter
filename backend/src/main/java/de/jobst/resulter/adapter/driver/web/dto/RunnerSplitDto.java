package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.analysis.RunnerSplit;

public record RunnerSplitDto(
        Long personId,
        String personName,
        String classResultShortName,
        Integer position,
        String splitTime,
        String timeBehind,
        Double splitTimeSeconds
) {

    public static RunnerSplitDto from(RunnerSplit split) {
        return new RunnerSplitDto(
                split.personId().value(),
                split.personName(),
                split.classResultShortName(),
                split.position(),
                formatTime(split.splitTimeSeconds()),
                formatTimeBehind(split.timeBehindLeader()),
                split.splitTimeSeconds()
        );
    }

    private static String formatTime(Double seconds) {
        if (seconds == null) {
            return "";
        }
        long sec = seconds.longValue();
        long minutes = sec / 60;
        long remainingSeconds = sec % 60;
        return String.format("%d:%02d", minutes, remainingSeconds);
    }

    private static String formatTimeBehind(Double seconds) {
        if (seconds == null || seconds == 0.0) {
            return "";
        }
        long sec = seconds.longValue();
        long minutes = sec / 60;
        long remainingSeconds = sec % 60;
        return String.format("+%d:%02d", minutes, remainingSeconds);
    }
}
