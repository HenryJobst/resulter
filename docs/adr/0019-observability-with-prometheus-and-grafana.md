# ADR 0019: Operational metrics with Micrometer, Prometheus and Grafana

> [Deutsch](0019-observability-with-prometheus-and-grafana.de.md) · **English**

- **Status:** accepted
- **Date:** 2025-02-16
- **Context:** Directories `prometheus/` and `grafana/` since 2025-02-16; `micrometer-registry-prometheus` in the backend since 2025-01-14; token documentation of 2026-05-06

## Context

For operations, metrics of the backend should be collected and displayed. The
Spring Boot Actuator endpoints are sensitive; the configuration disables
all of them by default (`management.endpoints.access.default=none`).

## Decision

1. **Micrometer with Prometheus registry in the backend.** `spring-boot-starter-actuator` and
   `micrometer-registry-prometheus` provide the metrics. In `prod` only the
   endpoints `health` and `prometheus` are exposed and readable
   (`management.endpoints.web.exposure.include=health,prometheus`);
   health details only for authorized users.

2. **Separate security chain for the scrape endpoint.** The endpoint
   `/actuator/prometheus` has its own, early-evaluated filter chain (`@Order(1)`) and is
   protected by `PrometheusApiTokenFilter` with a static bearer token (comparison
   with `MessageDigest.isEqual`). The token has no expiry and is rotated manually;
   creation and rotation are described in `doc/prometheus-token.md`. The value comes from the
   environment variable `PROMETHEUS_API_TOKEN` and does not belong in the repository.

3. **Prometheus and Grafana as separate services.** `prometheus/prometheus.yml` (development)
   and `prometheus/prometheus-prod.yml` (production; job `resulter-prod`, path
   `/actuator/prometheus`, interval 5 s, target `localhost:8080`) configure
   the collection. `startPrometheus.sh` and `startGrafana.sh` start `prom/prometheus` and
   `grafana/grafana-oss` locally as Docker containers.

4. **Building blocks for SQL analysis are separate.** `p6spy` and `datasource-proxy` are
   registered for query logging and are not part of the operational metrics
   ([ADR 0002](0002-spring-data-jdbc-instead-of-jpa.md)).

The repository contains no Grafana dashboards and no alert rules; whether they exist on the
server is not documented.

## Consequences

**Positive**

- Standard metrics of Spring Boot and the JVM are available without custom development.
- The scrape endpoint can be called by Prometheus without an OAuth2 session, but is not
  public.

**Negative / costs**

- A long-lived static token has to be kept secret and rotated by hand.
- Prometheus and Grafana run outside this repository; configurations for
  operations are spread out (`prometheus/` here, the rest in the operating environment).
- `scheme: http` and `localhost:8080` in `prometheus-prod.yml` assume that
  Prometheus runs on the same host as the backend.
- No versioned dashboards.

## Alternatives (not documented, obvious candidates)

- **A hosted monitoring service** instead of a self-run Prometheus/Grafana.
- **OAuth2 client credentials** for the scraper instead of a static token.

## Related documents

- [`prometheus.yml`](../../prometheus/prometheus.yml)
- [`prometheus-prod.yml`](../../prometheus/prometheus-prod.yml)
- [`startPrometheus.sh`](../../prometheus/startPrometheus.sh)
- [`startGrafana.sh`](../../grafana/startGrafana.sh)
- [`prometheus-token.md`](../../doc/prometheus-token.md)
- [`PrometheusApiTokenFilter.java`](../../backend/src/main/java/de/jobst/resulter/springapp/config/PrometheusApiTokenFilter.java)
- [`application-prod.properties`](../../backend/src/main/resources/application-prod.properties)
