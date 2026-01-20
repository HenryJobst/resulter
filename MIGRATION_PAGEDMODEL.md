# Migration Plan: PageImpl → PagedModel für paginierte API-Endpunkte

## Zusammenfassung

Migration aller paginierten Backend-Endpunkte von `ResponseEntity<Page<T>>` zu `ResponseEntity<PagedModel<T>>`, um die Abhängigkeit von Spring's `PageImpl` (Implementierungsdetail) in der API-Serialisierung zu eliminieren.

**Wichtige Erkenntnis**: Das Projekt hat bereits `@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)` in `ResulterApplication.java:21` konfiguriert. Spring konvertiert `PageImpl` bereits automatisch zu `PagedModel` bei der JSON-Serialisierung. Diese Migration macht die API-Signatur explizit und dokumentiert das tatsächliche Laufzeitverhalten.

**Auswirkung auf Frontend**: Keine Änderungen erforderlich - die JSON-Struktur bleibt identisch.

## Betroffene Backend-Controller (10 Endpunkte in 9 Controllern)

### Einfach (Start-Kandidaten)
1. **CourseController** (`adapter/driver/web/CourseController.java:35`)
   - 1 Endpunkt: `GET /course`
   - TODO: Filter/Pageable noch nicht vollständig implementiert

2. **RaceController** (`adapter/driver/web/RaceController.java`)
   - 1 Endpunkt: `GET /race`
   - TODO: Filter/Pageable noch nicht vollständig implementiert

3. **MediaFileController** (`adapter/driver/web/MediaFileController.java:44`)
   - 1 Endpunkt: `GET /media`

### Mittel
4. **OrganisationController** (`adapter/driver/web/OrganisationController.java:45`)
   - 1 Endpunkt: `GET /organisation`

5. **EventCertificateController** (`adapter/driver/web/EventCertificateController.java:58`)
   - 1 Endpunkt: `GET /event_certificate`

6. **CupController** (`adapter/driver/web/CupController.java:72-85`)
   - 1 Endpunkt: `GET /cup`

### Komplex (zuletzt)
7. **EventController** (`adapter/driver/web/EventController.java:77-89`)
   - 1 Endpunkt: `GET /event`
   - Nutzt `hasSplitTimes()` Helper-Methode im Mapping

8. **PersonController** (`adapter/driver/web/PersonController.java:40-63, 66-85`)
   - 2 Endpunkte: `GET /person`, `GET /person/duplicates`
   - Komplexe `groupLeaders` Logik für Merge-Button-Anzeige
   - Doppelte Property-Mapping-Calls

## Neue Utility-Klasse: PagedModelUtil

**Zweck**: Boilerplate für PageImpl + PagedModel Konstruktion eliminieren und Domain → DTO Mapping vereinfachen.

**Datei**: `backend/src/main/java/de/jobst/resulter/application/util/PagedModelUtil.java`

```java
package de.jobst.resulter.application.util;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedModel;

import java.util.List;
import java.util.function.Function;

/**
 * Utility class for creating PagedModel instances from Page objects.
 * Simplifies the construction of PagedModel by handling PageImpl creation internally.
 */
public class PagedModelUtil {

    private PagedModelUtil() {
        // Utility class, prevent instantiation
    }

    /**
     * Creates a PagedModel from already mapped DTO content.
     * Use when content is already transformed to DTOs.
     *
     * @param content        The mapped DTO content list
     * @param pageable       The pageable information
     * @param totalElements  The total number of elements
     * @return PagedModel containing the content
     */
    public static <T> PagedModel<T> of(List<T> content, Pageable pageable, long totalElements) {
        return new PagedModel<>(new PageImpl<>(content, pageable, totalElements));
    }

    /**
     * Creates a PagedModel from a domain Page by applying mapping functions.
     * Handles both content mapping (domain → DTO) and sort property mapping.
     *
     * @param domainPage   The page containing domain objects
     * @param contentMapper Function to map domain object to DTO
     * @param sortMapper   Function to map DTO sort properties to domain properties
     * @return PagedModel containing mapped DTOs
     */
    public static <D, T> PagedModel<T> of(
            Page<D> domainPage,
            Function<D, T> contentMapper,
            Function<Sort.Order, String> sortMapper) {

        List<T> mappedContent = domainPage.getContent().stream()
                .map(contentMapper)
                .toList();

        Pageable mappedPageable = FilterAndSortConverter.mapOrderProperties(
                domainPage.getPageable(),
                sortMapper);

        return new PagedModel<>(new PageImpl<>(
                mappedContent,
                mappedPageable,
                domainPage.getTotalElements()));
    }
}
```

**Verwendung**:

**Einfacher Fall** (bereits gemappte DTOs):
```java
List<CourseDto> dtos = courses.stream().map(CourseDto::from).toList();
return ResponseEntity.ok(PagedModelUtil.of(dtos, pageable, courses.size()));
```

**Standard-Fall** (mit Domain → DTO Mapping):
```java
return ResponseEntity.ok(PagedModelUtil.of(
    persons,
    PersonDto::from,
    PersonDto::mapOrdersDomainToDto
));
```

**Komplexer Fall** (mit Custom Mapping-Logik wie groupLeaders):
```java
return ResponseEntity.ok(PagedModelUtil.of(
    persons,
    p -> PersonDto.from(p, groupLeaders.contains(p.id().value())),
    PersonDto::mapOrdersDomainToDto
));
```

## Implementierungsmuster

### Vorher (Beispiel PersonController:40-63)
```java
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

@GetMapping("/person")
public ResponseEntity<Page<PersonDto>> searchPersons(
        @RequestParam Optional<String> filter,
        @Nullable Pageable pageable) {

    Pageable mapped = pageable != null
        ? FilterAndSortConverter.mapOrderProperties(pageable, PersonDto::mapOrdersDtoToDomain)
        : Pageable.unpaged();

    Page<Person> persons = personService.findAllOrPossibleDuplicates(filter.orElse(null), mapped, false);

    Set<Long> groupLeaders = personService.determineGroupLeaders(persons.getContent());

    return ResponseEntity.ok(new PageImpl<>(
        persons.getContent().stream()
            .map(p -> PersonDto.from(p, groupLeaders.contains(p.id().value())))
            .toList(),
        FilterAndSortConverter.mapOrderProperties(persons.getPageable(), PersonDto::mapOrdersDomainToDto),
        persons.getTotalElements()
    ));
}
```

### Nachher (mit PagedModelUtil)
```java
import org.springframework.data.web.PagedModel;
import de.jobst.resulter.application.util.PagedModelUtil;

@GetMapping("/person")
public ResponseEntity<PagedModel<PersonDto>> searchPersons(
        @RequestParam Optional<String> filter,
        @Nullable Pageable pageable) {

    Pageable mapped = pageable != null
        ? FilterAndSortConverter.mapOrderProperties(pageable, PersonDto::mapOrdersDtoToDomain)
        : Pageable.unpaged();

    Page<Person> persons = personService.findAllOrPossibleDuplicates(filter.orElse(null), mapped, false);

    Set<Long> groupLeaders = personService.determineGroupLeaders(persons.getContent());

    return ResponseEntity.ok(PagedModelUtil.of(
        persons,
        p -> PersonDto.from(p, groupLeaders.contains(p.id().value())),
        PersonDto::mapOrdersDomainToDto
    ));
}
```

### Änderungen im Detail
1. **PagedModelUtil erstellen**: Neue Utility-Klasse in `application/util/`
2. **Import hinzufügen**: `import org.springframework.data.web.PagedModel;`
3. **Import hinzufügen**: `import de.jobst.resulter.application.util.PagedModelUtil;`
4. **Return-Typ ändern**: `ResponseEntity<Page<T>>` → `ResponseEntity<PagedModel<T>>`
5. **PageImpl + PagedModel ersetzen durch**: `PagedModelUtil.of(...)`
6. **Imports entfernen**: `PageImpl` kann oft entfernt werden (nicht mehr direkt verwendet)

**Vorteile**:
- **Kein Boilerplate**: Keine manuellen `PageImpl` + `PagedModel` Konstruktionen
- **Konsistent**: Einheitliche Verwendung in allen Controllern
- **Lesbar**: Mapping-Funktionen bleiben sichtbar und verständlich
- **Wartbar**: Zentraler Ort für Änderungen an Pagination-Logik

**Wichtig**:
- `FilterAndSortConverter` für DTO→Domain Mapping bleibt (in `pageable` vor Service-Call)
- Domain→DTO Property-Mapping ist in `PagedModelUtil.of()` integriert
- Service-Layer (`PersonService`, etc.) gibt weiterhin `Page<Domain>` zurück
- Nur Controller-Rückgabe vereinfacht sich

## Migrationsreihenfolge

**Strategie**: Alle 9 Controller in einem Commit ändern

**Empfohlene Reihenfolge bei der Bearbeitung** (für systematisches Vorgehen):

### 0. Utility-Klasse erstellen
**Erste Aufgabe**: `PagedModelUtil` Klasse in `backend/src/main/java/de/jobst/resulter/application/util/` erstellen

### 1. Einfache Controller (Start)
1. CourseController
2. RaceController
3. MediaFileController

### 2. Mittlere Komplexität
4. OrganisationController
5. EventCertificateController
6. CupController

### 3. Komplexe Controller (zuletzt)
7. EventController (mit `hasSplitTimes()` Logik)
8. PersonController (mit `groupLeaders` Logik, 2 Endpunkte)

### 4. Finale Schritte
9. Ungenutzte Imports entfernen (`PageImpl` oft nicht mehr benötigt)
10. Tests wo nötig anpassen
11. Unit-Tests für PagedModelUtil schreiben
12. Commit erstellen: "refactor: migrate paginated endpoints from Page to PagedModel with PagedModelUtil"

## Service-Layer: Keine Änderungen

**Begründung**: Service-Interfaces und -Implementierungen geben weiterhin `Page<DomainObject>` zurück. Die Controller (Adapter-Layer) sind für die Präsentations-Konvertierung zu `PagedModel` verantwortlich. Dies folgt der hexagonalen Architektur des Projekts.

**Unverändert**:
- `PersonService.findAll()` → `Page<Person>`
- `EventService.findAll()` → `Page<Event>`
- Alle anderen Service-Methoden

## Frontend: Keine Änderungen

**Begründung**: Die JSON-Struktur bleibt identisch, da Spring bereits die Konvertierung durchführt.

**Unveränderte Dateien**:
- `frontend/src/features/generic/models/rest_page_result.ts` - Type Definition
- `frontend/src/features/generic/models/page.ts` - Page Metadata
- `frontend/src/features/generic/services/GenericService.ts` - API Service
- `frontend/src/features/generic/pages/GenericList.vue` - List Component
- Alle entity-spezifischen Services und List-Pages

**JSON-Struktur (vorher und nachher identisch)**:
```json
{
  "content": [
    { "id": 1, "name": "..." }
  ],
  "page": {
    "number": 0,
    "size": 20,
    "totalElements": 100,
    "totalPages": 5
  }
}
```

## Test-Strategie

**Ansatz**: Bestehende Tests anpassen wo nötig

### Backend Unit-Tests

**Was zu prüfen**:
1. Bestehende Controller-Tests auf Return-Typ-Assertions prüfen
2. Falls Tests explizit `ResponseEntity<Page<T>>` erwarten, auf `ResponseEntity<PagedModel<T>>` anpassen
3. Falls Tests nur den Response-Body prüfen (`.getContent()`, `.getTotalElements()`), sollten diese unverändert funktionieren

**Beispiel für Anpassung** (falls nötig):

```java
@Test
void searchPersons_returnsPagedModel() {
    // Given
    Pageable pageable = PageRequest.of(0, 10);
    Page<Person> mockPage = new PageImpl<>(List.of(mockPerson));
    when(personService.findAll(any(), any())).thenReturn(mockPage);

    // When
    ResponseEntity<PagedModel<PersonDto>> response = controller.searchPersons(Optional.empty(), pageable);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getContent()).hasSize(1);
}
```

**Hinweis**: Viele Tests könnten unverändert funktionieren, da `PagedModel` die gleichen Methoden wie `Page` bietet (`getContent()`, `getTotalElements()`, etc.).

### Backend Integration-Tests
JSON-Serialisierung verifizieren:

```bash
# Backend starten
./mvnw spring-boot:run -pl backend

# Jeder Endpunkt testen
curl "http://localhost:8080/person?page=0&size=10" | jq '.content | length'
curl "http://localhost:8080/event?page=0&size=10" | jq '.page'
curl "http://localhost:8080/cup?page=0&size=10" | jq '.page.totalElements'
```

### Frontend Manual Testing
Alle Listen-Seiten im Browser testen:
1. Navigiere zu jeder Liste
2. Teste Pagination (Next/Previous, Seitensprung)
3. Teste Seitengröße ändern (10, 20, 50)
4. Teste Sortierung (Spalten-Header klicken)
5. Teste Filterung (Suchfelder)
6. Browser Console auf Fehler prüfen

### Frontend E2E-Tests
```bash
cd frontend
pnpm test:e2e
```

Verifiziert automatisch:
- List-Komponenten laden Daten
- Pagination funktioniert
- Keine API-Fehler

## Edge Cases

### 1. Leere Ergebnisse
**Test**: `Page.empty()` → `PagedModel`
- CourseController mit nicht-existierendem Filter testen
- JSON: `{content: [], page: {number: 0, size: 10, totalElements: 0, totalPages: 0}}`

### 2. Unpaged Results
**Test**: `Pageable.unpaged()` Handling
- PersonController ohne Pageable-Parameter aufrufen
- Code behandelt bereits: `pageable != null ? ... : Pageable.unpaged()`

### 3. Große Datenmengen
**Test**: >1000 Elemente
- Performance sollte identisch bleiben (PagedModel ist lightweight wrapper)

### 4. Multi-Sort
**Test**: Mehrfache Sortier-Spalten
- Bereits durch `FilterAndSortConverter.mapOrderProperties()` behandelt

### 5. Null-Handling
**Test**: `@Nullable Pageable pageable`
- Alle Controller prüfen bereits: `pageable != null`

## Risiken & Mitigations

| Risiko | Wahrscheinlichkeit | Mitigation |
|--------|-------------------|------------|
| Frontend bricht durch JSON-Änderung | Sehr niedrig | JSON-Struktur bleibt durch Spring-Config identisch |
| FilterAndSortConverter Inkompatibilität | Keine | Converter arbeitet mit Pageable, nicht PagedModel |
| Performance-Regression | Sehr niedrig | PagedModel ist lightweight wrapper, keine DB-Änderungen |
| Komplexe Controller-Logik bricht | Niedrig | `groupLeaders` und `hasSplitTimes` vor PagedModel-Wrap, nicht betroffen |

## Rollback-Plan

Falls unerwartete Probleme auftreten:

```bash
# Kompletten Commit rückgängig machen
git revert <commit-hash>

# Oder: Änderungen vor Commit verwerfen
git checkout -- backend/src/main/java/de/jobst/resulter/adapter/driver/web/*Controller.java
```

## Kritische Dateien

### Backend (zu ändern)
1. `backend/src/main/java/de/jobst/resulter/adapter/driver/web/PersonController.java` (Zeilen 40, 66)
2. `backend/src/main/java/de/jobst/resulter/adapter/driver/web/EventController.java` (Zeile 77)
3. `backend/src/main/java/de/jobst/resulter/adapter/driver/web/CourseController.java` (Zeile 35)
4. `backend/src/main/java/de/jobst/resulter/adapter/driver/web/RaceController.java`
5. `backend/src/main/java/de/jobst/resulter/adapter/driver/web/OrganisationController.java` (Zeile 45)
6. `backend/src/main/java/de/jobst/resulter/adapter/driver/web/MediaFileController.java` (Zeile 44)
7. `backend/src/main/java/de/jobst/resulter/adapter/driver/web/EventCertificateController.java` (Zeile 58)
8. `backend/src/main/java/de/jobst/resulter/adapter/driver/web/CupController.java` (Zeilen 72-85)

### Backend (neu erstellen)
- `backend/src/main/java/de/jobst/resulter/application/util/PagedModelUtil.java` (NEU)
- `backend/src/test/java/de/jobst/resulter/application/util/PagedModelUtilTest.java` (NEU)

### Backend (Kontext, nicht ändern)
- `backend/src/main/java/de/jobst/resulter/ResulterApplication.java` (Zeile 21: VIA_DTO Config)
- `backend/src/main/java/de/jobst/resulter/application/util/FilterAndSortConverter.java`

### Frontend (keine Änderungen)
- `frontend/src/features/generic/models/rest_page_result.ts`
- `frontend/src/features/generic/models/page.ts`
- `frontend/src/features/generic/services/GenericService.ts`
- `frontend/src/features/generic/pages/GenericList.vue`

## Verifikation (End-to-End)

### 1. Backend Build
```bash
cd /Users/henryprivat/Documents/gitrepos/resulter
./mvnw clean install
```
**Erwartung**: Alle Tests grün, Build erfolgreich

### 2. Backend Starten
```bash
./mvnw spring-boot:run -pl backend
```
**Erwartung**: Startet ohne Fehler auf Port 8080

### 3. API-Tests (curl)
```bash
# Person-Liste
curl -s "http://localhost:8080/person?page=0&size=5" | jq '.page'

# Duplicate-Suche
curl -s "http://localhost:8080/person/duplicates?page=0&size=5" | jq '.content | length'

# Event-Liste
curl -s "http://localhost:8080/event?page=0&size=10" | jq '.page.totalElements'

# Weitere Endpunkte analog testen
```
**Erwartung**: JSON mit Struktur `{content: [...], page: {...}}`

### 4. Frontend Starten
```bash
cd frontend
pnpm install
pnpm dev
```
**Erwartung**: Vite Dev Server startet auf Port 5173

### 5. Browser-Tests
- Navigate zu `http://localhost:5173/person`
- Navigate zu `http://localhost:5173/event`
- Navigate zu `http://localhost:5173/cup`
- ...alle anderen Listen-Seiten

**Prüfen**:
- [ ] Daten werden angezeigt
- [ ] Pagination funktioniert (Next/Previous Buttons)
- [ ] Seitengröße ändern funktioniert (Dropdown)
- [ ] Sortierung funktioniert (Spalten-Header klicken)
- [ ] Filter/Suche funktioniert (Suchfeld)
- [ ] Keine Console-Errors

### 6. E2E-Tests
```bash
cd frontend
pnpm test:e2e
```
**Erwartung**: Alle Tests grün

### 7. Code-Review & Commit
- [ ] Alle Controller verwenden `PagedModel` statt `Page` im Return-Typ
- [ ] Import `org.springframework.data.web.PagedModel` vorhanden
- [ ] Kein `PageImpl` direkt in `ResponseEntity.ok()` (nur als Zwischenvariable)
- [ ] `FilterAndSortConverter` Logik unverändert
- [ ] Keine Änderungen in Service-Layer
- [ ] Keine Änderungen in Frontend
- [ ] Ungenutzte Imports entfernt
- [ ] Tests wo nötig angepasst und alle grün
- [ ] Commit erstellen: "refactor: migrate paginated endpoints from Page to PagedModel with PagedModelUtil"

## Erfolgs-Kriterien

✅ **Migration erfolgreich, wenn**:
1. Alle 10 paginierten Endpunkte geben `ResponseEntity<PagedModel<T>>` zurück
2. Backend-Tests sind grün: `./mvnw test -pl backend`
3. Frontend E2E-Tests sind grün: `cd frontend && pnpm test:e2e`
4. Manuelle Browser-Tests zeigen funktionierende Pagination
5. JSON-Struktur ist identisch zu vorher: `{content: [...], page: {...}}`
6. Keine Console-Errors im Browser
7. API-Response-Zeiten sind unverändert
8. Code-Review abgeschlossen

## Zusätzliche Hinweise

### Spring HATEOAS vs Spring Data PagedModel
Dieses Projekt nutzt **Spring Data's PagedModel** (`org.springframework.data.web.PagedModel`), NICHT Spring HATEOAS (`org.springframework.hateoas.PagedModel`).

**Grund**:
- Keine HATEOAS-Dependency im Projekt
- `@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)` nutzt Spring Data's PagedModel
- Einfacher, leichtgewichtiger Wrapper ohne Hypermedia-Links

### Warum diese Migration?
Obwohl Spring bereits automatisch konvertiert, ist die explizite Verwendung von `PagedModel` im Return-Typ wichtig, weil:
1. **Code-Klarheit**: Signatur zeigt tatsächliches Laufzeitverhalten
2. **Spring Best Practice**: Dokumentation warnt vor direktem `PageImpl`-Export
3. **API-Transparenz**: Entwickler sehen sofort das Antwort-Format
4. **Zukunftssicherheit**: Unabhängig von Konfigurations-Flags

### Zeitaufwand
- **PagedModelUtil erstellen** (inkl. Tests): ~30-45 Minuten
- **Controller-Migration** (9 Controller): ~2-3 Stunden (schneller durch Utility)
- **Test-Anpassungen**: ~1 Stunde
- **Verifikation & Testing**: ~1-2 Stunden
- **Gesamt**: ~4-6 Stunden (ca. 1 Arbeitstag)

**Hinweis**:
- Die PagedModelUtil macht die Controller-Migration schneller und konsistenter
- Da alle Änderungen in einem Commit erfolgen, ist kein phasenweises Testen/Commiten notwendig
- Die Verifikation erfolgt einmalig am Ende
