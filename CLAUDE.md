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

# Docker build
./backend/build.sh                     # Build Docker image
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

**Backend:** Java 21, Spring Boot 4.0.0, Spring Data JDBC (not JPA), PostgreSQL/H2, Liquibase, Maven, iText PDF, Testcontainers

**Frontend:** TypeScript, Vue 3 (Composition API), Vite, Pinia, Tanstack Query, PrimeVue 4, Tailwind CSS, Vitest, Playwright/Cypress, pnpm, Nx

**Infrastructure:** Keycloak OAuth2, Docker, Prometheus, Grafana

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
- Use **native SQL** with `@Query` for custom queries (Spring Data JDBC, not JPA/JPQL)
- Return DTOs for complex multi-join queries to avoid N+1 problems
- No lazy loading - use explicit joins in queries

### Services

```java
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
- Use **constructor injection** for dependencies (not `@Autowired` field injection)
- Use `@Transactional` for multi-DB operations
- Return DTOs, not entities (unless necessary for internal use)
- Use `.orElseThrow()` for existence checks
- ServiceImpl classes should use Repository methods, not query database directly

### Controllers

```java
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
- Use **constructor injection** for dependencies (not `@Autowired` field injection)
- Return `ResponseEntity<ApiResponse<T>>`
- No try-catch blocks - GlobalExceptionHandler handles all exceptions
- DTOs for request/response, never expose entities
- Controllers should not autowire Repositories directly - use Services instead

### DTOs

```java
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

```typescript
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

### Spring Data JDBC (Not JPA)

This project uses Spring Data JDBC, **not** JPA/Hibernate:

- No lazy loading - use `@Query` with explicit joins
- Use DTOs for complex queries to avoid N+1 problems
- `AggregateReference<T, ID>` for relationships instead of `@ManyToOne`
- Explicit cascade handling in services
- Database schema managed by Liquibase changesets

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
- CORS configured for frontend origin
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

- Environment configuration: Copy `.env.example` to `.env` and configure
- Backend Docker: `./backend/build.sh` builds Docker image
- Frontend Docker: Separate `build.sh` in frontend directory
- Full deployment: Docker Compose file available in `deploy/resulter/`

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

- Current version: 1.14.0
- License: CC BY-NC-ND 4.0 (non-commercial use only)
- Main branch: `main` for stable releases
