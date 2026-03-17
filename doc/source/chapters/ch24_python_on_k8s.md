# Python 应用上 K8s

```{mermaid}
flowchart TB
    subgraph Build
        Code[Python Source] --> Docker[Multi-stage Build]
        Docker --> Image[slim Image<br/>~150MB]
    end
    subgraph K8s["Kubernetes"]
        Deploy[Deployment<br/>Uvicorn Workers] --> Pod1[Pod]
        Deploy --> Pod2[Pod]
        CeleryDeploy[Deployment<br/>Celery Workers] --> CW1[Worker Pod]
        CeleryDeploy --> CW2[Worker Pod]
        Redis[(Redis)] --> CW1
        Redis --> CW2
        Svc[Service] --> Pod1
        Svc --> Pod2
    end
    Image --> Deploy
    Image --> CeleryDeploy
```

## FastAPI Application

```python
# app/main.py
from contextlib import asynccontextmanager
from fastapi import FastAPI
from prometheus_client import Counter, Histogram, generate_latest, CONTENT_TYPE_LATEST
from starlette.requests import Request
from starlette.responses import Response
import time
import logging
import json

# Structured logging
logging.basicConfig(
    format='{"timestamp":"%(asctime)s","level":"%(levelname)s","message":"%(message)s"}',
    level=logging.INFO,
)
logger = logging.getLogger(__name__)

# Prometheus metrics
REQUEST_COUNT = Counter("http_requests_total", "Total requests", ["method", "path", "status"])
REQUEST_LATENCY = Histogram("http_request_duration_seconds", "Request latency", ["method", "path"])

@asynccontextmanager
async def lifespan(app: FastAPI):
    logger.info("Starting application")
    yield
    logger.info("Shutting down application")

app = FastAPI(title="Python K8s App", lifespan=lifespan)

@app.middleware("http")
async def metrics_middleware(request: Request, call_next):
    start = time.time()
    response = await call_next(request)
    duration = time.time() - start
    REQUEST_COUNT.labels(request.method, request.url.path, response.status_code).inc()
    REQUEST_LATENCY.labels(request.method, request.url.path).observe(duration)
    return response

@app.get("/healthz")
async def healthz():
    return {"status": "ok"}

@app.get("/readyz")
async def readyz():
    return {"status": "ready"}

@app.get("/metrics")
async def metrics():
    return Response(generate_latest(), media_type=CONTENT_TYPE_LATEST)

@app.get("/api/hello")
async def hello():
    return {"message": "Hello from Python on K8s!"}
```

## Dockerfile (Multi-stage)

```dockerfile
# Stage 1: Build dependencies
FROM python:3.12-slim AS builder
WORKDIR /app

# Install build tools
RUN apt-get update && apt-get install -y --no-install-recommends gcc && \
    rm -rf /var/lib/apt/lists/*

# Install dependencies
COPY requirements.txt .
RUN pip install --no-cache-dir --prefix=/install -r requirements.txt

# Stage 2: Runtime
FROM python:3.12-slim
WORKDIR /app

# Copy installed packages
COPY --from=builder /install /usr/local

# Copy application
COPY . .

# Non-root user
RUN useradd -r -s /bin/false appuser
USER appuser

EXPOSE 8000

# Uvicorn with multiple workers
CMD ["uvicorn", "main:app", \
     "--host", "0.0.0.0", \
     "--port", "8000", \
     "--workers", "4", \
     "--access-log", \
     "--log-level", "info"]
```

### requirements.txt

```text
fastapi>=0.110
uvicorn[standard]>=0.27
prometheus-client>=0.20
sqlalchemy[asyncio]>=2.0
asyncpg>=0.29
celery[redis]>=5.3
redis>=5.0
httpx>=0.27
```

## Kubernetes Manifests

### Deployment (Web)

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: python-api
spec:
  replicas: 3
  selector:
    matchLabels:
      app: python-api
  template:
    metadata:
      labels:
        app: python-api
    spec:
      containers:
        - name: api
          image: registry.example.com/python-api:1.0.0
          ports:
            - containerPort: 8000
          env:
            - name: DATABASE_URL
              valueFrom:
                secretKeyRef:
                  name: app-secret
                  key: database-url
            - name: REDIS_URL
              value: "redis://redis:6379/0"
          resources:
            requests:
              cpu: "250m"
              memory: "256Mi"
            limits:
              cpu: "1"
              memory: "512Mi"
          livenessProbe:
            httpGet:
              path: /healthz
              port: 8000
            initialDelaySeconds: 10
            periodSeconds: 15
          readinessProbe:
            httpGet:
              path: /readyz
              port: 8000
            initialDelaySeconds: 5
            periodSeconds: 10
---
apiVersion: v1
kind: Service
metadata:
  name: python-api
spec:
  selector:
    app: python-api
  ports:
    - port: 80
      targetPort: 8000
---
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: python-api
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: python-api
  minReplicas: 2
  maxReplicas: 10
  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: 70
```

### Celery Worker Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: celery-worker
spec:
  replicas: 2
  selector:
    matchLabels:
      app: celery-worker
  template:
    metadata:
      labels:
        app: celery-worker
    spec:
      containers:
        - name: worker
          image: registry.example.com/python-api:1.0.0
          command: ["celery", "-A", "tasks", "worker",
                    "--loglevel=info",
                    "--concurrency=4",
                    "--max-tasks-per-child=1000"]
          env:
            - name: CELERY_BROKER_URL
              value: "redis://redis:6379/0"
            - name: CELERY_RESULT_BACKEND
              value: "redis://redis:6379/1"
          resources:
            requests:
              cpu: "500m"
              memory: "512Mi"
            limits:
              cpu: "1"
              memory: "1Gi"
          livenessProbe:
            exec:
              command: ["celery", "-A", "tasks", "inspect", "ping"]
            initialDelaySeconds: 30
            periodSeconds: 60
---
# Celery Beat (scheduler) - single replica
apiVersion: apps/v1
kind: Deployment
metadata:
  name: celery-beat
spec:
  replicas: 1
  selector:
    matchLabels:
      app: celery-beat
  template:
    metadata:
      labels:
        app: celery-beat
    spec:
      containers:
        - name: beat
          image: registry.example.com/python-api:1.0.0
          command: ["celery", "-A", "tasks", "beat", "--loglevel=info"]
          env:
            - name: CELERY_BROKER_URL
              value: "redis://redis:6379/0"
          resources:
            requests:
              cpu: "100m"
              memory: "128Mi"
```

## Docker Compose (Local Dev)

```yaml
services:
  app:
    build: .
    ports:
      - "8000:8000"
    volumes:
      - .:/app    # hot reload
    command: uvicorn main:app --host 0.0.0.0 --port 8000 --reload
    environment:
      - DATABASE_URL=postgresql+asyncpg://app:pass@db:5432/mydb
      - REDIS_URL=redis://redis:6379/0
    depends_on:
      db:
        condition: service_healthy
      redis:
        condition: service_started

  worker:
    build: .
    command: celery -A tasks worker --loglevel=info
    environment:
      - CELERY_BROKER_URL=redis://redis:6379/0
    depends_on:
      - redis

  db:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: mydb
      POSTGRES_PASSWORD: pass
    volumes:
      - pg_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready"]
      interval: 5s
      timeout: 3s
      retries: 5

  redis:
    image: redis:7-alpine

volumes:
  pg_data:
```

## Tips

- Use `uvicorn` with `--workers N` (N = 2 × CPU cores + 1) for production
- Set `WEB_CONCURRENCY` env var to control worker count dynamically
- Use `gunicorn -k uvicorn.workers.UvicornWorker` for process management
- Always set memory limits — Python can be memory-hungry
- Use `--max-tasks-per-child` for Celery to prevent memory leaks
