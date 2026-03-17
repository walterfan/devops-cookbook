# 23. Go 应用上 K8s

```{mermaid}
flowchart TB
    subgraph Build
        Code[Go Source] --> Docker[Multi-stage Build]
        Docker --> Image[scratch Image<br/>~10MB]
    end
    subgraph K8s["Kubernetes"]
        Deploy[Deployment] --> Pod1[Pod]
        Deploy --> Pod2[Pod]
        HPA[HPA] --> Deploy
        Svc[Service] --> Pod1
        Svc --> Pod2
        Ing[Ingress] --> Svc
    end
    Image --> Deploy
```

## Go 应用模板

```go
// main.go
package main

import (
    "context"
    "log/slog"
    "net/http"
    "os"
    "os/signal"
    "syscall"
    "time"

    "github.com/prometheus/client_golang/prometheus"
    "github.com/prometheus/client_golang/prometheus/promhttp"
)

var (
    httpRequestsTotal = prometheus.NewCounterVec(
        prometheus.CounterOpts{
            Name: "http_requests_total",
            Help: "Total HTTP requests",
        },
        []string{"method", "path", "status"},
    )
    httpRequestDuration = prometheus.NewHistogramVec(
        prometheus.HistogramOpts{
            Name:    "http_request_duration_seconds",
            Help:    "HTTP request duration",
            Buckets: prometheus.DefBuckets,
        },
        []string{"method", "path"},
    )
)

func init() {
    prometheus.MustRegister(httpRequestsTotal, httpRequestDuration)
}

func main() {
    logger := slog.New(slog.NewJSONHandler(os.Stdout, &slog.HandlerOptions{Level: slog.LevelInfo}))
    slog.SetDefault(logger)

    mux := http.NewServeMux()
    mux.HandleFunc("GET /healthz", func(w http.ResponseWriter, r *http.Request) {
        w.WriteHeader(http.StatusOK)
        w.Write([]byte(`{"status":"ok"}`))
    })
    mux.HandleFunc("GET /readyz", func(w http.ResponseWriter, r *http.Request) {
        // Check dependencies (DB, cache, etc.)
        w.WriteHeader(http.StatusOK)
        w.Write([]byte(`{"status":"ready"}`))
    })
    mux.Handle("GET /metrics", promhttp.Handler())
    mux.HandleFunc("GET /api/hello", func(w http.ResponseWriter, r *http.Request) {
        w.Header().Set("Content-Type", "application/json")
        w.Write([]byte(`{"message":"Hello, Kubernetes!"}`))
    })

    port := os.Getenv("PORT")
    if port == "" {
        port = "8080"
    }

    server := &http.Server{
        Addr:         ":" + port,
        Handler:      mux,
        ReadTimeout:  10 * time.Second,
        WriteTimeout: 30 * time.Second,
        IdleTimeout:  60 * time.Second,
    }

    // Graceful shutdown
    go func() {
        slog.Info("Starting server", "port", port)
        if err := server.ListenAndServe(); err != http.ErrServerClosed {
            slog.Error("Server error", "error", err)
            os.Exit(1)
        }
    }()

    quit := make(chan os.Signal, 1)
    signal.Notify(quit, syscall.SIGINT, syscall.SIGTERM)
    <-quit

    slog.Info("Shutting down server...")
    ctx, cancel := context.WithTimeout(context.Background(), 30*time.Second)
    defer cancel()
    if err := server.Shutdown(ctx); err != nil {
        slog.Error("Server forced to shutdown", "error", err)
    }
    slog.Info("Server stopped")
}
```

## Dockerfile（多阶段构建）

```dockerfile
# Stage 1: Build
FROM golang:1.22-alpine AS builder
RUN apk add --no-cache git ca-certificates
WORKDIR /app
COPY go.mod go.sum ./
RUN go mod download
COPY . .
RUN CGO_ENABLED=0 GOOS=linux GOARCH=amd64 \
    go build -ldflags="-s -w -X main.version=$(git describe --tags --always)" \
    -o /app/server ./cmd/server

# Stage 2: Runtime (scratch = ~0 bytes base)
FROM scratch
COPY --from=builder /etc/ssl/certs/ca-certificates.crt /etc/ssl/certs/
COPY --from=builder /app/server /server
EXPOSE 8080
ENTRYPOINT ["/server"]
```

## Kubernetes 资源清单

### Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: go-api
  labels:
    app: go-api
spec:
  replicas: 3
  selector:
    matchLabels:
      app: go-api
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 0
  template:
    metadata:
      labels:
        app: go-api
      annotations:
        prometheus.io/scrape: "true"
        prometheus.io/port: "8080"
        prometheus.io/path: "/metrics"
    spec:
      containers:
        - name: go-api
          image: registry.example.com/go-api:1.0.0
          ports:
            - containerPort: 8080
              name: http
          env:
            - name: PORT
              value: "8080"
            - name: DB_HOST
              valueFrom:
                configMapKeyRef:
                  name: go-api-config
                  key: db-host
          resources:
            requests:
              cpu: "100m"
              memory: "64Mi"
            limits:
              cpu: "500m"
              memory: "128Mi"
          livenessProbe:
            httpGet:
              path: /healthz
              port: http
            initialDelaySeconds: 5
            periodSeconds: 15
          readinessProbe:
            httpGet:
              path: /readyz
              port: http
            initialDelaySeconds: 3
            periodSeconds: 10
          startupProbe:
            httpGet:
              path: /healthz
              port: http
            failureThreshold: 10
            periodSeconds: 5
      terminationGracePeriodSeconds: 30
```

### Service + HPA

```yaml
apiVersion: v1
kind: Service
metadata:
  name: go-api
spec:
  selector:
    app: go-api
  ports:
    - port: 80
      targetPort: http
---
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: go-api
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: go-api
  minReplicas: 2
  maxReplicas: 20
  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: 70
    - type: Resource
      resource:
        name: memory
        target:
          type: Utilization
          averageUtilization: 80
```

## Helm Chart

```bash
helm create go-api
# Edit values.yaml, templates/deployment.yaml, etc.
helm install go-api ./go-api -f values-prod.yaml
```

### values.yaml

```yaml
replicaCount: 3
image:
  repository: registry.example.com/go-api
  tag: "1.0.0"
  pullPolicy: IfNotPresent
service:
  type: ClusterIP
  port: 80
ingress:
  enabled: true
  className: nginx
  hosts:
    - host: api.example.com
      paths:
        - path: /
          pathType: Prefix
resources:
  requests:
    cpu: 100m
    memory: 64Mi
  limits:
    cpu: 500m
    memory: 128Mi
autoscaling:
  enabled: true
  minReplicas: 2
  maxReplicas: 20
  targetCPUUtilizationPercentage: 70
```

## 为什么 Go 非常适合 K8s

| 特性 | 优势 |
|------|------|
| 静态编译二进制文件 | 无运行时依赖，可使用 `scratch` 作为基础镜像 |
| 镜像体积小 | 约 10-15MB，而 Java/Python 通常超过 200MB |
| 启动速度快 | 冷启动 < 100ms |
| 内存占用低 | 每个实例约 10-30MB |
| 内置并发支持 | goroutine 天然适合高吞吐场景 |
| 信号处理 | 可实现干净的优雅关闭(Graceful Shutdown) |
