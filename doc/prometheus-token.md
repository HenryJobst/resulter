# Prometheus API Token verwalten

Der Prometheus-Scraper authentifiziert sich am `/actuator/prometheus`-Endpunkt des Backends
mit einem statischen Bearer-Token. Dieses Dokument beschreibt, wie das Token erstellt,
konfiguriert und rotiert wird.

## Funktionsweise

Der `PrometheusApiTokenFilter` prüft bei jedem Request auf `/actuator/prometheus`, ob der
HTTP-Header `Authorization: Bearer <token>` mit dem serverseitig konfigurierten Wert
übereinstimmt (zeitkonstanter Vergleich via `MessageDigest.isEqual`). Das Token hat keine
Ablaufzeit und muss manuell rotiert werden.

```
Prometheus → GET /actuator/prometheus
            Authorization: Bearer <token>   ←  aus prometheus_api_token-Datei
                        ↓
            PrometheusApiTokenFilter
                        ↓
            Vergleich mit $PROMETHEUS_API_TOKEN
                        ↓
            200 OK  /  401 Unauthorized
```

## Token erstellen

Ein kryptographisch sicheres Token generieren:

```bash
openssl rand -base64 32
```

Beispielausgabe:

```
0icVawRjNLSzlGdQRsp3MbFdeM/7tCnladoa8j1cLRw=
```

Diesen Wert in den folgenden zwei Stellen eintragen.

## Konfiguration

### 1. Backend — Umgebungsvariable

Die Umgebungsvariable `PROMETHEUS_API_TOKEN` setzen. Der Wert wird über
`application.properties` (`security.prometheus.api-token=${PROMETHEUS_API_TOKEN}`)
in die Spring-Konfiguration eingelesen.

**`.env`-Datei oder Docker Compose:**

```env
PROMETHEUS_API_TOKEN=0icVawRjNLSzlGdQRsp3MbFdeM/7tCnladoa8j1cLRw=
```

**Systemd / Shell:**

```bash
export PROMETHEUS_API_TOKEN=0icVawRjNLSzlGdQRsp3MbFdeM/7tCnladoa8j1cLRw=
```

### 2. Prometheus — Token-Datei

Die Datei `prometheus/prometheus_api_token` mit **exakt demselben Wert** befüllen —
ohne abschließenden Zeilenumbruch. Prometheus liest sie via `bearer_token_file`
und sendet den Inhalt als `Authorization: Bearer`-Header.

```bash
printf 'HIER_TOKEN_EINTRAGEN' > prometheus/prometheus_api_token
```

> **Hinweis:** `printf` statt `echo` verwenden, damit kein Zeilenumbruch angehängt wird.
> Ein Zeilenumbruch am Ende würde die Token-Prüfung fehlschlagen lassen.

## Token rotieren

1. Neues Token generieren:
   ```bash
   openssl rand -base64 32
   ```

2. `prometheus/prometheus_api_token` aktualisieren — Prometheus liest die Datei bei
   jedem Scrape neu, kein Neustart nötig.

3. `PROMETHEUS_API_TOKEN` in der Backend-Umgebung setzen und das Backend neu starten.

> **Reihenfolge beachten:** Erst die Prometheus-Datei aktualisieren, dann das Backend
> neu starten. Andernfalls schlägt Prometheus während des Neustarts kurzzeitig mit
> `401 Unauthorized` fehl.

## Fehlerdiagnose

| HTTP-Status | Ursache | Lösung |
|-------------|---------|--------|
| `500 Internal Server Error` | `PROMETHEUS_API_TOKEN` nicht gesetzt oder leer | Umgebungsvariable prüfen und Backend neu starten |
| `403 Forbidden` | Kein `Authorization`-Header im Request | `prometheus_api_token`-Datei prüfen und Prometheus neu laden |
| `401 Unauthorized` | Token stimmt nicht überein | Beide Stellen auf denselben Wert prüfen |

Prometheus-Logs prüfen:

```bash
docker logs <prometheus-container> 2>&1 | grep "prometheus"
```

Backend-Logs prüfen:

```bash
docker logs <backend-container> 2>&1 | grep -i "prometheus\|actuator"
```

## Sicherheitshinweise

- Das Token niemals in die Versionskontrolle einchecken. Die Datei
  `prometheus/prometheus_api_token` ist in `.gitignore` einzutragen.
- Für Produktionsumgebungen ein Secret-Management-System (z. B. Vault, Kubernetes
  Secrets, Docker Secrets) verwenden statt einer Klartextdatei.
- Das Token regelmäßig rotieren — mindestens bei Mitarbeiterwechsel oder
  Sicherheitsvorfällen.
