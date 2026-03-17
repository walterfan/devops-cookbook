# 30. 生产环境检查清单

```{mermaid}
mindmap
  root((Production<br/>Readiness))
    Resources
      Requests & Limits
      HPA
      PDB
      Resource Quotas
    Reliability
      Health Checks
      Anti-affinity
      Graceful Shutdown
      Rollback Plan
    Security
      RBAC
      NetworkPolicy
      Image Scanning
      Secrets Management
    Observability
      Metrics
      Logging
      Tracing
      Alerting
    Networking
      TLS
      Ingress
      DNS
      Rate Limiting
    Backup
      etcd Backup
      PV Snapshots
      DB Backup
```

## 资源管理(Resource Management)

| 检查项 | 状态 | 说明 |
|--------|------|------|
| ☐ 所有容器均已设置 CPU/Memory requests | | 避免调度异常 |
| ☐ 所有容器均已设置 CPU/Memory limits | | 防止"吵闹邻居"问题 |
| ☐ 无状态工作负载已配置 HPA | | 基于 CPU/内存/自定义指标自动扩缩容 |
| ☐ 有状态工作负载已评估 VPA | | 自动调整资源请求至合理水平 |
| ☐ 已设置 PodDisruptionBudget | | 确保节点排空(Drain)期间的可用性 |
| ☐ 命名空间已设置 ResourceQuota | | 防止资源被耗尽 |
| ☐ 命名空间已设置 LimitRange | | 为未显式指定限制的 Pod 提供默认值 |

```yaml
# ResourceQuota
apiVersion: v1
kind: ResourceQuota
metadata:
  name: production-quota
  namespace: production
spec:
  hard:
    requests.cpu: "20"
    requests.memory: 40Gi
    limits.cpu: "40"
    limits.memory: 80Gi
    pods: "100"
    persistentvolumeclaims: "20"
---
# LimitRange
apiVersion: v1
kind: LimitRange
metadata:
  name: default-limits
  namespace: production
spec:
  limits:
    - default:
        cpu: "500m"
        memory: "512Mi"
      defaultRequest:
        cpu: "100m"
        memory: "128Mi"
      type: Container
```

## 可靠性(Reliability)

| 检查项 | 状态 | 说明 |
|--------|------|------|
| ☐ 已配置存活探针(Liveness Probe) | | 自动重启不健康的容器 |
| ☐ 已配置就绪探针(Readiness Probe) | | 启动期间或异常时从 Service 中摘除 |
| ☐ 慢启动应用已配置启动探针(Startup Probe) | | 防止存活探针过早判定失败 |
| ☐ 已实现优雅关闭(Graceful Shutdown) | | 正确处理 SIGTERM 信号，排空连接 |
| ☐ 已设置 terminationGracePeriodSeconds | | 默认 30 秒，按需调大 |
| ☐ 已配置 Pod 反亲和性(Anti-affinity) | | 将 Pod 分散到不同节点 |
| ☐ 已配置拓扑分布约束(Topology Spread Constraints) | | 跨可用区均匀分布 |
| ☐ 已配置滚动更新策略(Rolling Update Strategy) | | maxSurge=1, maxUnavailable=0 实现零停机 |
| ☐ 已编写回滚预案 | | `kubectl rollout undo` 或 Helm rollback |

```yaml
# Pod 反亲和性 + 拓扑分布约束
spec:
  affinity:
    podAntiAffinity:
      preferredDuringSchedulingIgnoredDuringExecution:
        - weight: 100
          podAffinityTerm:
            labelSelector:
              matchExpressions:
                - key: app
                  operator: In
                  values: ["myapp"]
            topologyKey: kubernetes.io/hostname
  topologySpreadConstraints:
    - maxSkew: 1
      topologyKey: topology.kubernetes.io/zone
      whenUnsatisfiable: DoNotSchedule
      labelSelector:
        matchLabels:
          app: myapp
```

## 安全(Security)

| 检查项 | 状态 | 说明 |
|--------|------|------|
| ☐ 已配置 RBAC（最小权限原则） | | 应用禁止使用 cluster-admin |
| ☐ 已应用 NetworkPolicy | | 默认拒绝 + 显式放行 |
| ☐ 已启用 Pod 安全标准(Pod Security Standards) | | 生产环境使用 `restricted` 级别 |
| ☐ 容器以非 root 用户运行 | | `runAsNonRoot: true` |
| ☐ 根文件系统设为只读 | | `readOnlyRootFilesystem: true` |
| ☐ 禁止特权提升 | | `allowPrivilegeEscalation: false` |
| ☐ 已丢弃所有 Linux Capabilities | | `drop: ["ALL"]` |
| ☐ CI 中已集成镜像漏洞扫描 | | 发现严重 CVE 时阻断流水线（Trivy/Snyk） |
| ☐ 镜像已签名 | | Cosign + 准入策略(Admission Policy) |
| ☐ 密钥由外部系统管理 | | Vault / Sealed Secrets / External Secrets |
| ☐ 私有镜像仓库已配置 imagePullSecrets | | 确保镜像可正常拉取 |
| ☐ 生产环境禁止使用 `:latest` 标签 | | 使用明确的版本标签 |

## 可观测性(Observability)

| 检查项 | 状态 | 说明 |
|--------|------|------|
| ☐ 已暴露 Prometheus 指标 | | `/metrics` 端点 |
| ☐ 已配置 ServiceMonitor | | Prometheus 采集配置 |
| ☐ 已创建 Grafana 仪表盘 | | 涵盖关键业务指标和基础设施指标 |
| ☐ 已定义告警规则 | | 错误率、延迟、饱和度 |
| ☐ 日志采用结构化格式（JSON） | | 便于日志聚合器解析 |
| ☐ 已配置日志聚合 | | Loki / ELK / CloudWatch |
| ☐ 已启用分布式链路追踪 | | OpenTelemetry / Jaeger |
| ☐ 已定义并监控 SLO | | 可用性、延迟目标 |

### 必须配置的核心告警

```text
# 必备告警
- HighErrorRate: 5xx 错误率 > 5% 持续 5 分钟
- HighLatency: p99 延迟 > 1s 持续 5 分钟
- PodCrashLooping: 重启频率 > 0 持续 5 分钟
- PodOOMKilled: 任何 OOM 事件
- NodeNotReady: 节点 NotReady 持续 5 分钟
- DiskPressure: 磁盘使用率 > 85%
- CertificateExpiring: TLS 证书将在 14 天内过期
- PVCAlmostFull: PVC 使用率 > 85%
```

## 网络(Networking)

| 检查项 | 状态 | 说明 |
|--------|------|------|
| ☐ Ingress 层已配置 TLS 终止 | | cert-manager + Let's Encrypt |
| ☐ 已配置 HTTP → HTTPS 重定向 | | `ssl-redirect` 注解 |
| ☐ 已配置速率限制(Rate Limiting) | | 防止滥用 |
| ☐ 已配置 CORS | | 如果 API 需要被浏览器访问 |
| ☐ 已设置安全响应头 | | X-Frame-Options、CSP 等 |
| ☐ DNS 记录已配置 | | 所有服务的 A/CNAME 记录 |
| ☐ 健康检查端点已排除认证 | | `/healthz`、`/readyz` |

## 备份与灾难恢复(Backup & Disaster Recovery)

| 检查项 | 状态 | 说明 |
|--------|------|------|
| ☐ etcd 备份已自动化 | | 每日快照，已验证恢复流程 |
| ☐ 已配置 PV 快照 | | 数据库使用 VolumeSnapshot |
| ☐ 数据库备份已自动化 | | pg_dump / mysqldump + 定时任务 |
| ☐ 备份存储在集群外部 | | S3 / GCS / 独立存储 |
| ☐ 恢复流程已文档化 | | 逐步操作手册(Runbook) |
| ☐ 灾难恢复已演练 | | 定期进行 DR 演练 |

## CI/CD

| 检查项 | 状态 | 说明 |
|--------|------|------|
| ☐ CI 流水线已运行测试 | | 单元测试 + 集成测试 |
| ☐ CI 中已构建并扫描镜像 | | 发现严重 CVE 时阻断 |
| ☐ 已配置 GitOps 工作流 | | ArgoCD / Flux |
| ☐ 已配置金丝雀/蓝绿部署 | | 渐进式发布(Progressive Rollout) |
| ☐ 回滚流程已文档化 | | 一条命令即可回滚 |
| ☐ 预发布环境与生产环境保持一致 | | 相同配置，较小规模 |

## 部署前验证

```bash
# 验证清单文件
kubectl apply --dry-run=server -f manifests/
helm template myapp ./chart -f values-prod.yaml | kubectl apply --dry-run=server -f -

# 检查资源配额
kubectl describe resourcequota -n production

# 验证镜像是否存在
docker manifest inspect registry.example.com/myapp:1.2.3

# 部署后运行冒烟测试
kubectl rollout status deployment/myapp --timeout=300s
curl -f https://myapp.example.com/healthz
```
