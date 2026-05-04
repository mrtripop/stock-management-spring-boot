# Deployment Guide

## Docker Build

### Multi-stage Dockerfile

The Dockerfile uses a two-stage build:

1. **Build stage**: `maven:3.9.5-amazoncorretto-17` - Compiles and packages
2. **Runtime stage**: `amazoncorretto:17-alpine` - Minimal runtime image

The OpenTelemetry Java agent is bundled in the final image.

```bash
docker build -t stock-management:latest .
```

## Docker Compose Infrastructure

### Services

| Service | Image | Port | Purpose |
|---------|-------|------|---------|
| postgres-db | postgres:14.6 | 5432 | Primary database |
| redis | redis:7.2.4 | 6379 | Cache layer |
| redisinsight | redislabs/redisinsight | 5540 | Redis GUI |
| collector | otel/opentelemetry-collector:0.88.0 | 4317 | Telemetry collector |
| elasticsearch | elasticsearch:8.12.0 | 9200 | Log storage |
| logstash | logstash:8.12.0 | 5044 | Log pipeline |
| kibana | kibana:8.12.0 | 5601 | Log visualization |
| filebeat | filebeat:8.12.0 | - | Log shipper |

### Quick Start

```bash
# Start database and cache only
docker compose up -d postgres-db redis

# Start full observability stack
docker compose up -d
```

### Volumes

Named volumes persist data across restarts:
- `postgres-db` - PostgreSQL data
- `elasticsearch` - Elasticsearch indices
- `redisinsight` - RedisInsight configuration

## Environment Configuration

### Required Environment Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `DATASOURCE_URL` | PostgreSQL JDBC URL | `jdbc:postgresql://localhost:5432/inventory` |
| `DATASOURCE_USERNAME` | Database username | `postgres` |
| `DATASOURCE_PASSWORD` | Database password | `postgres` |
| `REDIS_URL` | Redis connection URL | `redis://localhost:6379` |
| `REDIS_USERNAME` | Redis username | `default` |
| `REDIS_PASSWORD` | Redis password | `<password>` |
| `REDIS_TIMEOUT` | Redis timeout (ms) | `2000` |
| `CACHE_REDIS_TTL` | Cache TTL (ms) | `600000` |
| `JPA_PROPERTIES_HIBERNATE_FORMAT_SQL` | Format SQL in logs | `true`/`false` |
| `JPA_PROPERTIES_SHOW_SQL` | Show SQL in logs | `true`/`false` |
| `LOGGING_LEVEL_COM_MRTRIPOP` | App log level | `DEBUG`/`INFO` |

### Environment Profiles

| Profile | Config File | Database | Cache |
|---------|-------------|----------|-------|
| default | application.yml | PostgreSQL (env vars) | Redis |
| test | application-test.yml | H2 in-memory | None |

### Deployment URLs

| Environment | URL |
|-------------|-----|
| Local | http://localhost:8080 |
| Dev | http://dev-inventory.mrtripop-int.com |
| Staging | http://inventory.mrtripop-int.com |

## Observability

### OpenTelemetry

- Java agent bundled in Docker image (`opentelemetry-javaagent.jar`)
- Collector: OTLP endpoint at `localhost:4317`
- Config: `otel-config.yaml`

### ELK Stack

- **Filebeat**: Ships Docker container logs to Logstash
- **Logstash**: Processes and forwards to Elasticsearch
- **Elasticsearch**: Stores log data (single-node, security disabled for dev)
- **Kibana**: Log visualization at http://localhost:5601

### Logging

- **Framework**: Log4j2 (spring-boot-starter-log4j2, excludes default Logback)
- **AOP Logging**: `GlobalAspect` logs method entry/exit at DEBUG level
- **Config**: `src/main/resources/log4j2-spring.xml`

## Security Notes

- Spring Security is configured but currently `permitAll()` (all endpoints open)
- Default credentials in config: `user/password` (for development only)
- Role hierarchy planned: EMPLOYEE -> MANAGER -> ADMIN
- Never commit secrets - use environment variables
