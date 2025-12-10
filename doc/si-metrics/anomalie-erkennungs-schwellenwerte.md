# Anomalie-Erkennung - Aktuelle Schwellenwerte

**Stand:** 10.12.2025
**Version:** Ultra-Konservativ (Iteration 5)

## Zusammenfassung der Änderungen

Die Erkennungsschwellen wurden drastisch verschärft, um Fehlalarme zu minimieren. Das System markiert jetzt nur noch physikalisch extrem unplausible Fälle.

## Aktuelle Schwellenwerte

### Zeitschwellen (Dynamisch, Segmentlängen-abhängig)

| Parameter | Wert | Vorher | Änderung |
|-----------|------|--------|----------|
| Zeit-Prozentsatz | **20%** | 15% | +33% |
| Minimum Zeitschwelle | **40s** | 25s | +60% |
| Maximum Zeitschwelle | **150s** | 120s | +25% |
| HIGH-Multiplikator | **1.8x** | 1.5x | +20% |

### Prozentuale Schwellen

#### HIGH_SUSPICION (Hohe Wahrscheinlichkeit)
- Muss **≥70% schneller** sein als Top-3 Referenz (war: 60%)
- Muss **≥70% schneller** sein als eigene Normalleistung (war: 60%)
- Muss **≥72-270s** schneller sein (dynamisch)

#### MODERATE_SUSPICION (Mittlere Wahrscheinlichkeit)
- Muss **≥55% schneller** sein als Top-3 Referenz (war: 40%)
- Muss **≥55% schneller** sein als eigene Normalleistung (war: 40%)
- Muss **≥40-150s** schneller sein (dynamisch)

### Kurze Segmente (< 50s)

Für kurze Segmente werden ALLE Prozentschwellen um 30% strenger:
- HIGH: 0.30 → 0.21 (79% schneller erforderlich)
- MODERATE: 0.45 → 0.315 (≈68% schneller erforderlich)

## Erkennungslogik

**Alle drei Kriterien** müssen gleichzeitig erfüllt sein:

1. ✅ **Viel schneller als Top-Läufer** (PI-Wert)
2. ✅ **Viel schneller als eigene Normalleistung** (AI-Wert)
3. ✅ **Absolute Zeitersparnis groß genug** (dynamischer Schwellenwert)

## Spezielle Features

### Cluster-Erkennung

Wenn mehrere Läufer gemeinsam eine Abkürzung nutzen:
- Erkennt wenn Top 3-4 Zeiten eng zusammenliegen (< 10% Streuung)
- Aber deutlich von Platz 5-7 getrennt sind (> 25% Abstand)
- Nutzt dann Median von Platz 4-7 als Referenz

### Cross-Class Fallback

Bei Klassen mit weniger als 5 Läufern:
- Nutzt automatisch Zeiten aller Klassen für dieses Segment
- Verbessert statistische Zuverlässigkeit

### Finale Segment ausgeschlossen

Das letzte Segment zum Ziel wird komplett ignoriert:
- Ist typischerweise viel schneller
- Würde zu vielen Fehlalarmen führen

## Beispiel-Rechnung

**Segment mit 200s Referenzzeit:**

Moderate Suspicion:
- Zeitschwelle: 200s × 20% = 40s (Minimum greift)
- PI muss < 0.45 sein (≥55% schneller)
- AI muss < 0.45 sein
- Zeitdifferenz muss ≥ 40s sein

High Suspicion:
- Zeitschwelle: 40s × 1.8 = 72s
- PI muss < 0.30 sein (≥70% schneller)
- AI muss < 0.30 sein
- Zeitdifferenz muss ≥ 72s sein

**Konkretes Beispiel:**
Läufer mit Normalleistung PI = 1.0 (durchschnittlich)
- Läuft Segment in 80s statt erwartete 200s
- Zeitdifferenz: 120s ✅
- PI: 80/200 = 0.40 ❌ (nur 60% schneller, reicht nicht)
- → **KEINE Markierung**

Läufer läuft Segment in 55s:
- Zeitdifferenz: 145s ✅
- PI: 55/200 = 0.275 ✅ (73% schneller)
- AI: 0.275/1.0 = 0.275 ✅ (73% schneller als eigene Normalleistung)
- → **HIGH_SUSPICION**

## Interpretation der Ergebnisse

### HIGH_SUSPICION
- **Sehr unwahrscheinlich** ohne Regelverstoß
- Erfordert 70%+ bessere Leistung als Top-Läufer UND eigene Normalleistung
- Sollte genau überprüft werden (Video, Posten-Berichte)

### MODERATE_SUSPICION
- Auffällig, aber **möglicherweise** legitim bei Elite-Leistung
- Erfordert 55%+ bessere Leistung
- Lohnt Untersuchung, besonders wenn mehrere Segmente betroffen

### Mehrere Segmente
Wenn ein Läufer auf **mehreren** Segmenten markiert wird, steigt die Wahrscheinlichkeit erheblich.

## Test-Status

✅ Backend-Tests: 139 erfolgreich
✅ Frontend Type-Check: Erfolgreich
✅ Kompilierung: Erfolgreich

## Bekannte Einschränkungen

1. **Kleine Veranstaltungen:** Bei < 10 Teilnehmern sinkt die statistische Zuverlässigkeit
2. **Bahnfehler:** Versehentlich kürzere Route kann fälschlich markiert werden
3. **Extremleistungen:** Sehr gute Läufer an ihrem Besttag können trotzdem markiert werden

---

**Technische Details siehe:** `anomaly-detection-algorithm.md` (Englisch)
