# ADR 0004: Backend-for-Frontend with Keycloak and session cookie

> [Deutsch](0004-bff-with-keycloak-and-session-cookie.de.md) · **English**

- **Status:** accepted
- **Date:** 2025-12-25
- **Context:** Authentication and authorization; commits "implement bff frontend+backend part" (2025-12-25/26). A first Keycloak integration in the frontend already existed on 2023-12-24 ("First keycloak integration for the frontend").

## Context

The frontend is a single-page application, the backend a REST API. Users are managed
in Keycloak. Until the end of 2025 the frontend held the tokens itself (Keycloak in the
browser, bearer header to the backend). With the BFF commit the backend runs the
OAuth2/OIDC flow and gives the browser only a session cookie.
The rationale comes from the project owner: the BFF pattern is the most secure option for
this architecture. In the repository, the README mentions the pattern only as a property
of the architecture.

## Decision

1. **The backend is an OAuth2 client of Keycloak** (authorization code flow,
   registration `keycloak`, scopes `openid,profile,email`). The browser calls
   `/oauth2/authorization/keycloak`, the backend exchanges the code for tokens and
   keeps them on the server side. The authorization flow stores its intermediate state in
   a cookie (`CookieOAuth2AuthorizationRequestRepository`), not in the session.

2. **Sessions are stored in the database.** `spring-session-jdbc` with table
   `spring_session`, timeout 3600 s, cookie `RESULTER_SESSION` with `HttpOnly`,
   `Secure` and `SameSite=Lax`. One session per user is allowed
   (`maximumSessions(1)`).

3. **The frontend sends cookies and a CSRF header.** `withCredentials: true`
   in `api.ts`; the CSRF token is in the readable cookie `XSRF-TOKEN` and is sent back as
   `X-XSRF-TOKEN`. `/bff/logout` is exempt from CSRF.
   `/bff/user` returns the user information without handing a token to the browser.

4. **Several filter chains by order.** Special chains come before the
   session API: database creation for E2E (`@Order(0)`), Prometheus (1), bearer JWT API
   (2), BFF login (3), all other requests with a session (4). The JWT resource server chain
   for calls with `Authorization: Bearer` remains in place.

5. **Roles from Keycloak.** Realm roles and groups are translated into Spring
   authorities.

## Consequences

**Positive**

- No access or refresh tokens in the browser; the session is protected by an
  `HttpOnly` cookie.
- One domain for frontend and API; no token management in the frontend.

**Negative / costs**

- Server-side state (session table) and CSRF protection are required.
- Behavior behind a reverse proxy with a path prefix is tricky: the bug
  "BFF logout returned 403" (version 4.8.14) was caused by Traefik `stripprefix` and
  `X-Forwarded-Prefix`; since then the logout check uses `getServletPath()`.
- The profiles `dev`, `e2e-frontend-tests` and `nosecurity` need their own configuration
  (`OAuth2ResourceServerNoSecurityConfiguration`, `JwtAuthConverterNoSecurity`).

## Alternatives (not documented, obvious candidates)

- **Tokens in the browser** (Keycloak JS, state before 2025-12-25): simpler in the backend, but
  tokens are reachable by scripts.
- **Bearer JWT resource server only:** remains for API clients, without BFF login.

## Related documents

- [`BffSecurityConfiguration.java`](../../backend/src/main/java/de/jobst/resulter/springapp/config/BffSecurityConfiguration.java)
- [`OAuth2ResourceServerSecurityConfiguration.java`](../../backend/src/main/java/de/jobst/resulter/springapp/config/OAuth2ResourceServerSecurityConfiguration.java)
- [`SessionConfiguration.java`](../../backend/src/main/java/de/jobst/resulter/springapp/config/SessionConfiguration.java)
- [`BffController.java`](../../backend/src/main/java/de/jobst/resulter/adapter/driver/web/BffController.java)
- [`application.properties`](../../backend/src/main/resources/application.properties)
- [`api.ts`](../../frontend/src/features/auth/services/api.ts)
- [`bffAuthService.ts`](../../frontend/src/features/auth/services/bffAuthService.ts)
- [`CHANGELOG.md`](../../CHANGELOG.md) (entry 4.8.14)
