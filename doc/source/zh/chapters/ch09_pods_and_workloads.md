# 9. Pod 与工作负载

```{mermaid}
flowchart TB
    Deploy[Deployment] --> RS[ReplicaSet]
    RS --> Pod1[Pod]
    RS --> Pod2[Pod]
    RS --> Pod3[Pod]
    STS[StatefulSet] --> Pod4["Pod-0<br/>(stable identity)"]
    STS --> Pod5["Pod-1"]
    DS[DaemonSet] --> Pod6["Pod<br/>(every node)"]
    Job[Job] --> Pod7["Pod<br/>(run to completion)"]
    CJ[CronJob] --> Job
```

## Pod 生命周期

```{mermaid}
stateDiagram-v2
    [*] --> Pending: Scheduled
    Pending --> Running: Containers started
    Running --> Succeeded: All containers exit 0
    Running --> Failed: Container exits non-zero
    Running --> Unknown: Node unreachable
    Succeeded --> [*]
    Failed --> [*]
```

## Pod 规格定义

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: myapp
  labels:
    app: myapp
spec:
  # Init containers run before app containers
  initContainers:
    - name: init-db
      image: busybox:1.36
      command: ['sh', '-c', 'until nc -z db-svc 3306; do sleep 2; done']

  containers:
    - name: app
      image: myapp:1.0
      ports:
        - containerPort: 8080
      env:
        - name: DB_HOST
          value: "db-svc"
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: db-secret
              key: password
      resources:
        requests:
          cpu: "250m"
          memory: "256Mi"
        limits:
          cpu: "500m"
          memory: "512Mi"
      livenessProbe:
        httpGet:
          path: /healthz
          port: 8080
        initialDelaySeconds: 10
        periodSeconds: 15
      readinessProbe:
        httpGet:
          path: /readyz
          port: 8080
        initialDelaySeconds: 5
        periodSeconds: 10
      startupProbe:
        httpGet:
          path: /healthz
          port: 8080
        failureThreshold: 30
        periodSeconds: 10

    # Sidecar container
    - name: log-shipper
      image: fluent/fluent-bit:2.2
      volumeMounts:
        - name: logs
          mountPath: /var/log/app

  volumes:
    - name: logs
      emptyDir: {}

  restartPolicy: Always
  terminationGracePeriodSeconds: 30
```

上述示例展示了一个完整的 Pod 定义，包含以下关键部分：

- **初始化容器（Init Container）**：在应用容器启动前运行，此处用于等待数据库就绪
- **应用容器**：主业务容器，配置了环境变量、资源限制和三种健康检查探针
  - **存活探针（livenessProbe）**：检测容器是否存活，失败则重启容器
  - **就绪探针（readinessProbe）**：检测容器是否准备好接收流量
  - **启动探针（startupProbe）**：用于启动较慢的应用，避免被存活探针误杀
- **边车容器（Sidecar）**：辅助容器，此处用于日志采集
- **卷（Volume）**：使用 `emptyDir` 在容器间共享日志目录

## Deployment（部署）

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: web
spec:
  replicas: 3
  selector:
    matchLabels:
      app: web
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1          # max pods above desired count
      maxUnavailable: 0     # zero downtime
  template:
    metadata:
      labels:
        app: web
    spec:
      containers:
        - name: web
          image: myapp:1.0
          ports:
            - containerPort: 8080
          resources:
            requests:
              cpu: "250m"
              memory: "256Mi"
            limits:
              cpu: "500m"
              memory: "512Mi"
```

Deployment 是最常用的工作负载资源，通过 ReplicaSet 管理 Pod 副本。上面的配置采用滚动更新（RollingUpdate）策略，`maxSurge: 1` 表示更新时最多多出 1 个 Pod，`maxUnavailable: 0` 确保零停机更新。

```bash
# Rollout management
kubectl rollout status deployment/web
kubectl rollout history deployment/web
kubectl rollout undo deployment/web                    # rollback to previous
kubectl rollout undo deployment/web --to-revision=2    # rollback to specific
kubectl rollout restart deployment/web                 # restart all pods
```

## StatefulSet（有状态副本集）

```yaml
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: mysql
spec:
  serviceName: mysql-headless    # required headless service
  replicas: 3
  selector:
    matchLabels:
      app: mysql
  template:
    metadata:
      labels:
        app: mysql
    spec:
      containers:
        - name: mysql
          image: mysql:8.0
          ports:
            - containerPort: 3306
          volumeMounts:
            - name: data
              mountPath: /var/lib/mysql
  volumeClaimTemplates:          # each pod gets its own PVC
    - metadata:
        name: data
      spec:
        accessModes: ["ReadWriteOnce"]
        resources:
          requests:
            storage: 10Gi
---
# Headless service for StatefulSet
apiVersion: v1
kind: Service
metadata:
  name: mysql-headless
spec:
  clusterIP: None
  selector:
    app: mysql
  ports:
    - port: 3306
# Pods get stable DNS: mysql-0.mysql-headless, mysql-1.mysql-headless, etc.
```

StatefulSet 适用于需要稳定网络标识和持久化存储的有状态应用（如数据库）。每个 Pod 拥有固定的序号和稳定的 DNS 名称，且通过 `volumeClaimTemplates` 为每个 Pod 自动创建独立的 PVC。

## DaemonSet（守护进程集）

```yaml
apiVersion: apps/v1
kind: DaemonSet
metadata:
  name: node-exporter
  namespace: monitoring
spec:
  selector:
    matchLabels:
      app: node-exporter
  template:
    metadata:
      labels:
        app: node-exporter
    spec:
      containers:
        - name: node-exporter
          image: prom/node-exporter:v1.7.0
          ports:
            - containerPort: 9100
              hostPort: 9100
      tolerations:
        - operator: Exists    # run on all nodes including control plane
```

DaemonSet 确保在集群的每个节点（或符合条件的节点）上运行一个 Pod 副本，常用于日志采集、监控代理、网络插件等节点级别的守护进程。

## Job 与 CronJob

```yaml
# One-time Job
apiVersion: batch/v1
kind: Job
metadata:
  name: db-migration
spec:
  backoffLimit: 3
  activeDeadlineSeconds: 300
  template:
    spec:
      containers:
        - name: migrate
          image: myapp:1.0
          command: ["./migrate", "--up"]
      restartPolicy: Never
---
# CronJob
apiVersion: batch/v1
kind: CronJob
metadata:
  name: db-backup
spec:
  schedule: "0 2 * * *"    # daily at 2 AM
  concurrencyPolicy: Forbid
  successfulJobsHistoryLimit: 3
  failedJobsHistoryLimit: 1
  jobTemplate:
    spec:
      template:
        spec:
          containers:
            - name: backup
              image: mysql:8.0
              command:
                - /bin/sh
                - -c
                - mysqldump -h db-svc -u root -p$MYSQL_ROOT_PASSWORD mydb | gzip > /backup/mydb-$(date +%Y%m%d).sql.gz
              envFrom:
                - secretRef:
                    name: db-secret
              volumeMounts:
                - name: backup
                  mountPath: /backup
          volumes:
            - name: backup
              persistentVolumeClaim:
                claimName: backup-pvc
          restartPolicy: OnFailure
```

- **Job**：一次性任务，Pod 运行完成后即结束。`backoffLimit` 控制失败重试次数，`activeDeadlineSeconds` 设置超时时间。
- **CronJob**：定时任务，按 cron 表达式周期性创建 Job。`concurrencyPolicy: Forbid` 表示上一次任务未完成时不会启动新任务。

## QoS 服务质量等级

| 等级 | 条件 | 驱逐优先级 |
|------|------|------------|
| **Guaranteed（保证型）** | 所有容器的 requests 与 limits 相等 | 最后被驱逐 |
| **Burstable（突发型）** | 至少设置了一个 request 或 limit | 中等 |
| **BestEffort（尽力型）** | 未设置任何 request 或 limit | 最先被驱逐 |

当节点资源不足时，Kubernetes 会按照 BestEffort → Burstable → Guaranteed 的顺序驱逐 Pod。因此，生产环境中建议为所有容器设置合理的资源请求和限制。

## Pod 干扰预算（PodDisruptionBudget）

```yaml
apiVersion: policy/v1
kind: PodDisruptionBudget
metadata:
  name: web-pdb
spec:
  minAvailable: 2    # or maxUnavailable: 1
  selector:
    matchLabels:
      app: web
```

PodDisruptionBudget（PDB）用于保障自愿中断（如节点维护、集群升级）期间的服务可用性。上述配置确保在任何时刻至少有 2 个 `app: web` 的 Pod 保持运行。
