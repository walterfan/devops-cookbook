# 6. Kubernetes Architecture

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

## Control Plane Components

| Component | Role |
|-----------|------|
| **API Server** | Front-end for K8s API, all communication goes through it |
| **etcd** | Distributed key-value store for all cluster data |
| **Scheduler** | Assigns pods to nodes based on resource requirements |
| **Controller Manager** | Runs controllers (Deployment, ReplicaSet, Node, Job, etc.) |
| **Cloud Controller Manager** | Integrates with cloud provider APIs (optional) |

## Worker Node Components

| Component | Role |
|-----------|------|
| **kubelet** | Agent on each node, ensures containers are running in pods |
| **kube-proxy** | Network proxy, maintains network rules for service routing |
| **Container Runtime** | Runs containers (containerd, CRI-O) |

## Core API Resources

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

| Category | Resources |
|----------|-----------|
| **Workloads** | Pod, Deployment, StatefulSet, DaemonSet, ReplicaSet, Job, CronJob |
| **Networking** | Service, Ingress, NetworkPolicy, EndpointSlice |
| **Config** | ConfigMap, Secret |
| **Storage** | PersistentVolume, PersistentVolumeClaim, StorageClass |
| **Cluster** | Namespace, Node, ServiceAccount, Role, ClusterRole |

## Namespaces

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

## Labels and Selectors

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

## Annotations

```yaml
metadata:
  annotations:
    description: "Main web application"
    prometheus.io/scrape: "true"
    prometheus.io/port: "8080"
    kubernetes.io/change-cause: "Update to v1.2.3"
```

## Cluster Communication

All communication flows through the API Server:

1. **User → API Server**: kubectl, client libraries, dashboard
2. **API Server → etcd**: store/retrieve cluster state
3. **API Server → kubelet**: pod operations on nodes
4. **kubelet → API Server**: node status, pod status
5. **Controller → API Server**: watch resources, reconcile state
6. **Scheduler → API Server**: watch unscheduled pods, assign nodes

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
