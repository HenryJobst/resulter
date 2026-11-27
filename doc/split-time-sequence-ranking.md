# Section Sequences in the Split Time Ranking

> [Deutsch](split-time-sequence-ranking.de.md) · **English**

## Goal
The split time ranking was extended with **contiguous section sequences**. Besides the existing single-section analysis (e.g. `31 → 32`), sequences spanning several controls are now analyzed as well (e.g. `S → 31 → 32`).

## Domain semantics
- Only **contiguous** sequences are built (sliding window).
- The maximum sequence length is internally limited to **`n-1` controls** (based on the control sequence available for the runner).
- The minimum sequence length is controlled via `sequenceMinControls`.
- Existing filter rules apply consistently:
  - `filterPersonIds`: only selected runners
  - `filterIntersection=true`: only sequences in which all filtered runners appear
- Without a person filter, sequences with only one runner are hidden.

## API extension
Endpoint: `GET /split_time_analysis/result_list/{id}/ranking`

New optional query parameters:
- `includeSequences` (boolean, default `false`)
- `sequenceMinControls` (int, default `3`)

Backward compatibility:
- Without the new parameters, the behavior is identical to the previous single-section output.

## Guardrails
To limit payload and runtime:
- maximum runners per sequence: `100`
- maximum number of sequences in the response: `500`
- deterministic selection/sorting before the cut

## Manual acceptance scenarios
1. Base case with sequences enabled
- Request: `...?includeSequences=true&sequenceMinControls=3`
- Expectation: `sequenceSegments` is present and contains sequence labels with at least 3 controls.

2. Backward compatibility
- Request without the new parameters
- Expectation: `sequenceSegments` is empty, single-section logic unchanged.

3. Intersection filter
- Request with `filterPersonIds` (>=2) and `filterIntersection=true`
- Expectation: only sequences that contain all filtered runners.

4. Class filter in the UI
- Set a class filter on the split time page
- Expectation: single sections and section sequences are filtered consistently.

5. Large result list
- Test with many runners/controls
- Expectation: the response stays performant, sequences are limited to the guardrail limits if needed.
