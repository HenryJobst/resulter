# ADR 0004: Backend-for-Frontend mit Keycloak und Sitzungscookie

> **Deutsch** · [English](0004-bff-with-keycloak-and-session-cookie.md)

- **Status:** akzeptiert
- **Datum:** 2025-12-25
- **Kontext:** Authentifizierung und Autorisierung; Commits „implement bff frontend+backend part“ (2025-12-25/26). Bereits 2023-12-24 gab es eine erste Keycloak-Anbindung im Frontend („First keycloak integration for the frontend“).

## Kontext

Das Frontend ist eine Single-Page-Anwendung, das Backend eine REST-API. Die Benutzer
werden in Keycloak verwaltet. Bis Ende 2025 hielt das Frontend die Tokens selbst
(Keycloak im Browser, Bearer-Header an das Backend). Mit dem BFF-Commit führt das
Backend den OAuth2/OIDC-Ablauf aus und gibt dem Browser nur ein Sitzungscookie.
Die Begründung stammt vom Projektinhaber: Das BFF-Muster ist die sicherste Variante für
diesen Architekturaufbau. Im Repository nennt das README das Muster nur als Eigenschaft der
Architektur.

## Entscheidung

1. **Das Backend ist OAuth2-Client von Keycloak** (Authorization-Code-Flow,
   Registrierung `keycloak`, Scopes `openid,profile,email`). Der Browser ruft
   `/oauth2/authorization/keycloak` auf, das Backend tauscht den Code gegen Tokens und
   hält sie serverseitig. Der Autorisierungsablauf speichert seine Zwischenzustände in
   einem Cookie (`CookieOAuth2AuthorizationRequestRepository`), nicht in der Sitzung.

2. **Sitzungen liegen in der Datenbank.** `spring-session-jdbc` mit Tabelle
   `spring_session`, Timeout 3600 s, Cookie `RESULTER_SESSION` mit `HttpOnly`,
   `Secure` und `SameSite=Lax`. Pro Benutzer ist eine Sitzung erlaubt
   (`maximumSessions(1)`).

3. **Das Frontend sendet Cookies und einen CSRF-Header.** `withCredentials: true`
   in `api.ts`; der CSRF-Token steht im lesbaren Cookie `XSRF-TOKEN` und wird als
   `X-XSRF-TOKEN` zurückgeschickt. `/bff/logout` ist von CSRF ausgenommen.
   `/bff/user` liefert die Benutzerinformation, ohne Token an den Browser zu geben.

4. **Mehrere Filterketten nach Reihenfolge.** Spezielle Ketten stehen vor der
   Sitzungs-API: Datenbank-Anlage für E2E (`@Order(0)`), Prometheus (1), Bearer-JWT-API
   (2), BFF-Login (3), alle übrigen Anfragen mit Sitzung (4). Die JWT-Resource-Server-Kette
   für Aufrufe mit `Authorization: Bearer` bleibt bestehen.

5. **Rollen aus Keycloak.** Realm-Rollen und Gruppen werden in Spring-Authorities
   übersetzt.

## Konsequenzen

**Positiv**

- Im Browser liegen keine Access- oder Refresh-Tokens, die Sitzung ist per
  `HttpOnly`-Cookie geschützt.
- Eine Domäne für Frontend und API; keine Token-Verwaltung im Frontend.

**Negativ / Kosten**

- Zustand auf dem Server (Sitzungstabelle) und CSRF-Schutz sind nötig.
- Das Verhalten hinter einem Reverse Proxy mit Pfadpräfix ist heikel: Der Fehler
  „BFF-Logout lieferte 403“ (Version 4.8.14) entstand bei Traefik-`stripprefix` und
  `X-Forwarded-Prefix`; die Logout-Prüfung nutzt seitdem `getServletPath()`.
- Profile `dev`, `e2e-frontend-tests` und `nosecurity` brauchen eigene Konfiguration
  (`OAuth2ResourceServerNoSecurityConfiguration`, `JwtAuthConverterNoSecurity`).

## Alternativen (nicht dokumentiert, naheliegend)

- **Tokens im Browser** (Keycloak-JS, Stand vor 2025-12-25): einfacher im Backend, aber
  Tokens sind für Skripte erreichbar.
- **Nur Bearer-JWT-Resource-Server:** besteht weiter für API-Clients, ohne BFF-Anmeldung.

## Verwandte Dokumente

- [`BffSecurityConfiguration.java`](../../backend/src/main/java/de/jobst/resulter/springapp/config/BffSecurityConfiguration.java)
- [`OAuth2ResourceServerSecurityConfiguration.java`](../../backend/src/main/java/de/jobst/resulter/springapp/config/OAuth2ResourceServerSecurityConfiguration.java)
- [`SessionConfiguration.java`](../../backend/src/main/java/de/jobst/resulter/springapp/config/SessionConfiguration.java)
- [`BffController.java`](../../backend/src/main/java/de/jobst/resulter/adapter/driver/web/BffController.java)
- [`application.properties`](../../backend/src/main/resources/application.properties)
- [`api.ts`](../../frontend/src/features/auth/services/api.ts)
- [`bffAuthService.ts`](../../frontend/src/features/auth/services/bffAuthService.ts)
- [`CHANGELOG.md`](../../CHANGELOG.de.md) (Eintrag 4.8.14)
