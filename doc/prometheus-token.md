# Managing the Prometheus API Token

> [Deutsch](prometheus-token.de.md) · **English**

The Prometheus scraper authenticates at the backend's `/actuator/prometheus` endpoint
with a static bearer token. This document describes how to create, configure and
rotate the token.

## How it works

On every request to `/actuator/prometheus`, the `PrometheusApiTokenFilter` checks whether the
HTTP header `Authorization: Bearer <token>` matches the value configured on the server
(constant-time comparison via `MessageDigest.isEqual`). The token has no
expiry and must be rotated manually.

```
Prometheus → GET /actuator/prometheus
            Authorization: Bearer <token>   ←  from the prometheus_api_token file
                        ↓
            PrometheusApiTokenFilter
                        ↓
            Comparison with $PROMETHEUS_API_TOKEN
                        ↓
            200 OK  /  401 Unauthorized
```

## Creating the token

Generate a cryptographically secure token:

```bash
openssl rand -base64 32
```

Example output:

```
<44 base64 characters>
```

Enter this value in the following two places.

## Configuration

### 1. Backend: environment variable

Set the environment variable `PROMETHEUS_API_TOKEN`. The value is read into the Spring
configuration via `application.properties` (`security.prometheus.api-token=${PROMETHEUS_API_TOKEN}`).

**`.env` file or Docker Compose:**

```env
PROMETHEUS_API_TOKEN=<generated-token>
```

**Systemd / Shell:**

```bash
export PROMETHEUS_API_TOKEN=<generated-token>
```

### 2. Prometheus: token file

Fill the file `prometheus/prometheus_api_token` with **exactly the same value**,
without a trailing newline. Prometheus reads it via `bearer_token_file`
and sends the content as the `Authorization: Bearer` header.

```bash
printf 'HIER_TOKEN_EINTRAGEN' > prometheus/prometheus_api_token
```

> **Note:** Use `printf` instead of `echo` so that no newline is appended.
> A trailing newline would make the token check fail.

## Rotating the token

1. Generate a new token:
   ```bash
   openssl rand -base64 32
   ```

2. Update `prometheus/prometheus_api_token`. Prometheus re-reads the file on
   every scrape, no restart needed.

3. Set `PROMETHEUS_API_TOKEN` in the backend environment and restart the backend.

> **Mind the order:** Update the Prometheus file first, then restart the backend.
> Otherwise Prometheus briefly fails with `401 Unauthorized` during the restart.

## Troubleshooting

| HTTP status | Cause | Solution |
|-------------|---------|--------|
| `500 Internal Server Error` | `PROMETHEUS_API_TOKEN` not set or empty | Check the environment variable and restart the backend |
| `403 Forbidden` | No `Authorization` header in the request | Check the `prometheus_api_token` file and reload Prometheus |
| `401 Unauthorized` | Token does not match | Check that both places have the same value |

Check the Prometheus logs:

```bash
docker logs <prometheus-container> 2>&1 | grep "prometheus"
```

Check the backend logs:

```bash
docker logs <backend-container> 2>&1 | grep -i "prometheus\|actuator"
```

## Security notes

- Never commit the token to version control. Add the file
  `prometheus/prometheus_api_token` to `.gitignore`.
- For production environments, use a secret management system (e.g. Vault, Kubernetes
  Secrets, Docker Secrets) instead of a plain text file.
- Rotate the token regularly, at least when staff change or after
  security incidents.
