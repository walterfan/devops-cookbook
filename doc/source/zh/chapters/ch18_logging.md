# 18. 日志收集

```{mermaid}
flowchart LR
    subgraph Nodes
        P1[Pod] -->|stdout/stderr| NL[Node Log Files]
        P2[Pod] -->|stdout/stderr| NL
    end
    NL --> FB[Fluent Bit<br/>DaemonSet]
    FB --> Loki[(Loki)]
    FB --> ES[(Elasticsearch)]
    Loki --> Grafana[Grafana]
    ES --> Kibana[Kibana]
```

## 日志架构模式

| 模式 | 说明 | 优点 | 缺点 |
|------|------|------|------|
| **节点级代理(Node-level Agent)** | 以 DaemonSet 方式从 `/var/log` 采集 | 部署简单，资源开销低 | 仅能采集 stdout/stderr |
| **Sidecar 容器** | 每个 Pod 内附带日志采集代理 | 支持自定义日志路径 | 资源消耗较高 |
| **应用直推(Direct)** | 应用直接将日志发送到后端 | 灵活性高 | 与应用耦合 |

## Fluent Bit（轻量级方案）

### DaemonSet 配置

```yaml
apiVersion: apps/v1
kind: DaemonSet
metadata:
  name: fluent-bit
  namespace: logging
spec:
  selector:
    matchLabels:
      app: fluent-bit
  template:
    metadata:
      labels:
        app: fluent-bit
    spec:
      serviceAccountName: fluent-bit
      containers:
        - name: fluent-bit
          image: fluent/fluent-bit:2.2
          volumeMounts:
            - name: varlog
              mountPath: /var/log
              readOnly: true
            - name: containers
              mountPath: /var/lib/docker/containers
              readOnly: true
            - name: config
              mountPath: /fluent-bit/etc/
      volumes:
        - name: varlog
          hostPath:
            path: /var/log
        - name: containers
          hostPath:
            path: /var/lib/docker/containers
        - name: config
          configMap:
            name: fluent-bit-config
```

### Fluent Bit 配置文件

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: fluent-bit-config
  namespace: logging
data:
  fluent-bit.conf: |
    [SERVICE]
        Flush         5
        Log_Level     info
        Parsers_File  parsers.conf

    [INPUT]
        Name              tail
        Tag               kube.*
        Path              /var/log/containers/*.log
        Parser            cri
        DB                /var/log/flb_kube.db
        Mem_Buf_Limit     5MB
        Skip_Long_Lines   On
        Refresh_Interval  10

    [FILTER]
        Name                kubernetes
        Match               kube.*
        Kube_URL            https://kubernetes.default.svc:443
        Kube_CA_File        /var/run/secrets/kubernetes.io/serviceaccount/ca.crt
        Kube_Token_File     /var/run/secrets/kubernetes.io/serviceaccount/token
        Merge_Log           On
        K8S-Logging.Parser  On
        K8S-Logging.Exclude On

    [OUTPUT]
        Name            es
        Match           *
        Host            elasticsearch.logging.svc
        Port            9200
        Index           k8s-logs
        Type            _doc
        Logstash_Format On
        Retry_Limit     False

  parsers.conf: |
    [PARSER]
        Name        cri
        Format      regex
        Regex       ^(?<time>[^ ]+) (?<stream>stdout|stderr) (?<logtag>[^ ]*) (?<message>.*)$
        Time_Key    time
        Time_Format %Y-%m-%dT%H:%M:%S.%L%z
```

## Loki + Promtail（Grafana 技术栈）

### 通过 Helm 安装

```bash
# Loki
helm repo add grafana https://grafana.github.io/helm-charts
helm install loki grafana/loki-stack \
  -n logging --create-namespace \
  --set promtail.enabled=true \
  --set grafana.enabled=false    # use existing Grafana
```

### Loki 配置

```yaml
loki:
  persistence:
    enabled: true
    size: 50Gi
  config:
    limits_config:
      retention_period: 168h    # 7 days
    schema_config:
      configs:
        - from: 2024-01-01
          store: tsdb
          object_store: filesystem
          schema: v13
          index:
            prefix: index_
            period: 24h

promtail:
  config:
    snippets:
      pipelineStages:
        - cri: {}
        - json:
            expressions:
              level: level
              msg: msg
        - labels:
            level:
```

### 在 Grafana 中查询日志（LogQL）

```text
# All logs from a namespace
{namespace="default"}

# Filter by pod label
{app="myapp"} |= "error"

# JSON parsing
{app="myapp"} | json | level="error"

# Rate of errors
rate({app="myapp"} |= "error" [5m])

# Top 10 error messages
topk(10, sum by (msg) (count_over_time({app="myapp"} | json | level="error" [1h])))
```

## 在 K8s 上部署 EFK 技术栈

```bash
# Elasticsearch (using ECK operator)
helm repo add elastic https://helm.elastic.co
helm install elastic-operator elastic/eck-operator -n elastic-system --create-namespace
```

```yaml
# Elasticsearch cluster
apiVersion: elasticsearch.k8s.elastic.co/v1
kind: Elasticsearch
metadata:
  name: logs
  namespace: logging
spec:
  version: 8.12.0
  nodeSets:
    - name: default
      count: 3
      config:
        node.store.allow_mmap: false
      volumeClaimTemplates:
        - metadata:
            name: elasticsearch-data
          spec:
            accessModes: ["ReadWriteOnce"]
            resources:
              requests:
                storage: 100Gi
---
# Kibana
apiVersion: kibana.k8s.elastic.co/v1
kind: Kibana
metadata:
  name: logs
  namespace: logging
spec:
  version: 8.12.0
  count: 1
  elasticsearchRef:
    name: logs
```

## 结构化日志最佳实践

```json
{
  "timestamp": "2026-03-17T12:00:00Z",
  "level": "error",
  "service": "user-api",
  "trace_id": "abc123",
  "span_id": "def456",
  "message": "Failed to create user",
  "error": "duplicate email",
  "user_id": "u-789",
  "duration_ms": 45
}
```

- 始终使用 JSON 格式，便于机器解析
- 包含 `trace_id` 和 `span_id`，方便与分布式链路追踪(Distributed Tracing)关联
- 跨服务使用统一的字段命名规范
- 合理使用日志级别：DEBUG、INFO、WARN、ERROR
- 切勿记录敏感信息（密码、令牌、个人隐私数据）
