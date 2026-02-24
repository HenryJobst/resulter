# N+1 Review Backend (Controller/Service/Mapper)

Date: 2026-02-24
Scope: Controller, Service, Mapper, repository query paths for `/cup/{id}/results`, `/event`, `/event/all`, `/organisation`, `/result_list/{id}/cup_score_lists`.

## Runtime verification status
- Runtime query-count benchmark executed via `backend/src/test/java/de/jobst/resulter/adapter/driver/web/NPlusOneReviewIT.java`.
- Environment: Testcontainers PostgreSQL (Docker available).

### Query-count results (post-fix)
- `GET /cup/{id}/results` (1 / 10 / 50 events): `18 / 18 / 18`
- `GET /event?page=0&size=20|100`: `11 / 11`
- `GET /event/all` (small / medium / large seeded growth): `9 / 9 / 9`
- `GET /organisation?page=0&size=20|100`: `8 / 8`
- `GET /result_list/{id}/cup_score_lists` (1 / 10 / 50 lists): `2 / 2 / 2`

Interpretation: No linear growth with `N` in the measured endpoints; confirmed absence of N+1 behavior in these hot paths.

## Static findings (prioritized)

### [High] N+1 in cup detail aggregation by event/result-list
- Location: `CupServiceImpl#getCupDetailed`.
- Pattern:
  - Per-event `eventService.getById(eventId)` and `resultListService.findByEventId(eventId)` in stream.
  - Per-resultList `resultListService.getCupScoreLists(resultListId, cupId)` in stream.
- Risk: query count grows with number of events/result-lists.
- Fix implemented:
  - Batch load events via `eventService.findAllByIdAsMap(...)`.
  - Batch load result lists via `resultListService.findAllByEventIds(...)`.
  - Batch load cup score lists via new API `getCupScoreListsByResultListIds(...)`.

### [High] N+1 in cup score calculation by event
- Location: `CupServiceImpl#calculateScore`.
- Pattern: per-event `resultListService.findByEventId(eventId)` in loop.
- Risk: query count grows linearly with event count.
- Fix implemented: one batch call `resultListService.findAllByEventIds(eventIds)` and map lookup in-memory.

### [Medium] N+1 in mapper path via deprecated single-event mapping
- Location: `EventRacesCupScoreMapper -> EventMapper.toDto(event, hasSplitTimes)`.
- Pattern: deprecated single-entity mapping in list conversion path.
- Risk: repeated organisation/certificate lookup when mapping many event race scores.
- Fix implemented:
  - `EventRacesCupScoreMapper` now maps from precomputed `Map<EventId, EventDto>`.
  - `CupDetailedMapper` precomputes event DTOs in batch (`eventMapper.toDtos(...)`) and passes map to `EventRacesCupScoreMapper.toDtos(...)`.

## Interface/API changes implemented
- `CupScoreListRepository`
  - added: `Map<ResultListId, List<CupScoreList>> findAllByResultListIdsAndCupId(Collection<ResultListId>, CupId)`
- `ResultListService`
  - added: `Map<ResultListId, List<CupScoreList>> getCupScoreListsByResultListIds(Collection<ResultListId>, CupId)`
- `CupScoreListJdbcCustomRepository`
  - added: `findByResultListIdsAndCupIdWithoutCupScores(Collection<Long>, Long)`

## Verification executed
- Compile check: `./mvnw -pl backend -DskipTests test-compile` ✅
- Targeted test: `CupDetailedMapperBatchHasSplitTimesTest` ✅
- Runtime benchmark: `./mvnw -pl backend -Dtest=NPlusOneReviewIT test` ✅

## Remaining step to finish full plan
- Optional: run benchmark against production-like data volume and authenticated API gateway path for external latency profiling (query counts are already stable).

## Additional runtime bug fixed during benchmarking
- `OrganisationRepositoryDataJdbcAdapter#loadOrganisationTree` now returns empty map for empty input set.
- Reason: prevented SQL `IN ()` syntax error in `/cup/{id}/results` path when no referenced organisations exist.
