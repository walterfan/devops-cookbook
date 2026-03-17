# 监控体系

```{mermaid}
flowchart LR
    App[Application] -->|/metrics| Prom[Prometheus]
    NE[Node Exporter] -->|/metrics| Prom
    KSM[kube-state-metrics] -->|/metrics| Prom
    Prom --> AM[Alertmanager]
    Prom --> Grafana[Grafana]
    AM --> Slack[Slack / PagerDuty]
    SM[ServiceMonitor] -->|configure| Prom
```

## Prometheus Architecture

| Component | Role |
|-----------|------|
| **Prometheus Server** | Scrapes and stores time-series metrics |
| **Alertmanager** | Handles alerts (dedup, grouping, routing) |
| **Node Exporter** | Hardware/OS metrics from nodes |
| **kube-state-metrics** | K8s object state metrics |
| **Pushgateway** | For short-lived batch jobs |
| **ServiceMonitor** | CRD to configure scrape targets |

## Install with Helm (kube-prometheus-stack)

```bash
helm repo add prometheus https://prometheus-community.github.io/helm-charts
helm repo update

helm install monitoring prometheus/kube-prometheus-stack \
  -n monitoring --create-namespace \
  -f values-monitoring.yaml
```

### values-monitoring.yaml

```yaml
prometheus:
  prometheusSpec:
    retention: 15d
    storageSpec:
      volumeClaimTemplate:
        spec:
          storageClassName: fast-ssd
          accessModes: ["ReadWriteOnce"]
          resources:
            requests:
              storage: 50Gi

grafana:
  adminPassword: "admin123"
  persistence:
    enabled: true
    size: 10Gi

alertmanager:
  config:
    global:
      resolve_timeout: 5m
    route:
      receiver: slack
      group_by: ['alertname', 'namespace']
      group_wait: 30s
      group_interval: 5m
      repeat_interval: 4h
    receivers:
      - name: slack
        slack_configs:
          - api_url: 'https://hooks.slack.com/services/xxx'
            channel: '#alerts'
            title: '{{ .GroupLabels.alertname }}'
            text: '{{ range .Alerts }}{{ .Annotations.description }}{{ end }}'
```

## Metrics Types

| Type | Description | Example |
|------|-------------|---------|
| **Counter** | Monotonically increasing | `http_requests_total` |
| **Gauge** | Can go up and down | `temperature_celsius` |
| **Histogram** | Observations in buckets | `http_request_duration_seconds` |
| **Summary** | Quantiles over sliding window | `go_gc_duration_seconds` |

## PromQL Basics

```promql
# Instant vector
http_requests_total{method="GET", status="200"}

# Rate (per-second increase over 5m)
rate(http_requests_total[5m])

# Increase over time
increase(http_requests_total[1h])

# Aggregation
sum(rate(http_requests_total[5m])) by (service)
avg(node_memory_MemAvailable_bytes / node_memory_MemTotal_bytes) by (instance)

# Top 5 pods by CPU
topk(5, sum(rate(container_cpu_usage_seconds_total[5m])) by (pod))

# Histogram quantile (p99 latency)
histogram_quantile(0.99, sum(rate(http_request_duration_seconds_bucket[5m])) by (le))

# Alert condition: high error rate
sum(rate(http_requests_total{status=~"5.."}[5m])) / sum(rate(http_requests_total[5m])) > 0.05
```

## ServiceMonitor

```yaml
apiVersion: monitoring.coreos.com/v1
kind: ServiceMonitor
metadata:
  name: myapp
  labels:
    release: monitoring    # must match Prometheus selector
spec:
  selector:
    matchLabels:
      app: myapp
  endpoints:
    - port: metrics
      interval: 15s
      path: /metrics
```

## Alerting Rules

```yaml
apiVersion: monitoring.coreos.com/v1
kind: PrometheusRule
metadata:
  name: myapp-alerts
  labels:
    release: monitoring
spec:
  groups:
    - name: myapp
      rules:
        - alert: HighErrorRate
          expr: sum(rate(http_requests_total{status=~"5.."}[5m])) / sum(rate(http_requests_total[5m])) > 0.05
          for: 5m
          labels:
            severity: critical
          annotations:
            summary: "High error rate on {{ $labels.service }}"
            description: "Error rate is {{ $value | humanizePercentage }}"

        - alert: PodCrashLooping
          expr: rate(kube_pod_container_status_restarts_total[15m]) > 0
          for: 5m
          labels:
            severity: warning
          annotations:
            summary: "Pod {{ $labels.pod }} is crash looping"

        - alert: HighMemoryUsage
          expr: container_memory_working_set_bytes / container_spec_memory_limit_bytes > 0.9
          for: 5m
          labels:
            severity: warning
          annotations:
            summary: "Pod {{ $labels.pod }} memory usage > 90%"
```

## Access Dashboards

```bash
# Grafana
kubectl port-forward svc/monitoring-grafana 3000:80 -n monitoring
# Open http://localhost:3000 (admin / admin123)

# Prometheus
kubectl port-forward svc/monitoring-kube-prometheus-prometheus 9090:9090 -n monitoring

# Alertmanager
kubectl port-forward svc/monitoring-kube-prometheus-alertmanager 9093:9093 -n monitoring
```

## Useful Grafana Dashboard IDs

| Dashboard | ID | Description |
|-----------|----|-------------|
| K8s Cluster | 7249 | Cluster overview |
| Node Exporter | 1860 | Node metrics |
| K8s Pods | 6417 | Pod resource usage |
| Nginx Ingress | 9614 | Ingress metrics |
| CoreDNS | 5926 | DNS metrics |
