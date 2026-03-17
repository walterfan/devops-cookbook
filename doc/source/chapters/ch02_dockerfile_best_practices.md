# Dockerfile 最佳实践

```{mermaid}
flowchart LR
    subgraph "Multi-stage Build"
        A[Stage 1: Build] -->|COPY binary| B[Stage 2: Runtime]
    end
    B --> C[Minimal Image<br/>scratch / distroless / alpine]
    style A fill:#f9f,stroke:#333
    style B fill:#9f9,stroke:#333
    style C fill:#ff9,stroke:#333
```

## Dockerfile Instructions Reference

| Instruction | Purpose | Example |
|-------------|---------|---------|
| `FROM` | Base image | `FROM golang:1.22-alpine AS builder` |
| `RUN` | Execute command | `RUN apt-get update && apt-get install -y curl` |
| `COPY` | Copy files from build context | `COPY . /app` |
| `ADD` | Copy + extract archives / fetch URLs | `ADD app.tar.gz /app` |
| `WORKDIR` | Set working directory | `WORKDIR /app` |
| `ENV` | Set environment variable | `ENV APP_PORT=8080` |
| `ARG` | Build-time variable | `ARG VERSION=1.0` |
| `EXPOSE` | Document port | `EXPOSE 8080` |
| `VOLUME` | Create mount point | `VOLUME /data` |
| `USER` | Set runtime user | `USER 1001` |
| `CMD` | Default command (overridable) | `CMD ["./app"]` |
| `ENTRYPOINT` | Fixed command | `ENTRYPOINT ["./app"]` |
| `HEALTHCHECK` | Container health check | `HEALTHCHECK CMD curl -f http://localhost:8080/health` |

## Multi-stage Build

Multi-stage builds dramatically reduce image size by separating build dependencies from runtime.

### Go Application

```dockerfile
# Stage 1: Build
FROM golang:1.22-alpine AS builder
WORKDIR /app
COPY go.mod go.sum ./
RUN go mod download
COPY . .
RUN CGO_ENABLED=0 GOOS=linux go build -ldflags="-s -w" -o /app/server ./cmd/server

# Stage 2: Runtime
FROM scratch
COPY --from=builder /app/server /server
COPY --from=builder /etc/ssl/certs/ca-certificates.crt /etc/ssl/certs/
EXPOSE 8080
ENTRYPOINT ["/server"]
```

### Python Application

```dockerfile
# Stage 1: Build dependencies
FROM python:3.12-slim AS builder
WORKDIR /app
RUN pip install --no-cache-dir poetry
COPY pyproject.toml poetry.lock ./
RUN poetry export -f requirements.txt -o requirements.txt --without-hashes
RUN pip install --no-cache-dir --prefix=/install -r requirements.txt

# Stage 2: Runtime
FROM python:3.12-slim
WORKDIR /app
COPY --from=builder /install /usr/local
COPY . .
RUN useradd -r -s /bin/false appuser
USER appuser
EXPOSE 8000
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]
```

### Java Application (Spring Boot)

```dockerfile
# Stage 1: Build
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app
COPY gradle/ gradle/
COPY gradlew build.gradle.kts settings.gradle.kts ./
RUN ./gradlew dependencies --no-daemon
COPY src/ src/
RUN ./gradlew bootJar --no-daemon

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S app && adduser -S app -G app
COPY --from=builder /app/build/libs/*.jar app.jar
USER app
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=3s \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
```

## Layer Caching Optimization

```dockerfile
# BAD: Invalidates cache on any code change
COPY . /app
RUN pip install -r requirements.txt

# GOOD: Dependencies cached separately
COPY requirements.txt /app/
RUN pip install -r requirements.txt
COPY . /app
```

## .dockerignore

Always create a `.dockerignore` to exclude unnecessary files:

```text
.git
.gitignore
.env
*.md
LICENSE
docker-compose*.yml
Dockerfile*
__pycache__
*.pyc
node_modules
.vscode
.idea
build/
dist/
target/
```

## Security Best Practices

```dockerfile
# 1. Use specific image tags (never :latest in production)
FROM python:3.12.3-slim

# 2. Run as non-root user
RUN groupadd -r app && useradd -r -g app app
USER app

# 3. Read-only filesystem where possible
# (set at runtime: docker run --read-only)

# 4. No unnecessary packages
RUN apt-get update && apt-get install -y --no-install-recommends \
    curl \
    && rm -rf /var/lib/apt/lists/*

# 5. Use COPY instead of ADD (unless you need tar extraction)
COPY app.py /app/

# 6. Scan images
# docker scout cves myimage:1.0
# trivy image myimage:1.0
```

## Size Reduction Tips

| Technique | Impact |
|-----------|--------|
| Alpine base images | ~5MB vs ~120MB for Debian |
| Multi-stage builds | Remove build tools from final image |
| `--no-install-recommends` | Skip optional apt packages |
| `rm -rf /var/lib/apt/lists/*` | Remove apt cache |
| `--no-cache-dir` for pip | Skip pip cache |
| Combine RUN commands | Fewer layers |
| `.dockerignore` | Smaller build context |

## HEALTHCHECK

```dockerfile
# HTTP health check
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD curl -f http://localhost:8080/health || exit 1

# TCP health check (no curl needed)
HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
  CMD nc -z localhost 8080 || exit 1
```
