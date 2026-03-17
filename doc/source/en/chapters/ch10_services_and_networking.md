# 10. Services and Networking

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

## Service Types

### ClusterIP (default)

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

### Headless Service

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

## DNS in Kubernetes

CoreDNS provides DNS resolution within the cluster:

| Record | Format | Example |
|--------|--------|---------|
| Service | `<svc>.<ns>.svc.cluster.local` | `myapp.default.svc.cluster.local` |
| Pod | `<pod-ip>.<ns>.pod.cluster.local` | `10-244-1-5.default.pod.cluster.local` |
| StatefulSet Pod | `<pod>.<svc>.<ns>.svc.cluster.local` | `mysql-0.mysql-headless.default.svc.cluster.local` |

```bash
# Test DNS from a debug pod
kubectl run dns-test --rm -it --image=busybox -- nslookup myapp.default.svc.cluster.local
```

## NetworkPolicy

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

## Service Discovery Patterns

```bash
# 1. Environment variables (auto-injected)
# MYAPP_SERVICE_HOST=10.96.0.10
# MYAPP_SERVICE_PORT=80

# 2. DNS (recommended)
# curl http://myapp.default.svc.cluster.local

# 3. Short DNS (within same namespace)
# curl http://myapp
```

## Multi-port Service

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
