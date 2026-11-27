# Anomalie-Erkennungsalgorithmus - Konfigurationsübersicht

> **Deutsch** · [English](anomaly-detection-algorithm.md)

**Zuletzt aktualisiert:** 2025-12-10
**Version:** Ultra-Konservativ (Iteration 5)

## Aktuelle Schwellenwert-Konfiguration

### Zeitbasierte Schwellenwerte (dynamisch, abhängig von der Segmentlänge)

Diese Schwellenwerte skalieren mit der Referenzzeit des Segments, um die natürliche Leistungsstreuung auf unterschiedlichen Segmenttypen zu berücksichtigen:

```
TIME_THRESHOLD_PERCENTAGE = 0.20    // 20% of segment reference time
MIN_TIME_THRESHOLD = 40.0 seconds   // Absolute minimum threshold
MAX_TIME_THRESHOLD = 150.0 seconds  // Absolute maximum threshold
```

**Dynamische Berechnung:**
- **Zeitschwelle für moderaten Verdacht:** `max(40s, min(150s, referenceTime * 0.20))`
- **Zeitschwelle für hohen Verdacht:** `moderateThreshold * 1.8` (80 % höher)

**Beispiele:**
- Segment mit 30s → es gilt das Minimum von 40s
- Segment mit 100s → 20s (20 % von 100s) → es gilt das Minimum von 40s
- Segment mit 300s → 60s (20 % von 300s)
- Segment mit 500s → 100s (20 % von 500s)
- Segment mit 1000s → es gilt das Maximum von 150s

### Prozentuale Schwellenwerte

Sie legen fest, um wie viel schneller ein Läufer als die Referenzzeiten sein muss:

#### Hoher Verdacht
```
HIGH_ABSOLUTE_THRESHOLD = 0.30      // Must be ≥70% faster than top-3 (PI < 0.30)
HIGH_INDIVIDUAL_THRESHOLD = 0.30    // Must be ≥70% faster than own baseline (AI < 0.30)
```

#### Moderater Verdacht
```
MODERATE_ABSOLUTE_THRESHOLD = 0.45  // Must be ≥55% faster than top-3 (PI < 0.45)
MODERATE_INDIVIDUAL_THRESHOLD = 0.45 // Must be ≥55% faster than own baseline (AI < 0.45)
```

### Anpassung für kurze Segmente

Für Segmente mit einer Referenzzeit < 50 Sekunden:

```
isShortSegment = referenceTime < 50.0
absoluteMultiplier = 0.70  // All percentage thresholds become 30% stricter
```

**Beispiel:** Auf einem Segment mit 40s erfordert HIGH_SUSPICION PI < 0.21 statt PI < 0.30

### Cross-Class-Fallback

Wenn eine Klasse weniger als 5 Läufer hat:

```
MIN_RUNNERS_FOR_CLASS_SPECIFIC = 5
```

Der Algorithmus verwendet automatisch die Zeiten ALLER Klassen für dieses Segment, um zuverlässigere Referenzwerte zu erhalten.

## Erkennung mit drei Kriterien

ALLE drei Kriterien müssen gleichzeitig erfüllt sein, damit klassifiziert wird:

### Klassifizierung als hoher Verdacht

1. **PI < 0.30** (70 % schneller als die Top-3-Referenz)
2. **AI < 0.30** (70 % schneller als die eigene Normalleistung)
3. **Zeitdifferenz ≥ highTimeThreshold** (dynamisch, typischerweise 72-270s)

### Klassifizierung als moderater Verdacht

1. **PI < 0.45** (55 % schneller als die Top-3-Referenz)
2. **AI < 0.45** (55 % schneller als die eigene Normalleistung)
3. **Zeitdifferenz ≥ moderateTimeThreshold** (dynamisch, typischerweise 40-150s)

## Erweiterte Funktionen

### Cluster-Erkennung

Erkennt Gruppen von Läufern, die möglicherweise gemeinsam Abkürzungen nutzen:

- Analysiert die 7 besten Zeiten auf jedem Segment
- Wenn die Top 3-4 Zeiten eng zusammenliegen (< 10 % Streuung), sich aber von den Plätzen 5-7 absetzen (> 25 % Abstand)
- Wechselt stattdessen zum Median der Plätze 4-7 als Referenz

Das verhindert verfälschte Referenzen, wenn mehrere Läufer dieselbe Abkürzung nutzen.

### Ausschluss der eigenen Zeit des Läufers

Verwendet einen Epsilon-Vergleich (Toleranz 0.001s), damit die eigene Zeit des Läufers seine Referenzberechnung nicht beeinflusst.

### Ausschluss des letzten Segments

Das letzte Segment zum Ziel wird vollständig von der Analyse ausgeschlossen, weil:
- es typischerweise deutlich schneller ist als andere Segmente
- es nicht in die Berechnung des Normal-PI einfließt
- seine Einbeziehung massenhaft Fehlalarme verursachte

## Entwicklungsverlauf des Algorithmus

| Iteration | Datum      | Zeit-%  | Min. Zeit | Max. Zeit | HIGH %            | MODERATE %        | Grund                                  |
|-----------|------------|---------|-----------|-----------|-------------------|-------------------|----------------------------------------|
| 1         | Initial    | 15%     | 25s       | 120s      | 0.40 (60% schneller) | 0.60 (40% schneller) | Erste Implementierung               |
| 2         | -          | 15%     | 25s       | 120s      | 0.40              | 0.60              | Filter für das letzte Segment ergänzt  |
| 3         | -          | 15%     | 25s       | 120s      | 0.50              | 0.60              | Schwellenwerte konservativer gewählt   |
| 4         | -          | 15%     | 25s       | 120s      | 0.40              | 0.40              | Dynamische Schwellenwerte ergänzt      |
| 5         | 2025-12-10 | **20%** | **40s**   | **150s**  | **0.30 (70%)**    | **0.45 (55%)**    | **Ultra-konservativ**                  |

## Zentrale Entwurfsprinzipien

1. **Falsch-Positive sind schlimmer als Falsch-Negative:** Es ist besser, einige Anomalien zu übersehen, als legitime Leistungen fälschlich zu beschuldigen
2. **Berücksichtigung der Segmentlänge:** Schnelle Segmente (Straßen/Wege) haben naturgemäß eine größere Leistungsstreuung
3. **Robustheit gegenüber Gruppenabkürzungen:** Median-basierte Referenz und Cluster-Erkennung
4. **Zuverlässigkeit über Klassen hinweg:** Bei kleinen Klassen Rückgriff auf den breiteren Datenbestand
5. **Physikalische Plausibilität:** Nur wirklich extreme Fälle markieren (70 %+ schneller als Eliteläufer)

## Teststatus

✅ Backend-Tests: 139 bestanden, 0 fehlgeschlagen
✅ Frontend-Typprüfung: Bestanden
✅ Kompilierung: Erfolgreich

## Bekannte Einschränkungen

1. **Kleine Veranstaltungen:** Bei sehr wenigen Teilnehmern (< 10 insgesamt) sinkt die statistische Zuverlässigkeit
2. **Unregelmäßigkeiten bei der Bahnlegung:** Fehler beim Bahnlegen (z. B. versehentlich kürzere Strecke) können markiert werden
3. **Elite-Leistung:** Extrem schnelle Läufer an ihrem besten Tag können trotzdem markiert werden, wenn sie ihre eigene Normalleistung um 70 % oder mehr übertreffen

## Empfohlene Interpretation

- **HIGH_SUSPICION:** Renndaten sorgfältig prüfen, etwa Videomaterial und Posten-Berichte heranziehen
- **MODERATE_SUSPICION:** Eine Untersuchung lohnt sich, es kann aber eine legitime Elite-Leistung sein
- **Mehrere Segmente:** Mehrere auffällige Segmente beim selben Läufer erhöhen die Sicherheit deutlich
- **Mehrere Läufer auf demselben Segment:** Die Cluster-Erkennung sollte das abdecken, eine manuelle Prüfung wird trotzdem empfohlen

## Technische Hinweise

- **Epsilon-Toleranz:** 0.001s (1 Millisekunde) für Gleitkomma-Vergleiche
- **Referenzberechnung:** Median der 5-7 besten Läufer (robuster als der Durchschnitt der Top 3)
- **Klassifizierung NO_DATA:** Wird in der Frontend-Anzeige herausgefiltert
- **Logging:** Auf DEBUG-Ebene werden Schwellenwertberechnungen und Cluster-Erkennung protokolliert

---

**Konfigurationsdatei:** `backend/src/main/java/de/jobst/resulter/application/analysis/AnomalyDetectionServiceImpl.java`
