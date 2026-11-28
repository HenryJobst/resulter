# ADR 0022: Unit-Tests in der CI

> **Deutsch** · [English](0022-unit-tests-in-ci.md)

- **Status:** akzeptiert
- **Datum:** 2026-11-28
- **Kontext:** Die Architekturdokumentation ([`architecture.de.md`](../architecture.de.md)) zeigte, dass die CI keine Anwendungstests ausführte. Dieser ADR löst Punkt 5 von [ADR 0006](0006-test-strategy.de.md) ab.

## Kontext

[ADR 0006](0006-test-strategy.de.md) hielt fest, dass `build.yml` nur `mvn compile` und `pnpm build`
ausführte. Tests liefen lokal, über den Pre-Commit-Hook und von Hand. Ein lokaler Lauf am 2026-11-28
zeigte, dass die Unit-Tests schnell sind und weder Datenbank noch Docker brauchen: 742
Frontend-Tests in etwa 35 Sekunden und 1740 Backend-Tests mit einem Fehler. Der Fehler kam aus
`ResultListServiceTest`, der den Medienpfad über `Dotenv.load()` lädt und in `backend/` keine `.env`
fand. Die Begründung, warum die CI keine Tests ausführt, ist im Repository nicht dokumentiert.

## Entscheidung

1. **Die CI führt die Unit-Tests von Backend und Frontend aus.** `build.yml` hat einen Schritt
   `Run backend tests` (`mvn --batch-mode test`) und einen Schritt `Run frontend unit tests`
   (`pnpm test:unit`). Ein fehlschlagender Test lässt den Workflow fehlschlagen.

2. **Der Testschritt ist vom Build-Schritt getrennt.** Der Maven-Build behält seine Wiederholungsschleife
   für vorübergehende Repository-Fehler; die Tests laufen danach einmal, damit ein fehlschlagender
   Test nicht wiederholt wird.

3. **Der Backend-Schritt legt eine `.env` an.** Er kopiert vor den Tests `.env.example` nach
   `backend/.env`, weil `ResultListServiceTest` `RESULTER_MEDIA_FILE_PATH` braucht. Git ignoriert die
   Datei, sie wird nicht committet.

4. **End-to-End-Tests bleiben außerhalb der CI.** Sie brauchen Keycloak, Testcontainers und drei
   Browser; ob ein eigener Job entsteht, bleibt offen.

5. **Der Image-Workflow bleibt unverändert.** `build-paketo.yml` kompiliert und baut weiterhin ohne
   Tests und baut die Images mit `-Dmaven.test.skip=true`.

## Konsequenzen

**Positiv**

- Ein fehlschlagender Unit-Test ist bei jedem Push sichtbar, nicht nur in lokalen Läufen.
- Die vorhandene Testbasis (über 1700 Backend- und 700 Frontend-Tests) schützt den Hauptzweig.

**Negativ / Kosten**

- Der CI-Lauf wird länger; die Backend-Dauer auf einem Runner wurde nicht gemessen.
- Tests, die von der lokalen Umgebung abhängen, etwa von der `.env`-Datei, fallen in der CI auf und
  müssen dort behoben oder vorbereitet werden.
- Die Workflow-Änderung wurde beim Schreiben dieses ADR nicht auf GitHub ausgeführt.
- Die Spring- und Datenbanktests (`*IT`, `@SpringBootTest`) laufen als Teil von `mvn test`; ob sie
  auf einem Runner Docker brauchen, wurde nicht einzeln geprüft.

## Alternativen (nicht dokumentiert, naheliegende Kandidaten)

- **Unit-Tests nur auf Release-Branches:** kürzere normale Läufe, aber Fehler fallen spät auf.
- **Ein eigener Test-Job** neben dem Build-Job: mehr Parallelität, aber die Abhängigkeiten werden
  doppelt installiert.
- **`mvn verify` und die `*IT`-Tests einbeziehen:** breiter, muss aber vorher auf Docker und Laufzeit
  geprüft werden.

## Verwandte Dokumente

- [ADR 0006](0006-test-strategy.de.md)
- [`build.yml`](../../.github/workflows/build.yml)
- [`build-paketo.yml`](../../.github/workflows/build-paketo.yml)
- [`.env.example`](../../.env.example)
- [`ResultListServiceTest.java`](../../backend/src/test/java/de/jobst/resulter/application/ResultListServiceTest.java)
- [`vitest.config.ts`](../../frontend/vitest.config.ts)
