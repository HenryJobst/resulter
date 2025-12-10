# Anomaly Detection Algorithm - Configuration Overview

**Last Updated:** 2025-12-10
**Version:** Ultra-Conservative (Iteration 5)

## Current Threshold Configuration

### Time-Based Thresholds (Dynamic, Segment-Length-Aware)

These thresholds scale based on the segment's reference time to account for natural performance variation on different segment types:

```
TIME_THRESHOLD_PERCENTAGE = 0.20    // 20% of segment reference time
MIN_TIME_THRESHOLD = 40.0 seconds   // Absolute minimum threshold
MAX_TIME_THRESHOLD = 150.0 seconds  // Absolute maximum threshold
```

**Dynamic Calculation:**
- **Moderate Suspicion Time Threshold:** `max(40s, min(150s, referenceTime * 0.20))`
- **High Suspicion Time Threshold:** `moderateThreshold * 1.8` (80% higher)

**Examples:**
- 30s segment → 40s minimum applies
- 100s segment → 20s (20% of 100s) → 40s minimum applies
- 300s segment → 60s (20% of 300s)
- 500s segment → 100s (20% of 500s)
- 1000s segment → 150s maximum applies

### Percentage Thresholds

These define how much faster a runner must be compared to reference times:

#### High Suspicion
```
HIGH_ABSOLUTE_THRESHOLD = 0.30      // Must be ≥70% faster than top-3 (PI < 0.30)
HIGH_INDIVIDUAL_THRESHOLD = 0.30    // Must be ≥70% faster than own baseline (AI < 0.30)
```

#### Moderate Suspicion
```
MODERATE_ABSOLUTE_THRESHOLD = 0.45  // Must be ≥55% faster than top-3 (PI < 0.45)
MODERATE_INDIVIDUAL_THRESHOLD = 0.45 // Must be ≥55% faster than own baseline (AI < 0.45)
```

### Short Segment Adjustment

For segments with reference time < 50 seconds:

```
isShortSegment = referenceTime < 50.0
absoluteMultiplier = 0.70  // All percentage thresholds become 30% stricter
```

**Example:** On a 40s segment, HIGH_SUSPICION requires PI < 0.21 instead of PI < 0.30

### Cross-Class Fallback

When a class has fewer than 5 runners:

```
MIN_RUNNERS_FOR_CLASS_SPECIFIC = 5
```

The algorithm automatically uses times from ALL classes for that segment to get more reliable reference values.

## Triple-Criteria Detection

ALL three criteria must be met simultaneously for classification:

### High Suspicion Classification

1. **PI < 0.30** (70% faster than top-3 reference)
2. **AI < 0.30** (70% faster than own normal performance)
3. **Time difference ≥ highTimeThreshold** (dynamic, typically 72-270s)

### Moderate Suspicion Classification

1. **PI < 0.45** (55% faster than top-3 reference)
2. **AI < 0.45** (55% faster than own normal performance)
3. **Time difference ≥ moderateTimeThreshold** (dynamic, typically 40-150s)

## Advanced Features

### Cluster Detection

Detects groups of runners potentially using shortcuts together:

- Analyzes top 7 times on each segment
- If top 3-4 times cluster together (< 10% spread) BUT are separated from positions 5-7 (> 25% gap)
- Switches to using median of positions 4-7 as reference instead

This prevents contaminated references when multiple runners use the same shortcut.

### Runner's Own Time Exclusion

Uses epsilon-based comparison (0.001s tolerance) to ensure the runner's own time doesn't influence their reference calculation.

### Final Segment Exclusion

The final segment to the finish line is completely excluded from analysis because:
- It's typically much faster than other segments
- It's not included in Normal PI calculation
- Including it caused massive false positives

## Algorithm Evolution History

| Iteration | Date       | Time %  | Min Time | Max Time | HIGH %            | MODERATE %        | Reason                            |
|-----------|------------|---------|----------|----------|-------------------|-------------------|-----------------------------------|
| 1         | Initial    | 15%     | 25s      | 120s     | 0.40 (60% faster) | 0.60 (40% faster) | First implementation              |
| 2         | -          | 15%     | 25s      | 120s     | 0.40              | 0.60              | Added final segment filter        |
| 3         | -          | 15%     | 25s      | 120s     | 0.50              | 0.60              | Made thresholds more conservative |
| 4         | -          | 15%     | 25s      | 120s     | 0.40              | 0.40              | Added dynamic thresholds          |
| 5         | 2025-12-10 | **20%** | **40s**  | **150s** | **0.30 (70%)**    | **0.45 (55%)**    | **Ultra-conservative**            |

## Key Design Principles

1. **False Positives are Worse than False Negatives:** It's better to miss some anomalies than to falsely accuse legitimate performances
2. **Segment-Length Awareness:** Fast segments (roads/paths) naturally have larger performance spreads
3. **Robustness Against Group Shortcuts:** Median-based and cluster detection
4. **Cross-Class Reliability:** Fall back to broader dataset for small classes
5. **Physical Plausibility:** Only flag truly extreme cases (70%+ faster than elite runners)

## Testing Status

✅ Backend tests: 139 passed, 0 failed
✅ Frontend type checking: Passed
✅ Compilation: Successful

## Known Limitations

1. **Small Event Sizes:** With very few participants (< 10 total), statistical reliability decreases
2. **Course Irregularities:** Mistakes in course setting (e.g., accidentally shorter route) may be flagged
3. **Elite Performance:** Extremely fast runners on their best day may still be flagged if they outperform their own baseline by 70%+

## Recommended Interpretation

- **HIGH_SUSPICION:** Review race data carefully - consider video evidence, marshal reports
- **MODERATE_SUSPICION:** Worth investigating but could be legitimate elite performance
- **Multiple Segments:** Multiple suspicious segments for same runner significantly increases confidence
- **Multiple Runners Same Segment:** Cluster detection should handle this, but manual review recommended

## Technical Notes

- **Epsilon Tolerance:** 0.001s (1 millisecond) for floating-point comparisons
- **Reference Calculation:** Median of top 5-7 performers (more robust than average of top 3)
- **NO_DATA Classification:** Filtered out in frontend display
- **Logging:** DEBUG level logs threshold calculations and cluster detection

---

**Configuration File:** `backend/src/main/java/de/jobst/resulter/application/analysis/AnomalyDetectionServiceImpl.java`
