# Anomaly Detection - Current Thresholds

> [Deutsch](anomaly-detection-thresholds.de.md) · **English**

**As of:** 2025-12-10
**Version:** Ultra-Conservative (Iteration 5)

## Summary of Changes

The detection thresholds were tightened drastically to minimize false alarms. The system now flags only cases that are physically extremely implausible.

## Current Thresholds

### Time Thresholds (Dynamic, Depending on Segment Length)

| Parameter | Value | Before | Change |
|-----------|------|--------|----------|
| Time percentage | **20%** | 15% | +33% |
| Minimum time threshold | **40s** | 25s | +60% |
| Maximum time threshold | **150s** | 120s | +25% |
| HIGH multiplier | **1.8x** | 1.5x | +20% |

### Percentage Thresholds

#### HIGH_SUSPICION (High Probability)
- Must be **≥70% faster** than the top-3 reference (was: 60%)
- Must be **≥70% faster** than the runner's own normal performance (was: 60%)
- Must be **≥72-270s** faster (dynamic)

#### MODERATE_SUSPICION (Medium Probability)
- Must be **≥55% faster** than the top-3 reference (was: 40%)
- Must be **≥55% faster** than the runner's own normal performance (was: 40%)
- Must be **≥40-150s** faster (dynamic)

### Short Segments (< 50s)

For short segments, ALL percentage thresholds become 30% stricter:
- HIGH: 0.30 → 0.21 (79% faster required)
- MODERATE: 0.45 → 0.315 (≈68% faster required)

## Detection Logic

**All three criteria** must be met at the same time:

1. ✅ **Much faster than the top runners** (PI value)
2. ✅ **Much faster than the runner's own normal performance** (AI value)
3. ✅ **Absolute time saving large enough** (dynamic threshold)

## Special Features

### Cluster Detection

When several runners use a shortcut together:
- Detects when the top 3-4 times are close together (< 10% spread)
- but clearly separated from places 5-7 (> 25% gap)
- Then uses the median of places 4-7 as the reference

### Cross-Class Fallback

For classes with fewer than 5 runners:
- Automatically uses the times of all classes for this segment
- Improves statistical reliability

### Final Segment Excluded

The last segment to the finish is ignored completely:
- It is typically much faster
- It would lead to too many false alarms

## Example Calculation

**Segment with a 200s reference time:**

Moderate suspicion:
- Time threshold: 200s × 20% = 40s (minimum applies)
- PI must be < 0.45 (≥55% faster)
- AI must be < 0.45
- Time difference must be ≥ 40s

High suspicion:
- Time threshold: 40s × 1.8 = 72s
- PI must be < 0.30 (≥70% faster)
- AI must be < 0.30
- Time difference must be ≥ 72s

**Concrete example:**
Runner with normal performance PI = 1.0 (average)
- Runs the segment in 80s instead of the expected 200s
- Time difference: 120s ✅
- PI: 80/200 = 0.40 ❌ (only 60% faster, not enough)
- → **NO flag**

Runner runs the segment in 55s:
- Time difference: 145s ✅
- PI: 55/200 = 0.275 ✅ (73% faster)
- AI: 0.275/1.0 = 0.275 ✅ (73% faster than the runner's own normal performance)
- → **HIGH_SUSPICION**

## Interpreting the Results

### HIGH_SUSPICION
- **Very unlikely** without a rule violation
- Requires 70%+ better performance than the top runners AND the runner's own normal performance
- Should be checked closely (video, control reports)

### MODERATE_SUSPICION
- Conspicuous, but **possibly** legitimate for an elite performance
- Requires 55%+ better performance
- Worth investigating, especially if several segments are affected

### Multiple Segments
If a runner is flagged on **several** segments, the probability rises considerably.

## Test Status

✅ Backend tests: 139 passed
✅ Frontend type check: Passed
✅ Compilation: Successful

## Known Limitations

1. **Small events:** With < 10 participants, statistical reliability decreases
2. **Course errors:** An accidentally shorter route can be flagged wrongly
3. **Extreme performances:** Very good runners on their best day can still be flagged

---

**For technical details see:** `anomaly-detection-algorithm.md`
