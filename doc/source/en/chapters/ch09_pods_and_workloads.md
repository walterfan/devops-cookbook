# 9. Pods and Workloads

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

## Pod Lifecycle

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

## Pod Spec

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

## Deployment

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

```bash
# Rollout management
kubectl rollout status deployment/web
kubectl rollout history deployment/web
kubectl rollout undo deployment/web                    # rollback to previous
kubectl rollout undo deployment/web --to-revision=2    # rollback to specific
kubectl rollout restart deployment/web                 # restart all pods
```

## StatefulSet

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

## DaemonSet

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

## Job and CronJob

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

## QoS Classes

| Class | Condition | Eviction Priority |
|-------|-----------|-------------------|
| **Guaranteed** | requests == limits for all containers | Last to be evicted |
| **Burstable** | At least one request or limit set | Middle |
| **BestEffort** | No requests or limits | First to be evicted |

## Pod Disruption Budget

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
