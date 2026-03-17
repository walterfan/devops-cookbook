# Service Mesh 服务网格

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

## What is a Service Mesh?

A service mesh provides infrastructure-level features for service-to-service communication:

| Feature | Description |
|---------|-------------|
| **mTLS** | Automatic mutual TLS between services |
| **Traffic Management** | Routing, load balancing, retries, timeouts |
| **Observability** | Metrics, tracing, logging for all traffic |
| **Resilience** | Circuit breaking, fault injection |
| **Policy** | Rate limiting, access control |

## Istio vs Linkerd

| Feature | Istio | Linkerd |
|---------|-------|---------|
| Proxy | Envoy | linkerd2-proxy (Rust) |
| Complexity | High | Low |
| Resource usage | Higher | Lower |
| Features | Full-featured | Core features |
| Learning curve | Steep | Gentle |

## Istio Installation

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

## Traffic Management

### VirtualService (routing rules)

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

### DestinationRule (traffic policy)

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

### Gateway (external traffic)

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

## mTLS

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

## Observability

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

## Fault Injection (Testing)

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

## Linkerd (Lightweight Alternative)

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
