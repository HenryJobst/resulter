# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Resulter is a full-stack orienteering competition management system that handles result uploads, certificate generation, and cup scoring (German Nebel-Cup, Kristall-Cup, North-East-Ranking). Built with Spring Boot 4 backend and Vue 3 frontend.

## Development Commands

### Backend

```bash
# From root directory (using Maven wrapper and master pom)
./mvnw clean install                   # Build entire project
./mvnw compile                         # Compile backend
./mvnw test                            # Run backend tests
./mvnw spring-boot:run -pl backend     # Run application
./mvnw spotless:check -pl backend      # Check code formatting
./mvnw spotless:apply -pl backend      # Apply code formatting

# Or use mvn if you have Maven installed
mvn clean install                      # Build entire project
mvn compile                            # Compile backend

# Docker build (Dockerfile-based)
./backend/build.sh                     # Build Docker image

# Paketo Buildpacks build (alternative)
./backend/build-paketo.sh              # Build with Cloud Native Buildpacks
# Requires pack CLI: brew install buildpacks/tap/pack
```

### Frontend (from /frontend directory)

```bash
pnpm install                           # Install dependencies
pnpm dev                               # Start dev server (Vite on port 5173)
pnpm build                             # Type-check and build for production
pnpm test:unit                         # Run unit tests (Vitest)
pnpm test:e2e                          # Run E2E tests (Cypress)
pnpm test:e2e:dev                      # Open Cypress in dev mode
pnpm lint                              # Lint with ESLint
pnpm lint:fix                          # Fix linting issues
pnpm type-check                        # Type check with vue-tsc

# Docker build (Dockerfile-based)
./build.sh                             # Build Docker image

# Paketo Buildpacks build (alternative)
./build-paketo.sh                      # Build with Cloud Native Buildpacks
# Requires pack CLI: brew install buildpacks/tap/pack
```

### Running Single Tests

```bash
# Backend - single test class
./mvnw test -Dtest=EventServiceImplTest -pl backend

# Backend - single test method
./mvnw test -Dtest=EventServiceImplTest#testCreateEvent -pl backend

# Frontend - single test file
cd frontend && pnpm test:unit src/features/event/services/EventService.spec.ts
```

## Technology Stack

**Backend:** Java 21, Spring Boot 4.0.1, Spring Data JDBC (not JPA), PostgreSQL/H2, Liquibase, Maven, iText PDF, Testcontainers

**Frontend:** TypeScript, Vue 3 (Composition API), Vite, Pinia, Tanstack Query, PrimeVue 4, Tailwind CSS, Vitest, Playwright/Cypress, pnpm, Nx

**Infrastructure:** Keycloak OAuth2, Docker, Traefik (reverse proxy), Prometheus, Grafana

## Architecture

### Backend - Hexagonal Architecture (Ports & Adapters)

The backend follows strict hexagonal architecture with DDD principles enforced by JMolecules annotations:

**Layer Structure:**

1. **Domain Layer** (`backend/src/main/java/de/jobst/resulter/domain/`)
   - Pure business logic with no framework dependencies
   - Core aggregates: Event, Person, Organisation, Cup, ResultList, Course, Race
   - Value objects for type safety (CourseId, EventName, etc.)
   - Scoring algorithms for cup calculations

2. **Application Layer** (`backend/src/main/java/de/jobst/resulter/application/`)
   - Service interfaces and implementations (Interface + ServiceImpl pattern)
   - Port interfaces defining repository contracts (`application/port/`)
   - Certificate generation logic (`application/certificate/`)
   - Business use cases orchestrating domain logic

3. **Adapter Layer** (`backend/src/main/java/de/jobst/resulter/adapter/`)
   - **Driver Adapters** (`adapter/driver/web/`): REST Controllers, DTOs, XML result parsing
   - **Driven Adapters** (`adapter/driven/`):
     - `jdbc/` - Spring Data JDBC repository implementations
     - `inmemory/` - In-memory implementations for testing

**Key Patterns:**
- Dependencies point inward. Domain never depends on application or adapters. Application depends on domain but not adapters. Adapters depend on both but are pluggable.
- Controllers (driver adapters) cannot autowire Repositories directly - they must use Services
- Services cannot query the database directly - they must use Repository methods
- Data flows: DTOs/Domain Entities between Controllers and Services; DBOs between Repositories and Services

**Batch Loading Pattern (N+1 Query Optimization):**

Spring Data JDBC's `@MappedCollection` causes N+1 queries (1 main query + N queries for each association). Use the Batch Loading Pattern to eliminate this:

1. **Load entities WITHOUT associations** using custom queries
2. **Batch load associations** in a single query for all entities
3. **Populate associations** programmatically

Example implementation (see `EventRepositoryDataJdbcAdapter.java`):

```java
// 1. Load events without MappedCollection
List<EventDbo> eventDbos = eventJdbcRepository.findAllEventsWithoutOrganisations();

// 2. Batch load event-organisation mappings (single query for all events)
List<Long> eventIds = eventDbos.stream().map(EventDbo::getId).toList();
Map<Long, Set<EventOrganisationDbo>> eventOrgMap =
    eventJdbcRepository.findOrganisationsByEventIds(eventIds).stream()
        .collect(Collectors.groupingBy(eo -> eo.getEventId().getId(), Collectors.toSet()));

// 3. Populate organisations in EventDbo objects
eventDbos.forEach(event -> {
    Set<EventOrganisationDbo> orgs = eventOrgMap.getOrDefault(event.getId(), Collections.emptySet());
    event.setOrganisations(orgs);
});

// 4. Batch load organisation entities (if needed)
Map<Long, Organisation> orgMap = batchLoadOrganisations(eventDbos);
```

**For paginated queries**, implement custom repository fragment with JdbcClient:

```java
// Custom interface
public interface EventJdbcRepositoryCustom {
    Page<EventDbo> findAllWithoutOrganisations(Pageable pageable);
}

// Implementation using JdbcClient (no MappedCollection loading)
public class EventJdbcRepositoryImpl implements EventJdbcRepositoryCustom {
    private final JdbcClient jdbcClient;

    @Override
    public Page<EventDbo> findAllWithoutOrganisations(Pageable pageable) {
        String query = "SELECT id, name, ... FROM event " + orderByClause + " LIMIT ? OFFSET ?";
        List<EventDbo> eventDbos = jdbcClient.sql(query)
            .param(pageable.getPageSize())
            .param(pageable.getOffset())
            .query(new EventDboRowMapper())
            .list();
        // Return PageImpl with total count
    }
}
```

**Performance improvement:** Query count reduced from N+1 to 3-4 queries (70-80% reduction).

**Reference implementations:**
- `EventJdbcRepositoryCustom.java` - Custom repository interface
- `EventJdbcRepositoryImpl.java` - JdbcClient-based pagination implementation
- `EventRepositoryDataJdbcAdapter.java` - Service adapter with batch loading orchestration
- `EventJdbcRepository.java` - Base repository with batch loading queries

**JdbcClient vs NamedParameterJdbcTemplate:**

Always use `JdbcClient` (Spring 6.1+) instead of `NamedParameterJdbcTemplate`:

```java
// ✓ Preferred: JdbcClient (fluent, modern)
List<PersonDbo> found = jdbcClient.sql("SELECT * FROM person WHERE id IN (:ids)")
    .param("ids", idList)
    .query((rs, rowNum) -> mapRow(rs))
    .list();

// ✗ Deprecated: NamedParameterJdbcTemplate
MapSqlParameterSource params = new MapSqlParameterSource();
params.addValue("ids", idList);
List<PersonDbo> found = namedParameterJdbcTemplate.query(sql, params, rowMapper);
```

**For batch operations with many items**, chunk into batches to avoid parameter limits.

Use `BatchUtils` utility class (`application/util/BatchUtils.java`) for cleaner batch processing:

```java
import de.jobst.resulter.application.util.BatchUtils;

// ✓ Preferred: Using BatchUtils (cleaner, reusable)
public void deleteAllByKeys(Set<DomainKey> keys) {
    BatchUtils.processInBatches(keys, this::deleteBatch);
}

// With custom batch size
public void deleteAllByKeys(Set<DomainKey> keys) {
    BatchUtils.processInBatches(keys, 1000, this::deleteBatch);
}

// Manual approach (avoid if possible)
private static final int BATCH_SIZE = 500;

public void deleteAllByKeys(Set<DomainKey> keys) {
    List<DomainKey> keyList = new ArrayList<>(keys);
    for (int start = 0; start < keyList.size(); start += BATCH_SIZE) {
        List<DomainKey> batch = keyList.subList(start, Math.min(start + BATCH_SIZE, keyList.size()));
        deleteBatch(batch);  // Single DELETE with OR-conditions
    }
}
```

**BatchUtils features:**
- Default batch size: 500 items
- Handles empty/null collections gracefully
- Converts any Collection to List if needed
- Accepts Consumer<List<T>> for batch processing logic

**OR-based DELETE/UPDATE pattern for composite keys:**

When deleting or updating by composite keys, use OR-conditions in a single query instead of `batchUpdate()`:

```java
// ✓ Preferred: Single query with OR-conditions
private void deleteBatch(List<DomainKey> batch) {
    StringBuilder sql = new StringBuilder("DELETE FROM table_name WHERE ");
    Map<String, Object> params = new HashMap<>();
    List<String> conditions = new ArrayList<>();

    int idx = 0;
    for (DomainKey key : batch) {
        String condition = "(cup_id = :c" + idx
                + " AND result_list_id = :r" + idx
                + " AND status = :s" + idx + ")";
        conditions.add(condition);

        params.put("c" + idx, key.cupId().value());
        params.put("r" + idx, key.resultListId().value());
        params.put("s" + idx, key.status());
        idx++;
    }

    sql.append(String.join(" OR ", conditions));

    jdbcClient.sql(sql.toString())
        .params(params)
        .update();
}

// ✗ Avoid: batchUpdate with multiple round trips
namedParameterJdbcTemplate.batchUpdate(sql, batchParams.toArray(new MapSqlParameterSource[0]));
```

**Benefits:** Single database round trip instead of N queries, better performance for batch operations.

**Reference implementation:** `CupScoreListJdbcCustomRepositoryImpl.deleteBatch()`

### Frontend - Feature-Based Structure

Organized by features in `frontend/src/features/`:

```
features/
├── cup/              # Cup management and scoring
├── certificate/      # Certificate template design
├── event/            # Event and result management
├── person/           # Person/participant management
├── course/           # Course definitions
├── common/           # Shared utilities and components
└── [feature]/
    ├── model/        # TypeScript types/interfaces
    ├── services/     # API services (Tanstack Query)
    ├── stores/       # Pinia stores
    ├── pages/        # Route pages
    └── widgets/      # Components
```

## Backend Coding Conventions

### Dependency Injection

- **Always use constructor injection** for dependencies in production code
- Never use `@Autowired` field injection (except in test classes)
- Declare dependencies as `private final` fields
- Spring automatically performs constructor injection when there's a single constructor

### Database Entities (DBOs)

```java
@Data
public class EventDbo {
    @Id @With @Column("id")
    private Long id;

    @Column("organisation_id")
    private AggregateReference<OrganisationDbo, Long> organisationId;

    private String name;
    private LocalDate eventDate;
}
```

- Suffix: `*Dbo`
- Annotate: `@Data` (Lombok)
- IDs: `@Id`, `@With`, `@Column("id")`
- References: `AggregateReference<EntityType, Long>` for relationships

### Repositories

```java
@Repository
public interface EventRepository extends CrudRepository<EventDbo, Long> {
    @Query("SELECT e.* FROM event e WHERE e.organisation_id = :orgId")
    List<EventDbo> findByOrganisationId(@Param("orgId") Long orgId);
}
```

- Port interface in `application/port/`
- Implementation in `adapter/driven/jdbc/`
- Use **native SQL** with `@Query` for custom queries (not JPQL)
- Return DTOs for complex multi-join queries to avoid N+1 problems
- No lazy loading - use explicit joins in queries

### Services

```
// Port interface
public interface EventService {
    EventDto createEvent(CreateEventRequest request);
    Optional<EventDto> findById(Long id);
}

// Implementation
@Service
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;

    public EventServiceImpl(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    @Transactional
    public EventDto createEvent(CreateEventRequest request) {
        // Validate, create entity, save
        EventDbo event = new EventDbo(/* ... */);
        EventDbo saved = eventRepository.save(event);
        return toDto(saved);
    }
}
```

- Pattern: Interface + `*ServiceImpl`
- Annotate implementation with `@Service`
- Use `@Transactional` for multi-DB operations
- Return DTOs, not entities (unless necessary for internal use)
- Use `.orElseThrow()` for existence checks

### Controllers

```
@RestController
@RequestMapping("/api/events")
public class EventController {
    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EventDto>> createEvent(@RequestBody CreateEventRequest request) {
        EventDto event = eventService.createEvent(request);
        return ResponseEntity.ok(ApiResponse.success(event));
    }
}
```

- Annotate: `@RestController`
- Class-level `@RequestMapping` for base path (e.g., `/api/events`)
- Return `ResponseEntity<ApiResponse<T>>`
- No try-catch blocks - GlobalExceptionHandler handles all exceptions
- DTOs for request/response, never expose entities

### DTOs

```
public record EventDto(
    Long id,
    String name,
    LocalDate eventDate,
    Long organisationId
) {
    // Compact canonical constructor for validation
    public EventDto {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be blank");
        }
    }
}
```

- Use Java records
- Compact canonical constructor for validation
- Validate not null, not blank, valid ranges

### Mappers

Mapper classes convert between Domain entities and DTOs. Located in `adapter/driver/web/mapper/`:

```java
@Component
public class OrganisationMapper {

    private final CountryService countryService;
    private final OrganisationService organisationService;

    public OrganisationMapper(CountryService countryService, OrganisationService organisationService) {
        this.countryService = countryService;
        this.organisationService = organisationService;
    }

    // Single entity conversion (may cause N+1 if used in loops)
    public OrganisationDto toDto(Organisation organisation) {
        Optional<Country> country = Optional.ofNullable(organisation.getCountry())
            .flatMap(countryService::findById);
        return new OrganisationDto(/* ... */);
    }

    // Batch conversion with pre-loaded context (avoids N+1)
    public OrganisationDto toDto(Organisation organisation, 
            Map<CountryId, Country> countryMap, 
            Map<OrganisationId, Organisation> orgMap) {
        Country country = organisation.getCountry() != null 
            ? countryMap.get(organisation.getCountry()) : null;
        return new OrganisationDto(/* ... */);
    }

    // Batch method: loads all dependencies once, then maps
    public List<OrganisationDto> toDtos(List<Organisation> organisations) {
        Map<CountryId, Country> countryMap = countryService.batchLoadForOrganisations(organisations);
        Map<OrganisationId, Organisation> orgMap = organisationService.batchLoadChildOrganisations(organisations);
        return organisations.stream().map(o -> toDto(o, countryMap, orgMap)).toList();
    }

    // Static method for simple key DTOs (no dependencies needed)
    public static OrganisationKeyDto toKeyDto(Organisation organisation) {
        return new OrganisationKeyDto(organisation.getId().value(), organisation.getName().value());
    }
}
```

**Mapper Conventions:**
- Annotate with `@Component`
- Use constructor injection for service dependencies
- Provide three method variants:
  - `toDto(Entity)` - single entity, fetches dependencies on demand
  - `toDto(Entity, Map..., Map...)` - single entity with pre-loaded context
  - `toDtos(List<Entity>)` - batch conversion, loads dependencies once via `batchLoadFor*()` methods
- `toKeyDto(Entity)` - static method for simple key/reference DTOs (id + name)
- Always use batch methods (`toDtos`) when converting lists to avoid N+1 queries

### API Response Structure

All API endpoints return `ApiResponse<T>`:

```java
@Setter
@Getter
public class ApiResponse<T> {
    private boolean success;              // was successful or not
    private LocalizableString message;    // localizable message to describe the result
    private T data;                       // actual response data of type T
    private List<String> errors;          // list of errors if request failed
    private int errorCode;                // integer error code for error classification
    private long timestamp;               // time the response was generated
    private String path;                  // URL path of the request (for debugging)
}
```

Use `ResponseUtil` helper methods for creating responses:
- `ResponseUtil.success(data)` - for successful responses
- `ResponseUtil.error(errors, message, errorCode, path, exception)` - for error responses

### Error Handling

All errors are handled centrally by `GlobalExceptionHandler` (annotated with `@RestControllerAdvice`):

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGeneralException(
            Exception ex, HttpServletRequest request) {
        return ResponseUtil.error(
            Collections.singletonList(ex.getMessage()),
            LocalizableString.of(MessageKeys.UNEXPECTED_ERROR),
            AdditionalStatusCodes.UNEXPECTED.value(),
            request.getRequestURI(),
            ex);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFoundException(
            ResourceNotFoundException ex, HttpServletRequest request) {
        return ResponseUtil.error(
            Collections.singletonList(ex.getMessage()),
            LocalizableString.of(MessageKeys.RESOURCE_NOT_FOUND),
            HttpStatus.NOT_FOUND.value(),
            request.getRequestURI(),
            ex);
    }

    // Additional handlers for:
    // - ResponseNotFoundException
    // - OptimisticEntityLockException
    // - IllegalArgumentException
    // - ConstraintViolationException
    // - MethodArgumentNotValidException
}
```

**Error Handling Rules:**
- Controllers should NOT have try-catch blocks - let GlobalExceptionHandler handle all exceptions
- Use `LocalizableString.of(MessageKeys.*)` for user-facing messages
- Common exception types: `ResourceNotFoundException`, `OptimisticEntityLockException`, `IllegalArgumentException`
- Validation errors are automatically caught and formatted with field-level details

## Frontend Coding Conventions

### Code Style (ESLint - Antfu Config)

- 4 space indentation
- Single quotes
- No semicolons
- 100 character line width
- TypeScript strict mode
- Vue 3 Composition API only (no Options API)

### Component Structure

```
<script setup lang="ts">
import { ref, computed } from 'vue'
import { useEventStore } from '@/features/event/stores/eventStore'

const eventStore = useEventStore()
const selectedEvent = ref<Event | null>(null)

const filteredEvents = computed(() => {
    // computed logic
})
</script>

<template>
    <div class="event-list">
        <!-- template -->
    </div>
</template>

<style scoped>
/* scoped styles */
</style>
```

### State Management

- **Pinia** for global state (user preferences, UI state)
- **Tanstack Query** for server state (API data fetching, caching)
- Pinia plugin for state persistence

### Service Pattern (Tanstack Query)

```typescript
import { useQuery, useMutation } from '@tanstack/vue-query'
import api from '@/common/services/api'

export function useEvents() {
    return useQuery({
        queryKey: ['events'],
        queryFn: () => api.get<Event[]>('/api/events')
    })
}

export function useCreateEvent() {
    return useMutation({
        mutationFn: (event: CreateEventRequest) =>
            api.post<Event>('/api/events', event),
        onSuccess: () => {
            queryClient.invalidateQueries(['events'])
        }
    })
}
```

## Important Patterns and Rules

### Code Comment Guidelines
**Comments:** Use English only.
**Comment Style:** Be sparse. Focus on the "why", not the "what".

### Spring Data JDBC (Not JPA)

This project uses Spring Data JDBC, **not** JPA/Hibernate:

- No lazy loading - use `@Query` with explicit joins or Batch Loading Pattern
- Use DTOs for complex queries to avoid N+1 problems
- `AggregateReference<T, ID>` for relationships instead of `@ManyToOne`
- Explicit cascade handling in services

### SOLID Principles

The codebase strictly follows SOLID principles:

- **Single Responsibility:** Each class/module has one reason to change
- **Open/Closed:** Open for extension, closed for modification
- **Liskov Substitution:** Subtypes must be substitutable for base types
- **Interface Segregation:** Many specific interfaces over one general interface
- **Dependency Inversion:** Depend on abstractions, not concretions

Also: DRY (Don't Repeat Yourself), KISS (Keep It Simple), YAGNI (You Aren't Gonna Need It)

### Security

- OAuth2 Resource Server with Keycloak
- JWT token validation
- Same-origin architecture (no CORS needed)
- Session cookies with `SameSite=Lax` (CSRF protection)
- OWASP best practices enforced
- Input validation in DTOs
- SQL injection prevention via parameterized queries
- XSS prevention via proper escaping

### Testing Strategy

**Backend:**
- Unit tests for services and domain logic
- Integration tests with Testcontainers (real PostgreSQL)
- ArchUnit tests to validate architecture constraints
- JMolecules annotations for architecture validation
- `@Autowired` field injection is acceptable in test classes only

**Frontend:**
- Unit tests (Vitest) for services and utilities
- Component tests for Vue components
- E2E tests (Playwright/Cypress) for critical user flows

## Project-Specific Details

### Certificate Generation

Certificate templates are designed in the UI using a visual editor. The system:
1. Stores template definitions (layout, fields, styling)
2. Generates PDFs using iText library
3. Populates templates with result data
4. Supports placeholders for dynamic content

### Cup Scoring

Three cup systems implemented in `backend/src/main/java/de/jobst/resulter/domain/scoring/`:
- **Nebel-Cup:** German regional cup scoring
- **Kristall-Cup:** Crystal cup scoring system
- **North-East-Ranking:** Regional ranking calculations

Each has unique scoring algorithms and rules encoded in domain services.

### Result File Parsing

XML result files (IOF format) parsed in `backend/src/main/java/de/jobst/resulter/adapter/driver/web/xml/`:
- Validates against IOF schema
- Extracts race results, courses, competitors
- Links to existing persons/organizations or creates new ones
- Updates result lists and rankings

## Database & Migrations

- **Liquibase** manages schema migrations in `backend/src/main/resources/db/changelog/`
- Never modify existing changesets - always create new ones
- PostgreSQL in production, H2 for dev/testing
- Schema includes: events, persons, courses, results, cups, certificates, organizations

## Deployment

### Architecture Overview

**Same-Domain Path-Based Routing:**
- Frontend: `https://resulter.olberlin.de/` → Frontend container (nginx)
- Backend: `https://resulter.olberlin.de/api` → Backend container (Spring Boot)
- Traefik reverse proxy handles routing and SSL termination
- Backend router has higher priority (100) to match `/api` before frontend fallback
- `stripprefix` middleware removes `/api` before forwarding to backend

### Environment Configuration

1. **Copy environment template:** `cp deploy/resulter/.env.example deploy/resulter/.env`
2. **Configure variables:**
   - `RESULTER_HOSTNAME`: Your domain (e.g., `resulter.olberlin.de`)
   - `VITE_API_ENDPOINT`: Must be `https://resulter.olberlin.de/api` (path-based routing)
   - `COOKIE_DOMAIN`: Leave empty (not needed for same-domain)
   - `API_CORS_ALLOWED_ORIGINS`: Leave empty (same-origin, no CORS needed)
   - Database, Keycloak, and other settings as needed

### Docker Images

Two build approaches available:

**Dockerfile-based (default):**
- Backend: `./backend/build.sh` builds Docker image
- Frontend: `./frontend/build.sh` builds Docker image
- CI/CD: `.github/workflows/build-image.yml`
- Tags: `latest`, `4.6.4`, `4`, `4.6`

**Paketo Buildpacks (alternative):**
- Backend: `./backend/build-paketo.sh` builds with Cloud Native Buildpacks
- Frontend: `./frontend/build-paketo.sh` builds with Cloud Native Buildpacks
- CI/CD: `.github/workflows/build-paketo.yml`
- Tags: `paketo-latest`, `paketo-4.6.4`, `paketo-4`, `paketo-4.6`
- Requires pack CLI: `brew install buildpacks/tap/pack`
- Benefits: 12-factor app, runtime config, automatic SBOM, non-root user, auto-updating base images

**Key Differences:**
- **Config**: Dockerfile uses build-time ARGs; Paketo uses runtime ENV vars
- **Base**: Dockerfile uses Alpine; Paketo uses Ubuntu Jammy
- **User**: Dockerfile runs as root; Paketo runs as non-root (UID 1000)
- **Paths**: Dockerfile uses `/app`; Paketo uses `/workspace`
- **SBOM**: Paketo auto-generates Software Bill of Materials

**Deployment:**
- Full deployment: Docker Compose file in `deploy/resulter/docker-compose.yml`

### Traefik Configuration

Backend service labels in docker-compose.yml:
```yaml
- "traefik.http.routers.resulter-api.rule=Host(`${RESULTER_HOSTNAME}`) && PathPrefix(`/api`)"
- "traefik.http.routers.resulter-api.priority=100"  # Higher than frontend
- "traefik.http.middlewares.resulter-api-stripprefix.stripprefix.prefixes=/api"
- "traefik.http.routers.resulter-api.middlewares=resulter-api-stripprefix,compresstraefik"
```

### Local Development

- Frontend: `cd frontend && pnpm dev` (runs on localhost:5173)
- Backend: `cd backend && ./mvnw spring-boot:run` (runs on localhost:8080)
- Vite proxy configured to forward `/api` requests to backend
- `.env.development`: `VITE_API_ENDPOINT=/api` (uses Vite proxy)

## Profiles

- **dev** (default): H2 database, debug logging, Swagger UI enabled
- **prod**: PostgreSQL, production logging, security enabled
- **native**: GraalVM native image compilation

## Monitoring & Observability

- Spring Boot Actuator endpoints at `/actuator`
- Prometheus metrics at `/actuator/prometheus`
- Grafana dashboards for visualization
- Health checks, info endpoints

## Version & License

- Current version: 4.6.4
- License: CC BY-NC-ND 4.0 (non-commercial use only)
- Main branch: `main` for stable releases
