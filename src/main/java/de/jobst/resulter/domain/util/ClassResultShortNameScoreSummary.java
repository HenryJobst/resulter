package de.jobst.resulter.domain.util;

import de.jobst.resulter.domain.PersonId;
import lombok.Getter;

import java.util.Set;

public class ClassResultShortNameScoreSummary {
    @Getter
    private double score; // Die summierte Punktzahl
    @Getter
    private final Set<PersonId> ids; // Gesammelte IDs

    public ClassResultShortNameScoreSummary(double score, Set<PersonId> ids) {
        this.score = score;
        this.ids = ids;
    }

    public void sumScore(double additionalScore) {
        this.score += additionalScore;
    }

    @Override
    public String toString() {
        return "Score: " + score + ", IDs: " + ids;
    }
}

