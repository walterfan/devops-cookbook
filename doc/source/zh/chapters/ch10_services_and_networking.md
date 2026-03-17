# 10. Service 与网络

```{mermaid}
flowchart LR
    Client[External Client] --> LB[LoadBalancer<br/>:80]
    LB --> NP[NodePort<br/>:30080]
    NP --> CIP[ClusterIP<br/>:80]
    CIP --> Pod1[Pod A]
    CIP --> Pod2[Pod B]
    CIP --> Pod3[Pod C]
    DNS[CoreDNS] -.->|resolves| CIP
```

## Service 类型

### ClusterIP（默认类型）

```yaml
apiVersion: v1
kind: Service
metadata:
  name: myapp
spec:
  type: ClusterIP    # default
  selector:
    app: myapp
  ports:
    - port: 80           # service port
      targetPort: 8080   # container port
      protocol: TCP
```

ClusterIP 是默认的 Service 类型，仅在集群内部可访问。它会分配一个虚拟 IP 地址，集群内的其他 Pod 可以通过该 IP 或 DNS 名称访问服务。

### NodePort

```yaml
apiVersion: v1
kind: Service
metadata:
  name: myapp-nodeport
spec:
  type: NodePort
  selector:
    app: myapp
  ports:
    - port: 80
      targetPort: 8080
      nodePort: 30080    # 30000-32767 range
```

NodePort 在每个节点上开放一个固定端口（范围 30000-32767），外部流量可以通过 `<节点IP>:<NodePort>` 访问服务。

### LoadBalancer

```yaml
apiVersion: v1
kind: Service
metadata:
  name: myapp-lb
  annotations:
    service.beta.kubernetes.io/aws-load-balancer-type: nlb
spec:
  type: LoadBalancer
  selector:
    app: myapp
  ports:
    - port: 80
      targetPort: 8080
```

LoadBalancer 类型会向云服务商申请一个外部负载均衡器，自动将流量转发到后端 Pod。这是在云环境中暴露服务的推荐方式。

### ExternalName

```yaml
apiVersion: v1
kind: Service
metadata:
  name: external-db
spec:
  type: ExternalName
  externalName: db.example.com    # CNAME record
```

ExternalName 类型不做代理转发，而是返回一条 CNAME 记录，将服务名映射到外部域名。适用于引用集群外部的服务。

### Headless Service（无头服务）

```yaml
apiVersion: v1
kind: Service
metadata:
  name: myapp-headless
spec:
  clusterIP: None    # headless
  selector:
    app: myapp
  ports:
    - port: 8080
# DNS returns individual pod IPs instead of a single ClusterIP
```

将 `clusterIP` 设为 `None` 即为无头服务（Headless Service）。DNS 查询会直接返回后端各 Pod 的 IP 地址，而非单一的 ClusterIP。常与 StatefulSet 配合使用。

## Kubernetes 中的 DNS

CoreDNS 为集群内部提供 DNS 解析服务：

| 记录类型 | 格式 | 示例 |
|----------|------|------|
| Service | `<svc>.<ns>.svc.cluster.local` | `myapp.default.svc.cluster.local` |
| Pod | `<pod-ip>.<ns>.pod.cluster.local` | `10-244-1-5.default.pod.cluster.local` |
| StatefulSet Pod | `<pod>.<svc>.<ns>.svc.cluster.local` | `mysql-0.mysql-headless.default.svc.cluster.local` |

```bash
# Test DNS from a debug pod
kubectl run dns-test --rm -it --image=busybox -- nslookup myapp.default.svc.cluster.local
```

## 网络策略（NetworkPolicy）

```yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: allow-web-to-db
  namespace: default
spec:
  podSelector:
    matchLabels:
      app: db
  policyTypes:
    - Ingress
    - Egress
  ingress:
    - from:
        - podSelector:
            matchLabels:
              app: web
      ports:
        - protocol: TCP
          port: 3306
  egress:
    - to:
        - podSelector:
            matchLabels:
              app: web
      ports:
        - protocol: TCP
          port: 8080
---
# Default deny all ingress
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: default-deny-ingress
spec:
  podSelector: {}
  policyTypes:
    - Ingress
```

NetworkPolicy 用于控制 Pod 之间以及 Pod 与外部之间的网络流量。上面的示例仅允许带有 `app: web` 标签的 Pod 访问数据库 Pod 的 3306 端口。需要注意的是，NetworkPolicy 需要网络插件（如 Calico、Cilium）的支持才能生效。

## 服务发现模式

```bash
# 1. Environment variables (auto-injected)
# MYAPP_SERVICE_HOST=10.96.0.10
# MYAPP_SERVICE_PORT=80

# 2. DNS (recommended)
# curl http://myapp.default.svc.cluster.local

# 3. Short DNS (within same namespace)
# curl http://myapp
```

Kubernetes 提供两种服务发现机制：

1. **环境变量**：每个 Pod 启动时会自动注入同命名空间下所有 Service 的地址和端口信息
2. **DNS（推荐）**：通过 CoreDNS 解析服务名称。同一命名空间内可直接使用短名称（如 `myapp`），跨命名空间则需使用完整域名

## 多端口 Service

```yaml
apiVersion: v1
kind: Service
metadata:
  name: myapp
spec:
  selector:
    app: myapp
  ports:
    - name: http
      port: 80
      targetPort: 8080
    - name: grpc
      port: 9090
      targetPort: 9090
    - name: metrics
      port: 9100
      targetPort: 9100
```

当 Service 需要暴露多个端口时，每个端口必须指定 `name` 字段以便区分。
