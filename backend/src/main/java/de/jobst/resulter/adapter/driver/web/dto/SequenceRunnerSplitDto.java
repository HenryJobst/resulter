package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.analysis.SequenceRunnerSplit;

import java.util.List;

public record SequenceRunnerSplitDto(
        Long personId,
        String classResultShortName,
        Integer position,
        String splitTime,
        String timeBehind,
        Double splitTimeSeconds,
        List<String> legSplitTimes,
        List<Double> legSplitTimesSeconds
) {

    public static SequenceRunnerSplitDto from(SequenceRunnerSplit split) {
        List<Double> legTimes = split.legSplitTimesSeconds() == null ? List.of() : split.legSplitTimesSeconds();
        return new SequenceRunnerSplitDto(
                split.personId().value(),
                split.classResultShortName(),
                split.position(),
                formatTime(split.splitTimeSeconds()),
                formatTimeBehind(split.timeBehindLeader()),
                split.splitTimeSeconds(),
                legTimes.stream().map(SequenceRunnerSplitDto::formatTime).toList(),
                legTimes
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
