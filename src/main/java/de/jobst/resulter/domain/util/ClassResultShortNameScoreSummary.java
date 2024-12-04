package de.jobst.resulter.domain.util;

import de.jobst.resulter.domain.PersonId;
import lombok.Getter;

import java.text.MessageFormat;

public class ClassResultShortNameScoreSummary {
    @Getter
    private double score; // Die summierte Punktzahl
    @Getter
    private final PersonId id;

    public ClassResultShortNameScoreSummary(double score, PersonId id) {
        this.score = score;
        this.id = id;
    }

    public void sumScore(double additionalScore) {
        this.score += additionalScore;
    }

    @Override
    public String toString() {
        return MessageFormat.format("Score: {0}, ID: {1}", score, id);
    }
}

