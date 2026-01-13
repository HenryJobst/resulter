# PRD: Backend Query Performance Optimization

## Introduction

Die Backend-Endpunkte des Resulter-Systems leiden unter N+1 Query-Problemen. Bei der Konvertierung von DBOs zu Domain-Entities werden "Resolver"-Funktionen verwendet, die für jede Referenz (Organisation, Country, etc.) einzelne `findById`-Queries ausführen. Dies führt zu exponentiell wachsenden Datenbankabfragen bei steigender Datenmenge.

Das Ziel ist eine systematische Reduktion der DB-Queries um mindestens 50% durch Batch-Fetching, explizite JOINs und DTO-basierte Abfragen.

## Goals

- Reduktion der Datenbankabfragen um mindestens 50%
- Eliminierung aller N+1 Query-Patterns in Repository-Adaptern
- Ersetzung von Single-Select-Resolvers durch Batch-Loading-Strategien
- Beibehaltung der hexagonalen Architektur und SOLID-Prinzipien
- Keine funktionalen Regressionen in bestehenden Endpunkten

## User Stories

### US-001: Batch-Loading für Country-Resolver implementieren
**Description:** Als Entwickler möchte ich, dass Countries in einem Batch geladen werden, damit nicht für jede Organisation ein einzelner DB-Call erfolgt.

**Acceptance Criteria:**
- [ ] Neue Methode `findAllById(Set<Long> ids)` in `CountryRepository`
- [ ] `CountryRepositoryDataJdbcAdapter` implementiert Batch-Query mit `IN`-Clause
- [ ] Resolver-Pattern wird durch Map-basiertes Lookup ersetzt
- [ ] Typecheck/Compile passes: `./mvnw compile -pl backend`
- [ ] Tests grün: `./mvnw test -pl backend`

### US-002: Batch-Loading für Organisation-Resolver implementieren
**Description:** Als Entwickler möchte ich, dass Organisations in einem Batch geladen werden, damit die rekursive Resolver-Struktur nicht N+1 Queries verursacht.

**Acceptance Criteria:**
- [ ] Erweiterung von `OrganisationRepositoryDataJdbcAdapter.getOrganisationDbos()` für Parent-Organisationen
- [ ] Batch-Loading von Countries für alle Organisationen gleichzeitig
- [ ] Map-basiertes Lookup statt rekursiver Resolver
- [ ] Typecheck/Compile passes: `./mvnw compile -pl backend`
- [ ] Tests grün: `./mvnw test -pl backend`

### US-003: EventRepositoryDataJdbcAdapter optimieren
**Description:** Als Entwickler möchte ich, dass `EventRepositoryDataJdbcAdapter.findAll()` und `findAll(filter, pageable)` keine N+1 Queries mehr produzieren.

**Acceptance Criteria:**
- [ ] `getOrganisationResolver()` wird durch Batch-Loading ersetzt
- [ ] `getCountryResolver()` wird durch Batch-Loading ersetzt
- [ ] `getPrimaryEventCertificateResolver()` wird durch Batch-Query ersetzt
- [ ] Query-Count für 100 Events: maximal 5 Queries (statt 100+)
- [ ] Typecheck/Compile passes: `./mvnw compile -pl backend`
- [ ] Tests grün: `./mvnw test -pl backend`

### US-004: OrganisationRepositoryDataJdbcAdapter optimieren
**Description:** Als Entwickler möchte ich, dass `findAll()`, `findAll(filter, pageable)` und `findByIds()` keine N+1 Queries für Countries produzieren.

**Acceptance Criteria:**
- [ ] Countries werden in einem Batch für alle Organisationen geladen
- [ ] `asOrganisation()` erhält vorgeladene Map statt Resolver-Function
- [ ] Query-Count für 100 Organisationen: maximal 3 Queries
- [ ] Typecheck/Compile passes: `./mvnw compile -pl backend`
- [ ] Tests grün: `./mvnw test -pl backend`

### US-005: PersonRepositoryDataJdbcAdapter.findOrCreate() optimieren
**Description:** Als Entwickler möchte ich, dass `findOrCreate(Collection<Person>)` Batch-Operationen nutzt.

**Acceptance Criteria:**
- [ ] Batch-Query für Existenzprüfung (statt N Einzelabfragen)
- [ ] Batch-Insert für neue Personen
- [ ] Query-Count für 100 Personen: maximal 2 Queries
- [ ] Typecheck/Compile passes: `./mvnw compile -pl backend`
- [ ] Tests grün: `./mvnw test -pl backend`

### US-006: ResultListRepositoryDataJdbcAdapter optimieren
**Description:** Als Entwickler möchte ich, dass ResultList-Operationen keine N+1 Queries produzieren.

**Acceptance Criteria:**
- [ ] `findOrCreate(Collection)` nutzt Batch-Loading
- [ ] Zugehörige Entities (Persons, Courses) werden mit JOINs oder Batch-Queries geladen
- [ ] Typecheck/Compile passes: `./mvnw compile -pl backend`
- [ ] Tests grün: `./mvnw test -pl backend`

### US-007: RaceRepositoryDataJdbcAdapter optimieren
**Description:** Als Entwickler möchte ich, dass Race-Operationen keine N+1 Queries produzieren.

**Acceptance Criteria:**
- [ ] `findOrCreate(Collection)` nutzt Batch-Loading
- [ ] Typecheck/Compile passes: `./mvnw compile -pl backend`
- [ ] Tests grün: `./mvnw test -pl backend`

### US-008: SplitTimeListRepositoryDataJdbcAdapter optimieren
**Description:** Als Entwickler möchte ich, dass SplitTimeList-Operationen keine N+1 Queries produzieren.

**Acceptance Criteria:**
- [ ] `findOrCreate(Collection)` nutzt Batch-Loading
- [ ] Typecheck/Compile passes: `./mvnw compile -pl backend`
- [ ] Tests grün: `./mvnw test -pl backend`

### US-009: CupRepositoryDataJdbcAdapter optimieren
**Description:** Als Entwickler möchte ich, dass Cup-Operationen effizient geladen werden.

**Acceptance Criteria:**
- [ ] Events und Organisationen werden in Batch-Queries geladen
- [ ] Keine N+1 Queries für Cup-Listen
- [ ] Typecheck/Compile passes: `./mvnw compile -pl backend`
- [ ] Tests grün: `./mvnw test -pl backend`

### US-010: Performance-Monitoring einrichten
**Description:** Als Entwickler möchte ich die Query-Anzahl vor und nach der Optimierung messen können.

**Acceptance Criteria:**
- [ ] SQL-Logging aktiviert im dev-Profil
- [ ] Baseline-Messung dokumentiert (Query-Count pro Endpunkt)
- [ ] Finale Messung dokumentiert (Query-Count pro Endpunkt)
- [ ] Mindestens 50% Reduktion nachgewiesen
- [ ] Prometheus-Metriken für DB-Calls verfügbar

## Functional Requirements

- FR-1: Alle Repository-Adapter müssen `findAllById(Collection<ID> ids)` Methoden unterstützen
- FR-2: Batch-Queries müssen `IN`-Clauses mit parametrisiertem SQL verwenden
- FR-3: Resolver-Functions müssen durch Map-basiertes Lookup ersetzt werden
- FR-4: DBO-zu-Domain-Konvertierung muss vorgeladene Referenzdaten akzeptieren
- FR-5: Collection-basierte `findOrCreate()`-Methoden müssen Batch-Operationen nutzen
- FR-6: Native SQL-Queries mit expliziten JOINs für komplexe Aggregat-Ladungen
- FR-7: DTOs für Multi-Entity-Abfragen, wenn Domain-Mapping zu komplex wird

## Non-Goals

- Keine Änderung der Domain-Entities oder Value Objects
- Keine Einführung von Caching (Redis, Caffeine, etc.)
- Keine Änderung der REST-API-Contracts
- Keine Änderung der Liquibase-Migrationen oder Datenbankschema
- Keine Einführung von Lazy-Loading (widerspricht Spring Data JDBC Philosophie)
- Keine Optimierung von Write-Operationen (nur Read-Operationen)

## Technical Considerations

### Implementierungsmuster

```java
// VORHER: N+1 Pattern
private Function<Long, Organisation> getOrganisationResolver() {
    return id -> organisationJdbcRepository.findById(id).orElseThrow()
        .asOrganisation(getOrganisationResolver(), getCountryResolver());
}

// NACHHER: Batch-Loading Pattern
public List<Event> findAll() {
    List<EventDbo> events = eventJdbcRepository.findAll();
    
    // Collect all organisation IDs
    Set<Long> orgIds = events.stream()
        .map(e -> e.getOrganisationId().getId())
        .collect(Collectors.toSet());
    
    // Batch-load all organisations
    Map<Long, Organisation> orgMap = organisationRepository.findAllById(orgIds);
    
    // Map with pre-loaded data
    return events.stream()
        .map(e -> e.asEvent(orgMap::get))
        .toList();
}
```

### Betroffene Dateien

1. `CountryRepositoryDataJdbcAdapter.java` - Batch-Loading hinzufügen
2. `OrganisationRepositoryDataJdbcAdapter.java` - Country-Batch-Loading
3. `EventRepositoryDataJdbcAdapter.java` - Organisation/Country/Certificate-Batch-Loading
4. `PersonRepositoryDataJdbcAdapter.java` - Batch-findOrCreate
5. `ResultListRepositoryDataJdbcAdapter.java` - Batch-Operationen
6. `RaceRepositoryDataJdbcAdapter.java` - Batch-Operationen
7. `SplitTimeListRepositoryDataJdbcAdapter.java` - Batch-Operationen
8. `CupRepositoryDataJdbcAdapter.java` - Batch-Loading

### DBO-Anpassungen

Die `asXxx()`-Methoden in DBOs müssen überladene Varianten erhalten:
```java
// Bestehend (für Einzelabfragen)
public Organisation asOrganisation(Function<Long, Organisation> orgResolver, 
                                    Function<Long, Country> countryResolver)

// Neu (für Batch-Loading)
public Organisation asOrganisation(Map<Long, Organisation> orgMap, 
                                    Map<Long, Country> countryMap)
```

## Success Metrics

| Endpunkt | Baseline (Queries) | Ziel (Queries) | Reduktion |
|----------|-------------------|----------------|-----------|
| GET /api/events | ~100+ (N+1) | ≤5 | >90% |
| GET /api/organisations | ~50+ (N+1) | ≤3 | >90% |
| GET /api/persons | 1 | 1 | 0% (bereits optimiert) |
| GET /api/cups | ~200+ (N+1) | ≤10 | >90% |
| POST /api/results (import) | ~1000+ (N+1) | ≤20 | >95% |

### Verifizierung

1. **Log-Analyse:** SQL-Logging aktivieren, Query-Count zählen
2. **Prometheus-Metriken:** `jdbc.connections.active`, `hikaricp.connections.usage`
3. **Actuator-Endpoints:** `/actuator/metrics/jdbc.connections.active`
4. **Manuelle Tests:** Frontend-Performance in Browser DevTools

## Open Questions

1. Soll für sehr große Batch-Queries ein Chunking implementiert werden (z.B. max 1000 IDs pro IN-Clause)?
2. Sollen die optimierten Methoden die bestehenden ersetzen oder als separate Methoden existieren?
3. Wie sollen die Performance-Messungen dokumentiert werden (Wiki, README, etc.)?
