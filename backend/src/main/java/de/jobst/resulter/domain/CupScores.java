package de.jobst.resulter.domain;

import de.jobst.resulter.domain.util.ValueObjectChecks;
import java.util.Collection;
import java.util.Map;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

@ValueObject
public class CupScores {

    private final Map<CupType, CupScore> cupScores;

    public CupScores(Map<CupType, CupScore> cupScores) {
        this.cupScores = cupScores;
    }

    public static CupScores of(@NonNull Map<CupType, CupScore> cupScores) {
        ValueObjectChecks.requireNotNull(cupScores);
        return new CupScores(cupScores);
    }

    public void add(CupType cupType, CupScore score) {
        this.cupScores.put(cupType, score);
    }

    public CupScore get(CupType cupType) {
        if (this.cupScores.containsKey(cupType)) {
            return this.cupScores.get(cupType);
        }
        return null;
    }

    public Collection<CupScore> values() {
        return this.cupScores.values();
    }
}
