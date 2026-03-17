# 6. Kubernetes 架构

```{mermaid}
flowchart TB
    subgraph CP["Control Plane"]
        API[API Server]
        ETCD[(etcd)]
        SCHED[Scheduler]
        CM[Controller Manager]
        CCM[Cloud Controller Manager]
        API --> ETCD
        SCHED --> API
        CM --> API
        CCM --> API
    end
    subgraph W1["Worker Node 1"]
        KL1[kubelet]
        KP1[kube-proxy]
        CR1[Container Runtime]
        P1[Pod A]
        P2[Pod B]
        KL1 --> CR1
        CR1 --> P1
        CR1 --> P2
    end
    subgraph W2["Worker Node 2"]
        KL2[kubelet]
        KP2[kube-proxy]
        CR2[Container Runtime]
        P3[Pod C]
        KL2 --> CR2
        CR2 --> P3
    end
    API --> KL1
    API --> KL2
```

## 控制平面组件

| 组件 | 职责 |
|------|------|
| **API Server** | K8s API 的前端入口，所有通信都经由它中转 |
| **etcd** | 分布式键值存储，保存集群的全部状态数据 |
| **Scheduler（调度器）** | 根据资源需求将 Pod 分配到合适的节点 |
| **Controller Manager（控制器管理器）** | 运行各类控制器（Deployment、ReplicaSet、Node、Job 等） |
| **Cloud Controller Manager** | 与云服务商 API 对接（可选组件） |

## 工作节点组件

| 组件 | 职责 |
|------|------|
| **kubelet** | 运行在每个节点上的代理，确保容器在 Pod 中正常运行 |
| **kube-proxy** | 网络代理，维护 Service 路由所需的网络规则 |
| **Container Runtime（容器运行时）** | 负责运行容器（containerd、CRI-O） |

## 核心 API 资源

```{mermaid}
flowchart TB
    NS[Namespace] --> Deploy[Deployment]
    NS --> STS[StatefulSet]
    NS --> DS[DaemonSet]
    NS --> Job[Job / CronJob]
    Deploy --> RS[ReplicaSet]
    RS --> Pod[Pod]
    STS --> Pod
    DS --> Pod
    Job --> Pod
    NS --> Svc[Service]
    NS --> Ing[Ingress]
    NS --> CM[ConfigMap]
    NS --> Sec[Secret]
    NS --> PVC[PersistentVolumeClaim]
    Svc --> Pod
```

| 类别 | 资源 |
|------|------|
| **工作负载（Workloads）** | Pod、Deployment、StatefulSet、DaemonSet、ReplicaSet、Job、CronJob |
| **网络（Networking）** | Service、Ingress、NetworkPolicy、EndpointSlice |
| **配置（Config）** | ConfigMap、Secret |
| **存储（Storage）** | PersistentVolume、PersistentVolumeClaim、StorageClass |
| **集群（Cluster）** | Namespace、Node、ServiceAccount、Role、ClusterRole |

## 命名空间（Namespace）

```bash
# List namespaces
kubectl get namespaces

# Create namespace
kubectl create namespace dev

# Set default namespace
kubectl config set-context --current --namespace=dev

# Default namespaces:
# - default: for user workloads
# - kube-system: for K8s system components
# - kube-public: publicly readable
# - kube-node-lease: node heartbeat leases
```

## 标签与选择器（Labels and Selectors）

```yaml
# Labels on a pod
apiVersion: v1
kind: Pod
metadata:
  name: web
  labels:
    app: myapp
    env: production
    tier: frontend
    version: v1.2.3
```

```bash
# Select by label
kubectl get pods -l app=myapp
kubectl get pods -l 'env in (production, staging)'
kubectl get pods -l app=myapp,tier=frontend

# Add/remove labels
kubectl label pod web release=stable
kubectl label pod web release-
```

## 注解（Annotations）

```yaml
metadata:
  annotations:
    description: "Main web application"
    prometheus.io/scrape: "true"
    prometheus.io/port: "8080"
    kubernetes.io/change-cause: "Update to v1.2.3"
```

## 集群通信机制

所有通信都通过 API Server 进行中转：

1. **用户 → API Server**：通过 kubectl、客户端库或 Dashboard 发起请求
2. **API Server → etcd**：存储和读取集群状态
3. **API Server → kubelet**：向节点下发 Pod 操作指令
4. **kubelet → API Server**：上报节点状态和 Pod 状态
5. **Controller → API Server**：监听资源变化，执行状态协调（Reconcile）
6. **Scheduler → API Server**：监听未调度的 Pod，为其分配节点

```bash
# View cluster info
kubectl cluster-info
kubectl get nodes -o wide
kubectl get componentstatuses  # deprecated but still works

# View API resources
kubectl api-resources
kubectl api-versions

# View API server endpoints
kubectl get --raw /healthz
kubectl get --raw /apis
```
