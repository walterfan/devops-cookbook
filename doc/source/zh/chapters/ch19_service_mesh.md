# 19. Service Mesh 服务网格

```{mermaid}
flowchart TB
    subgraph "Control Plane"
        Istiod[istiod<br/>Pilot + Citadel + Galley]
    end
    subgraph "Data Plane"
        subgraph PodA["Pod A"]
            AppA[App A] <--> ProxyA[Envoy Proxy]
        end
        subgraph PodB["Pod B"]
            AppB[App B] <--> ProxyB[Envoy Proxy]
        end
    end
    Istiod -->|config| ProxyA
    Istiod -->|config| ProxyB
    ProxyA <-->|mTLS| ProxyB
```

## 什么是服务网格(Service Mesh)？

服务网格在基础设施层面为服务间通信提供了一系列通用能力：

| 能力 | 说明 |
|------|------|
| **mTLS（双向 TLS）** | 服务间自动建立双向 TLS 加密通信 |
| **流量管理(Traffic Management)** | 路由、负载均衡、重试、超时 |
| **可观测性(Observability)** | 对所有流量进行指标采集、链路追踪、日志记录 |
| **弹性(Resilience)** | 熔断(Circuit Breaking)、故障注入(Fault Injection) |
| **策略(Policy)** | 速率限制、访问控制 |

## Istio 与 Linkerd 对比

| 特性 | Istio | Linkerd |
|------|-------|---------|
| 代理(Proxy) | Envoy | linkerd2-proxy（Rust 实现） |
| 复杂度 | 高 | 低 |
| 资源消耗 | 较高 | 较低 |
| 功能 | 功能全面 | 核心功能 |
| 学习曲线 | 陡峭 | 平缓 |

## Istio 安装

```bash
# Install istioctl
curl -L https://istio.io/downloadIstio | sh -
export PATH=$PWD/istio-1.21.0/bin:$PATH

# Install Istio (demo profile for learning)
istioctl install --set profile=demo -y

# Enable sidecar injection for a namespace
kubectl label namespace default istio-injection=enabled

# Verify
kubectl get pods -n istio-system
istioctl analyze
```

## 流量管理

### VirtualService（路由规则）

```yaml
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: myapp
spec:
  hosts:
    - myapp
  http:
    # Canary: 90% to v1, 10% to v2
    - route:
        - destination:
            host: myapp
            subset: v1
          weight: 90
        - destination:
            host: myapp
            subset: v2
          weight: 10
      retries:
        attempts: 3
        perTryTimeout: 2s
      timeout: 10s
```

### DestinationRule（流量策略）

```yaml
apiVersion: networking.istio.io/v1beta1
kind: DestinationRule
metadata:
  name: myapp
spec:
  host: myapp
  trafficPolicy:
    connectionPool:
      tcp:
        maxConnections: 100
      http:
        h2UpgradePolicy: DEFAULT
        http1MaxPendingRequests: 100
    outlierDetection:
      consecutive5xxErrors: 5
      interval: 30s
      baseEjectionTime: 30s
  subsets:
    - name: v1
      labels:
        version: v1
    - name: v2
      labels:
        version: v2
```

### Gateway（外部流量入口）

```yaml
apiVersion: networking.istio.io/v1beta1
kind: Gateway
metadata:
  name: myapp-gateway
spec:
  selector:
    istio: ingressgateway
  servers:
    - port:
        number: 443
        name: https
        protocol: HTTPS
      tls:
        mode: SIMPLE
        credentialName: myapp-tls
      hosts:
        - myapp.example.com
---
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: myapp-external
spec:
  hosts:
    - myapp.example.com
  gateways:
    - myapp-gateway
  http:
    - route:
        - destination:
            host: myapp
            port:
              number: 80
```

## mTLS 双向认证

```yaml
# Strict mTLS for a namespace
apiVersion: security.istio.io/v1beta1
kind: PeerAuthentication
metadata:
  name: default
  namespace: default
spec:
  mtls:
    mode: STRICT
```

## 可观测性

```bash
# Install addons
kubectl apply -f istio-1.21.0/samples/addons/

# Kiali (service mesh dashboard)
istioctl dashboard kiali

# Jaeger (distributed tracing)
istioctl dashboard jaeger

# Grafana (metrics)
istioctl dashboard grafana

# Prometheus
istioctl dashboard prometheus
```

## 故障注入(Fault Injection)测试

```yaml
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: myapp
spec:
  hosts:
    - myapp
  http:
    - fault:
        delay:
          percentage:
            value: 10
          fixedDelay: 5s
        abort:
          percentage:
            value: 5
          httpStatus: 500
      route:
        - destination:
            host: myapp
```

## Linkerd（轻量级替代方案）

```bash
# Install
curl --proto '=https' --tlsv1.2 -sSfL https://run.linkerd.io/install | sh
linkerd install --crds | kubectl apply -f -
linkerd install | kubectl apply -f -
linkerd check

# Inject sidecar
kubectl get deploy -o yaml | linkerd inject - | kubectl apply -f -

# Dashboard
linkerd viz install | kubectl apply -f -
linkerd viz dashboard
```
