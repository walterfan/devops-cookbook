# 28. 安全最佳实践

```{mermaid}
flowchart TB
    subgraph "Security Layers"
        L1[Image Security<br/>Scan, Sign, Minimal Base]
        L2[Container Security<br/>Non-root, Read-only FS, Seccomp]
        L3[K8s Security<br/>RBAC, PodSecurity, NetworkPolicy]
        L4[Runtime Security<br/>OPA/Gatekeeper, Falco]
        L5[Secrets Management<br/>Vault, Sealed Secrets]
    end
    L1 --> L2 --> L3 --> L4 --> L5
```

## 容器安全

### 非 root 用户运行

```dockerfile
# Dockerfile
FROM python:3.12-slim
RUN groupadd -r app && useradd -r -g app -d /app -s /sbin/nologin app
WORKDIR /app
COPY --chown=app:app . .
USER app
```

### 只读文件系统

```yaml
spec:
  containers:
    - name: app
      securityContext:
        readOnlyRootFilesystem: true
        runAsNonRoot: true
        runAsUser: 1001
        allowPrivilegeEscalation: false
        capabilities:
          drop:
            - ALL
      volumeMounts:
        - name: tmp
          mountPath: /tmp
  volumes:
    - name: tmp
      emptyDir: {}
```

## Pod 安全标准

Kubernetes 提供了 Pod Security Standards（Pod 安全标准），通过在命名空间(Namespace)上设置标签来强制执行安全策略：

```yaml
# Enforce restricted policy on namespace
apiVersion: v1
kind: Namespace
metadata:
  name: production
  labels:
    pod-security.kubernetes.io/enforce: restricted
    pod-security.kubernetes.io/audit: restricted
    pod-security.kubernetes.io/warn: restricted
```

### 受限 Pod 示例

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: secure-pod
spec:
  securityContext:
    runAsNonRoot: true
    seccompProfile:
      type: RuntimeDefault
  containers:
    - name: app
      image: myapp:1.0
      securityContext:
        allowPrivilegeEscalation: false
        readOnlyRootFilesystem: true
        runAsUser: 1001
        capabilities:
          drop:
            - ALL
      resources:
        limits:
          cpu: "500m"
          memory: "256Mi"
```

## RBAC 权限控制

RBAC（基于角色的访问控制）是 Kubernetes 中管理权限的核心机制。应遵循最小权限原则(Principle of Least Privilege)进行配置：

```yaml
# Role: read-only access to pods in a namespace
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  name: pod-reader
  namespace: production
rules:
  - apiGroups: [""]
    resources: ["pods", "pods/log"]
    verbs: ["get", "list", "watch"]
  - apiGroups: ["apps"]
    resources: ["deployments"]
    verbs: ["get", "list"]
---
apiVersion: rbac.authorization.k8s.io/v1
kind: RoleBinding
metadata:
  name: dev-pod-reader
  namespace: production
subjects:
  - kind: Group
    name: developers
    apiGroup: rbac.authorization.k8s.io
roleRef:
  kind: Role
  name: pod-reader
  apiGroup: rbac.authorization.k8s.io
---
# ClusterRole for cluster-wide access
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRole
metadata:
  name: namespace-admin
rules:
  - apiGroups: [""]
    resources: ["namespaces"]
    verbs: ["get", "list"]
  - apiGroups: ["apps"]
    resources: ["deployments", "statefulsets"]
    verbs: ["*"]
```

## 网络策略

NetworkPolicy（网络策略）用于控制 Pod 之间以及 Pod 与外部之间的网络流量。生产环境建议采用"默认拒绝，显式放行"的策略：

```yaml
# Default deny all ingress
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: default-deny
  namespace: production
spec:
  podSelector: {}
  policyTypes:
    - Ingress
    - Egress
---
# Allow web → api → db
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: allow-api-to-db
  namespace: production
spec:
  podSelector:
    matchLabels:
      app: db
  ingress:
    - from:
        - podSelector:
            matchLabels:
              app: api
      ports:
        - port: 5432
---
# Allow egress to DNS
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: allow-dns
  namespace: production
spec:
  podSelector: {}
  policyTypes:
    - Egress
  egress:
    - to: []
      ports:
        - port: 53
          protocol: UDP
        - port: 53
          protocol: TCP
```

## OPA Gatekeeper 策略引擎

OPA Gatekeeper 是一个基于 Open Policy Agent 的 Kubernetes 准入控制器(Admission Controller)，可以在资源创建时强制执行自定义策略：

```bash
# Install Gatekeeper
helm repo add gatekeeper https://open-policy-agent.github.io/gatekeeper/charts
helm install gatekeeper gatekeeper/gatekeeper -n gatekeeper-system --create-namespace
```

```yaml
# Constraint Template: require labels
apiVersion: templates.gatekeeper.sh/v1
kind: ConstraintTemplate
metadata:
  name: k8srequiredlabels
spec:
  crd:
    spec:
      names:
        kind: K8sRequiredLabels
      validation:
        openAPIV3Schema:
          type: object
          properties:
            labels:
              type: array
              items:
                type: string
  targets:
    - target: admission.k8s.gatekeeper.sh
      rego: |
        package k8srequiredlabels
        violation[{"msg": msg}] {
          provided := {label | input.review.object.metadata.labels[label]}
          required := {label | label := input.parameters.labels[_]}
          missing := required - provided
          count(missing) > 0
          msg := sprintf("Missing required labels: %v", [missing])
        }
---
# Constraint: all deployments must have "app" and "owner" labels
apiVersion: constraints.gatekeeper.sh/v1beta1
kind: K8sRequiredLabels
metadata:
  name: require-labels
spec:
  match:
    kinds:
      - apiGroups: ["apps"]
        kinds: ["Deployment"]
  parameters:
    labels:
      - "app"
      - "owner"
```

## CI 中的镜像扫描

在 CI 流水线中集成镜像漏洞扫描，可以在部署前发现并阻止存在严重漏洞的镜像进入生产环境：

```yaml
# GitHub Actions
- name: Scan image
  uses: aquasecurity/trivy-action@master
  with:
    image-ref: myapp:${{ github.sha }}
    format: 'table'
    exit-code: '1'
    severity: 'CRITICAL,HIGH'
    ignore-unfixed: true
```

## 供应链安全

软件供应链安全(Supply Chain Security)关注从代码到部署的整个链路的完整性和可信度。核心实践包括生成 SBOM（软件物料清单）和对镜像进行签名：

```bash
# Generate SBOM
syft myapp:1.0 -o spdx-json > sbom.json

# Sign image with cosign
cosign sign --key cosign.key registry.example.com/myapp:1.0

# Verify in admission controller (Kyverno)
```

```yaml
# Kyverno policy: require signed images
apiVersion: kyverno.io/v1
kind: ClusterPolicy
metadata:
  name: verify-image-signature
spec:
  validationFailureAction: Enforce
  rules:
    - name: verify-signature
      match:
        any:
          - resources:
              kinds:
                - Pod
      verifyImages:
        - imageReferences:
            - "registry.example.com/*"
          attestors:
            - entries:
                - keys:
                    publicKeys: |-
                      -----BEGIN PUBLIC KEY-----
                      ...
                      -----END PUBLIC KEY-----
```
