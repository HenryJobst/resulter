# 🧠 Metric: Mental Resilience Index

> [Deutsch](mental-resilience-index-metric.de.md) · **English**

## 1. Goal and Concept

The **Mental Resilience Index (MRI)** measures the **mental reaction** of an orienteer after a significant navigation error.

Common analysis tools show *where* time was lost. The MRI shows *how the runner then adjusts speed and risk*.

The metric classifies runners into three types:

1.  **Panic runner (high risk):** React to a mistake by running clearly faster at the next control than their normal physical level allows (index is strongly negative).
2.  **Resigner (uncertainty):** Slow down sharply after a mistake (index is strongly positive).
3.  **Ice-Man (stable):** Keep running at their normal speed at the next control despite the mistake (index is close to zero).

---

## 2. Calculation Basics

The calculation is based on normalizing the split times via the **Performance Index (PI)**.

### A. Performance Index (PI)

The PI normalizes all course lengths and classes by relating the runner's time to the best time on the same leg.

$$PI = \frac{\text{Zeit des Läufers auf Abschnitt } n}{\text{Bestzeit auf Abschnitt } n}$$

### B. Calculating the Mental Resilience Index (MRI)

The MRI is the deviation of the PI at the following control ($n+1$) from the individual normal level ($\text{NormalPI}$), after a mistake ($PI > 1.30$) was detected at the current control ($n$).

$$\text{MRI} = PI_{n+1} - \text{NormalPI}$$

| Value | Classification | Meaning |
| :---: | :---: | :---: |
| $\text{MRI} \approx 0$ | **Ice-Man** | Runner keeps a constant pace. |
| $\text{MRI} < -0.05$ | **Panic** | Runner is clearly faster than normal (risks a new mistake). |
| $\text{MRI} > +0.05$ | **Resignation** | Runner is clearly slower than normal (loses time through uncertainty). |

### C. The Individual Normal Level ($\text{NormalPI}$)

The $\text{NormalPI}$ is the runner's average PI over the whole race, **ignoring all split times that are interpreted as gross errors ($PI > 1.30$).**

---

## 3. Source Code (Java)

The following code contains the necessary Java models (`record`) and the service logic (`MentalScoreService`) for calculating the MRI. The models were extended for cup and yearly analysis.

```java
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Entity for the enriched raw data from the database.
 * Contains context information for cup and yearly aggregation.
 */
public record RunnerSplitData(
    long runnerId,
    long raceId,
    int legNumber,
    double seconds,
    double bestTimeSeconds, 
    int raceYear,
    Long cupId
) {}

/**
 * Entity for the result of the analysis: a mistake-reaction pair.
 */
public record MentalScoreResult(
    long runnerId,
    long raceId,
    int mistakeLeg,
    double mistakeSeverity, // PI of the mistake leg
    double mentalReactionIndex // The deviation of the PI at the following control from the normal PI
) {}


public class MentalScoreService {

    // Threshold for a mistake: 30% slower than the best time (PI > 1.30)
    private static final double MISTAKE_THRESHOLD_PI = 1.30; 
    
    // Tolerance for "Ice-Man" stability: +/- 5% deviation from the normal PI
    private static final double STABILITY_TOLERANCE_PI = 0.05; 

    /**
     * Performs the core analysis of mental resilience.
     * @param allSplits List of all raw split data, sorted by runner and control number.
     * @return A list of all detected mistake-reaction pairs (MRI instances).
     */
    public List<MentalScoreResult> analyzeSplits(List<RunnerSplitData> allSplits) {
        
        // Group the splits by runner and race
        Map<Long, Map<Long, List<RunnerSplitData>>> groupedSplits = allSplits.stream()
            .collect(Collectors.groupingBy(
                RunnerSplitData::runnerId,
                Collectors.groupingBy(RunnerSplitData::raceId)
            ));

        List<MentalScoreResult> results = new ArrayList<>();

        for (Map.Entry<Long, Map<Long, List<RunnerSplitData>>> runnerEntry : groupedSplits.entrySet()) {
            for (Map.Entry<Long, List<RunnerSplitData>> raceEntry : runnerEntry.getValue().entrySet()) {
                
                List<RunnerSplitData> splits = raceEntry.getValue().stream()
                        .sorted((a, b) -> Integer.compare(a.legNumber(), b.legNumber()))
                        .toList();

                if (splits.size() < 2) continue;

                // 1. Calculate normal PI (baseline): average of all non-mistake splits
                double normalPI = splits.stream()
                    .mapToDouble(s -> s.seconds() / s.bestTimeSeconds())
                    .filter(pi -> pi < MISTAKE_THRESHOLD_PI) 
                    .average()
                    .orElse(1.0); 

                // 2. Analyze the control pairs (mistake n and reaction n+1)
                for (int i = 0; i < splits.size() - 1; i++) {
                    RunnerSplitData currentSplit = splits.get(i);
                    RunnerSplitData nextSplit = splits.get(i + 1);
                    
                    double currentPI = currentSplit.seconds() / currentSplit.bestTimeSeconds();
                    double nextPI = nextSplit.seconds() / nextSplit.bestTimeSeconds();
                    
                    // A. Mistake detected? (trigger)
                    if (currentPI >= MISTAKE_THRESHOLD_PI) {
                        
                        // B. Calculate the reaction (Mental Resilience Index)
                        double mentalReactionIndex = nextPI - normalPI;
                        
                        results.add(new MentalScoreResult(
                            currentSplit.runnerId(),
                            currentSplit.raceId(),
                            currentSplit.legNumber(),
                            currentPI,
                            mentalReactionIndex
                        ));
                    }
                }
            }
        }
        return results;
    }


    // --- Aggregation methods for cup and year ---

    /**
     * Calculates the average Mental Resilience Index per runner and cup.
     */
    public Map<Long, Map<Long, Double>> calculateAverageCupScore(List<MentalScoreResult> results, List<RunnerSplitData> allSplits) {
        
        // 1. Create a mapping from race ID to cup ID
        Map<Long, Long> raceToCupMap = allSplits.stream()
            .filter(s -> s.cupId() != null)
            .collect(Collectors.toMap(RunnerSplitData::raceId, RunnerSplitData::cupId, (existing, replacement) -> existing));
        
        // 2. Grouping and calculation
        return results.stream()
            .filter(r -> raceToCupMap.containsKey(r.raceId()))
            .collect(Collectors.groupingBy(
                MentalScoreResult::runnerId,
                Collectors.groupingBy(
                    r -> raceToCupMap.get(r.raceId()), // Grouping by cup ID
                    Collectors.averagingDouble(MentalScoreResult::mentalReactionIndex)
                )
            ));
    }

    /**
     * Calculates the average Mental Resilience Index per runner and year.
     */
    public Map<Long, Map<Integer, Double>> calculateAverageYearlyScore(List<MentalScoreResult> results, List<RunnerSplitData> allSplits) {
        
        // 1. Create a mapping from race ID to year
        Map<Long, Integer> raceToYearMap = allSplits.stream()
            .collect(Collectors.toMap(RunnerSplitData::raceId, RunnerSplitData::raceYear, (existing, replacement) -> existing));
        
        // 2. Grouping and calculation
        return results.stream()
            .collect(Collectors.groupingBy(
                MentalScoreResult::runnerId,
                Collectors.groupingBy(
                    r -> raceToYearMap.get(r.raceId()), // Grouping by year
                    Collectors.averagingDouble(MentalScoreResult::mentalReactionIndex)
                )
            ));
    }

    /**
     * Helper function for classifying the reaction (for the final output).
     */
    public String classifyReaction(double reactionIndex) {
        if (reactionIndex < -STABILITY_TOLERANCE_PI) {
            return "Panik-Läufer (Risiko/Überpace)";
        } else if (reactionIndex > STABILITY_TOLERANCE_PI) {
            return "Resignierer (Unsicherheit/Verlangsamung)";
        } else {
            return "Ice-Man (Stabil)";
        }
    }
}
```
