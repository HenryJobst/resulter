# ADR 0019: Betriebsmetriken mit Micrometer, Prometheus und Grafana

> **Deutsch** · [English](0019-observability-with-prometheus-and-grafana.md)

- **Status:** akzeptiert
- **Datum:** 2025-02-16
- **Kontext:** Verzeichnisse `prometheus/` und `grafana/` seit 2025-02-16; `micrometer-registry-prometheus` im Backend seit 2025-01-14; Token-Dokumentation vom 2026-05-06

## Kontext

Für den Betrieb sollen Metriken des Backends gesammelt und angezeigt werden. Die
Spring-Boot-Actuator-Endpunkte sind sensibel; die Konfiguration schaltet standardmäßig
alle ab (`management.endpoints.access.default=none`).

## Entscheidung

1. **Micrometer mit Prometheus-Registry im Backend.** `spring-boot-starter-actuator` und
   `micrometer-registry-prometheus` liefern die Metriken. In `prod` sind nur die
   Endpunkte `health` und `prometheus` freigegeben und lesbar
   (`management.endpoints.web.exposure.include=health,prometheus`);
   Gesundheitsdetails nur für berechtigte Benutzer.

2. **Eigene Sicherheitskette für den Scrape-Endpunkt.** Der Endpunkt
   `/actuator/prometheus` hat eine eigene, früh geprüfte Filterkette (`@Order(1)`) und wird
   über `PrometheusApiTokenFilter` mit einem statischen Bearer-Token geschützt (Vergleich
   mit `MessageDigest.isEqual`). Das Token hat keine Ablaufzeit und wird manuell rotiert;
   Erzeugung und Rotation stehen in `doc/prometheus-token.md`. Der Wert kommt aus der
   Umgebungsvariablen `PROMETHEUS_API_TOKEN` und gehört nicht ins Repository.

3. **Prometheus und Grafana als eigene Dienste.** `prometheus/prometheus.yml` (Entwicklung)
   und `prometheus/prometheus-prod.yml` (Produktion; Job `resulter-prod`, Pfad
   `/actuator/prometheus`, Intervall 5 s, Ziel `localhost:8080`) konfigurieren das
   Sammeln. `startPrometheus.sh` und `startGrafana.sh` starten `prom/prometheus` und
   `grafana/grafana-oss` lokal als Docker-Container.

4. **Bausteine zur SQL-Analyse sind getrennt.** `p6spy` und `datasource-proxy` sind für
   die Abfrageprotokollierung eingetragen und gehören nicht zu den Betriebsmetriken
   ([ADR 0002](0002-spring-data-jdbc-instead-of-jpa.de.md)).

Das Repository enthält keine Grafana-Dashboards und keine Alarmregeln; ob sie auf dem
Server liegen, ist nicht belegt.

## Konsequenzen

**Positiv**

- Standardmetriken von Spring Boot und JVM stehen ohne Eigenentwicklung bereit.
- Der Scrape-Endpunkt ist auch ohne OAuth2-Sitzung für Prometheus abrufbar, aber nicht
  öffentlich.

**Negativ / Kosten**

- Ein langlebiges statisches Token muss geheim gehalten und von Hand rotiert werden.
- Prometheus und Grafana laufen außerhalb dieses Repositorys; Konfigurationen für den
  Betrieb liegen verteilt (`prometheus/` hier, der Rest in der Betriebsumgebung).
- `scheme: http` und `localhost:8080` in `prometheus-prod.yml` setzen voraus, dass
  Prometheus auf demselben Host läuft wie das Backend.
- Keine versionierten Dashboards.

## Alternativen (nicht dokumentiert, naheliegend)

- **Ein gehostetes Monitoring** statt eigenem Prometheus/Grafana.
- **OAuth2-Client-Credentials** für den Scraper statt statischem Token.

## Verwandte Dokumente

- [`prometheus.yml`](../../prometheus/prometheus.yml)
- [`prometheus-prod.yml`](../../prometheus/prometheus-prod.yml)
- [`startPrometheus.sh`](../../prometheus/startPrometheus.sh)
- [`startGrafana.sh`](../../grafana/startGrafana.sh)
- [`prometheus-token.md`](../../doc/prometheus-token.de.md)
- [`PrometheusApiTokenFilter.java`](../../backend/src/main/java/de/jobst/resulter/springapp/config/PrometheusApiTokenFilter.java)
- [`application-prod.properties`](../../backend/src/main/resources/application-prod.properties)
