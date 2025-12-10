# 🧠 Metrik: Mental Resilience Index (Mentale Widerstandsfähigkeit)

## 1. Ziel und Konzept

Der **Mental Resilience Index (MRI)** misst die **mentale Reaktion** eines Orientierungsläufers, nachdem er einen signifikanten Navigationsfehler gemacht hat.

Gängige Auswertetools zeigen *wo* Zeit verloren wurde. Der MRI zeigt, *wie der Läufer daraufhin seine Geschwindigkeit und sein Risiko anpasst*.

Die Metrik klassifiziert Läufer in drei Typen:

1.  **Panik-Läufer (Hohes Risiko):** Reagieren auf einen Fehler, indem sie am nächsten Posten deutlich schneller laufen, als es ihr normales physisches Niveau erlaubt (Index ist stark negativ).
2.  **Resignierer (Unsicherheit):** Reduzieren die Geschwindigkeit nach einem Fehler stark (Index ist stark positiv).
3.  **Ice-Man (Stabil):** Laufen trotz des Fehlers am nächsten Posten ihre normale Geschwindigkeit weiter (Index liegt nahe Null).

---

## 2. Berechnungsgrundlagen

Die Berechnung basiert auf der Normalisierung der Split-Zeiten über den **Performance Index (PI)**.

### A. Performance Index (PI)

Der PI normalisiert alle Streckenlängen und Klassen, indem er die Läuferzeit zur Bestzeit auf dem gleichen Abschnitt in Beziehung setzt.

$$PI = \frac{\text{Zeit des Läufers auf Abschnitt } n}{\text{Bestzeit auf Abschnitt } n}$$

### B. Berechnung des Mental Resilience Index (MRI)

Der MRI ist die Abweichung des PI des Folgepostens ($n+1$) vom individuellen Normalniveau ($\text{NormalPI}$), nachdem auf dem aktuellen Posten ($n$) ein Fehler ($PI > 1.30$) festgestellt wurde.

$$\text{MRI} = PI_{n+1} - \text{NormalPI}$$

| Wert | Klassifizierung | Bedeutung |
| :---: | :---: | :---: |
| $\text{MRI} \approx 0$ | **Ice-Man** | Läufer hält konstantes Pacing bei. |
| $\text{MRI} < -0.05$ | **Panik** | Läufer ist deutlich schneller als normal (riskiert neuen Fehler). |
| $\text{MRI} > +0.05$ | **Resignation** | Läufer ist deutlich langsamer als normal (verliert Zeit durch Unsicherheit). |

### C. Das individuelle Normalniveau ($\text{NormalPI}$)

Das $\text{NormalPI}$ ist der Durchschnitts-PI des Läufers im gesamten Rennen, **wobei alle Split-Zeiten, die als grobe Fehler interpretiert werden ($PI > 1.30$), ignoriert werden.**

---

## 3. Quellcode (Java)

Der folgende Code enthält die notwendigen Java-Modelle (`record`) und die Service-Logik (`MentalScoreService`) zur Berechnung des MRI. Die Modelle wurden für die Cup- und Jahres-Analyse erweitert.

```java
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Entität für die angereicherten Rohdaten aus der Datenbank.
 * Enthält Kontext-Informationen für Cup- und Jahres-Aggregation.
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
 * Entität für das Ergebnis der Analyse: ein Fehler-Reaktions-Paar.
 */
public record MentalScoreResult(
    long runnerId,
    long raceId,
    int mistakeLeg,
    double mistakeSeverity, // PI des Fehlerabschnitts
    double mentalReactionIndex // Die Abweichung des PI am Folgeposten vom Normal-PI
) {}


public class MentalScoreService {

    // Schwellenwert für Fehler: 30% langsamer als die Bestzeit (PI > 1.30)
    private static final double MISTAKE_THRESHOLD_PI = 1.30; 
    
    // Toleranz für "Ice-Man"-Stabilität: +/- 5% Abweichung vom normalen PI
    private static final double STABILITY_TOLERANCE_PI = 0.05; 

    /**
     * Führt die Kernanalyse der mentalen Widerstandsfähigkeit durch.
     * @param allSplits Liste aller rohen Split-Daten, sortiert nach Läufer und Postennummer.
     * @return Eine Liste aller festgestellten Fehler-Reaktions-Paare (MRI-Instanzen).
     */
    public List<MentalScoreResult> analyzeSplits(List<RunnerSplitData> allSplits) {
        
        // Gruppierung der Splits nach Läufer und Rennen
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

                // 1. Normal-PI (Baseline) berechnen: Durchschnitt aller Nicht-Fehler-Splits
                double normalPI = splits.stream()
                    .mapToDouble(s -> s.seconds() / s.bestTimeSeconds())
                    .filter(pi -> pi < MISTAKE_THRESHOLD_PI) 
                    .average()
                    .orElse(1.0); 

                // 2. Analyse der Posten-Paare (Fehler n und Reaktion n+1)
                for (int i = 0; i < splits.size() - 1; i++) {
                    RunnerSplitData currentSplit = splits.get(i);
                    RunnerSplitData nextSplit = splits.get(i + 1);
                    
                    double currentPI = currentSplit.seconds() / currentSplit.bestTimeSeconds();
                    double nextPI = nextSplit.seconds() / nextSplit.bestTimeSeconds();
                    
                    // A. Fehler erkannt? (Auslöser)
                    if (currentPI >= MISTAKE_THRESHOLD_PI) {
                        
                        // B. Reaktion (Mental Resilience Index) berechnen
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


    // --- Aggregations-Methoden für Cup und Jahr ---

    /**
     * Berechnet den durchschnittlichen Mental Resilience Index pro Läufer und Cup.
     */
    public Map<Long, Map<Long, Double>> calculateAverageCupScore(List<MentalScoreResult> results, List<RunnerSplitData> allSplits) {
        
        // 1. Mapping von Race-ID zu Cup-ID erstellen
        Map<Long, Long> raceToCupMap = allSplits.stream()
            .filter(s -> s.cupId() != null)
            .collect(Collectors.toMap(RunnerSplitData::raceId, RunnerSplitData::cupId, (existing, replacement) -> existing));
        
        // 2. Gruppierung und Berechnung
        return results.stream()
            .filter(r -> raceToCupMap.containsKey(r.raceId()))
            .collect(Collectors.groupingBy(
                MentalScoreResult::runnerId,
                Collectors.groupingBy(
                    r -> raceToCupMap.get(r.raceId()), // Gruppierung nach Cup-ID
                    Collectors.averagingDouble(MentalScoreResult::mentalReactionIndex)
                )
            ));
    }

    /**
     * Berechnet den durchschnittlichen Mental Resilience Index pro Läufer und Jahr.
     */
    public Map<Long, Map<Integer, Double>> calculateAverageYearlyScore(List<MentalScoreResult> results, List<RunnerSplitData> allSplits) {
        
        // 1. Mapping von Race-ID zu Jahr erstellen
        Map<Long, Integer> raceToYearMap = allSplits.stream()
            .collect(Collectors.toMap(RunnerSplitData::raceId, RunnerSplitData::raceYear, (existing, replacement) -> existing));
        
        // 2. Gruppierung und Berechnung
        return results.stream()
            .collect(Collectors.groupingBy(
                MentalScoreResult::runnerId,
                Collectors.groupingBy(
                    r -> raceToYearMap.get(r.raceId()), // Gruppierung nach Jahr
                    Collectors.averagingDouble(MentalScoreResult::mentalReactionIndex)
                )
            ));
    }

    /**
     * Hilfsfunktion zur Klassifizierung der Reaktion (für die finale Ausgabe).
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
