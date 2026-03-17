# 15. Kubernetes Operator

```{mermaid}
flowchart TB
    User[User] -->|create CR| API[API Server]
    API -->|watch| Controller[Operator Controller]
    Controller -->|reconcile| API
    API --> Deploy[Deployment]
    API --> Svc[Service]
    API --> CM[ConfigMap]
    API --> PVC[PVC]
    CRD[Custom Resource Definition] -->|register| API
```

## Operator 模式

Operator 通过定义**自定义资源(Custom Resource, CR)**和**控制器(Controller)**来扩展 Kubernetes 的能力。控制器持续监听自定义资源的变化，并通过协调(Reconcile)循环将实际状态调整为期望状态。

```{mermaid}
flowchart LR
    A[Observe] --> B[Diff]
    B --> C[Act]
    C --> A
    style A fill:#9cf
    style B fill:#fc9
    style C fill:#9f9
```

## 自定义资源定义(CRD)

```yaml
apiVersion: apiextensions.k8s.io/v1
kind: CustomResourceDefinition
metadata:
  name: databases.example.com
spec:
  group: example.com
  versions:
    - name: v1
      served: true
      storage: true
      schema:
        openAPIV3Schema:
          type: object
          properties:
            spec:
              type: object
              properties:
                engine:
                  type: string
                  enum: ["mysql", "postgres"]
                version:
                  type: string
                replicas:
                  type: integer
                  minimum: 1
                  maximum: 5
                storage:
                  type: string
              required: ["engine", "version"]
            status:
              type: object
              properties:
                phase:
                  type: string
                ready:
                  type: boolean
      subresources:
        status: {}
      additionalPrinterColumns:
        - name: Engine
          type: string
          jsonPath: .spec.engine
        - name: Version
          type: string
          jsonPath: .spec.version
        - name: Ready
          type: boolean
          jsonPath: .status.ready
  scope: Namespaced
  names:
    plural: databases
    singular: database
    kind: Database
    shortNames:
      - db
```

## 自定义资源(CR)

```yaml
apiVersion: example.com/v1
kind: Database
metadata:
  name: my-mysql
spec:
  engine: mysql
  version: "8.0"
  replicas: 3
  storage: "50Gi"
```

```bash
# Use like any K8s resource
kubectl apply -f my-database.yaml
kubectl get databases
kubectl get db    # short name
kubectl describe db my-mysql
kubectl delete db my-mysql
```

## Operator SDK

```bash
# Install Operator SDK
brew install operator-sdk

# Scaffold a Go operator
operator-sdk init --domain example.com --repo github.com/myuser/db-operator
operator-sdk create api --group example --version v1 --kind Database --resource --controller

# Project structure
# ├── api/v1/database_types.go       # CR type definition
# ├── controllers/database_controller.go  # Reconciliation logic
# ├── config/                         # K8s manifests
# ├── Dockerfile
# ├── Makefile
# └── main.go
```

### 协调循环(Reconciliation Loop)简化示例

```go
func (r *DatabaseReconciler) Reconcile(ctx context.Context, req ctrl.Request) (ctrl.Result, error) {
    log := log.FromContext(ctx)

    // Fetch the Database CR
    var db examplev1.Database
    if err := r.Get(ctx, req.NamespacedName, &db); err != nil {
        return ctrl.Result{}, client.IgnoreNotFound(err)
    }

    // Create or update StatefulSet
    sts := &appsv1.StatefulSet{}
    err := r.Get(ctx, types.NamespacedName{Name: db.Name, Namespace: db.Namespace}, sts)
    if errors.IsNotFound(err) {
        sts = r.buildStatefulSet(&db)
        if err := r.Create(ctx, sts); err != nil {
            return ctrl.Result{}, err
        }
        log.Info("Created StatefulSet", "name", sts.Name)
    }

    // Update status
    db.Status.Ready = sts.Status.ReadyReplicas == *sts.Spec.Replicas
    db.Status.Phase = "Running"
    if err := r.Status().Update(ctx, &db); err != nil {
        return ctrl.Result{}, err
    }

    return ctrl.Result{RequeueAfter: 30 * time.Second}, nil
}
```

## 常用 Operator

| Operator | 用途 | 安装方式 |
|----------|------|----------|
| **MySQL Operator** | MySQL InnoDB 集群 | `helm install mysql-operator mysql/mysql-operator` |
| **CloudNativePG** | PostgreSQL | `helm install cnpg cloudnative-pg/cloudnative-pg` |
| **Redis Operator** | Redis 集群 | `helm install redis-operator ot-helm/redis-operator` |
| **Prometheus Operator** | 监控体系 | `helm install kube-prometheus prometheus/kube-prometheus-stack` |
| **Strimzi** | Apache Kafka | `helm install strimzi strimzi/strimzi-kafka-operator` |
| **cert-manager** | TLS 证书管理 | `helm install cert-manager jetstack/cert-manager` |
| **ECK** | Elasticsearch | `helm install eck elastic/eck-operator` |

## Operator 生命周期管理器(OLM)

```bash
# Install OLM
operator-sdk olm install

# Browse OperatorHub
# https://operatorhub.io

# Install an operator from OperatorHub
kubectl create -f https://operatorhub.io/install/prometheus.yaml
```
