# ADR 0002: Spring Data JDBC statt JPA

> **Deutsch** · [English](0002-spring-data-jdbc-instead-of-jpa.md)

- **Status:** akzeptiert
- **Datum:** 2024-01-14
- **Kontext:** Umstellung der Persistenz im Backend (Commit `04ed998f`, „major refactoring of jpa entities to spring data jdbc entities“)

## Kontext

Das Backend startete am 2023-12-02 mit `spring-boot-starter-data-jpa`; die Adapter
hießen `EventRepositoryDataJpaAdapter` und lagen im Paket `adapter/driven/jpa`. Am
2024-01-14 wurden die Entitäten in einem Commit auf Spring Data JDBC umgestellt
(Paket `adapter/driven/jdbc`, Klassen `*Dbo`, `*JdbcRepository`,
`*RepositoryDataJdbcAdapter`). Die Commit-Nachricht nennt keinen Grund. Die Begründung
stammt vom Projektinhaber: Spring Data JDBC ist leichtgewichtiger als JPA.

## Entscheidung

1. **Spring Data JDBC ist die Persistenztechnik.** `spring-boot-starter-data-jdbc` ist
   die einzige Spring-Data-Abhängigkeit; `spring-boot-starter-data-jpa` wurde entfernt.

2. **Persistenzmodell getrennt vom Domänenmodell.** Pro Aggregat gibt es ein `*Dbo`,
   ein `*JdbcRepository` (Spring Data) und einen Adapter
   `*RepositoryDataJdbcAdapter`, der den Port aus `application.port`
   implementiert (siehe [ADR 0001](0001-hexagonal-architecture-backend.de.md)).
   Beziehungen zwischen Aggregaten laufen über IDs; `DboResolver` und
   `DboResolvers` lösen sie beim Mapping auf.

3. **Eigene Abfragen für Auswertungen.** Wo das Standard-Repository nicht reicht,
   gibt es Custom-Repositories (`EventJdbcRepositoryCustom`,
   `CupScoreListJdbcCustomRepository` und weitere) sowie `DashboardJdbcRepository`.

4. **Abfrageverhalten wird getestet.** `NPlusOneReviewIT` untersucht Zugriffe auf
   N+1-Muster; `datasource-proxy` und `p6spy` sind als Abhängigkeiten eingetragen.

Begründung der Umstellung (Projektinhaber): Spring Data JDBC ist leichtgewichtiger als JPA.

## Konsequenzen

**Positiv**

- Kein Persistenzkontext, keine Lazy-Loading-Effekte; was geladen wird, ist
  im Adapter sichtbar.
- Das Schema wird ausschließlich über Liquibase gepflegt
  ([ADR 0003](0003-postgresql-and-liquibase.de.md)).

**Negativ / Kosten**

- Das Mapping zwischen Dbo und Domäne wird von Hand geschrieben.
- Komplexe Abfragen und Aggregat-Beziehungen verlangen eigene SQL- oder
  Custom-Repository-Implementierungen.
- `.cursorrules` beschreibt noch `JpaRepository` und JPQL und ist damit gegenüber
  dem Code veraltet.

## Alternativen (nicht dokumentiert, naheliegend)

- **JPA mit Hibernate** (Ausgangszustand bis 2024-01-14): weniger Handarbeit beim
  Mapping, dafür schwergewichtiger (Persistenzkontext, Lazy Loading).
- **Reines JdbcTemplate/jOOQ:** mehr Kontrolle, aber keine Repository-Grundfunktionen.

## Verwandte Dokumente

- [`pom.xml` des Backends](../../backend/pom.xml)
- [`adapter/driven/jdbc`](../../backend/src/main/java/de/jobst/resulter/adapter/driven/jdbc)
- [`EventRepositoryDataJdbcAdapter.java`](../../backend/src/main/java/de/jobst/resulter/adapter/driven/jdbc/EventRepositoryDataJdbcAdapter.java)
- [`NPlusOneReviewIT.java`](../../backend/src/test/java/de/jobst/resulter/adapter/driver/web/NPlusOneReviewIT.java)
- [`.cursorrules`](../../.cursorrules)
- [README, Abschnitt Technologies](../../README.md)
