# 28. 安全最佳实践

```{mermaid}
flowchart TB
    subgraph "Security Layers"
        L1[Image Security<br/>Scan, Sign, Minimal Base]
        L2[Container Security<br/>Non-root, Read-only FS, Seccomp]
        L3[K8s Security<br/>RBAC, PodSecurity, NetworkPolicy]
        L4[Pod AuthN/AuthZ<br/>ServiceAccount, IRSA, OIDC]
        L5[Runtime Security<br/>OPA/Gatekeeper, Falco]
        L6[Secrets Management<br/>Vault, Sealed Secrets]
    end
    L1 --> L2 --> L3 --> L4 --> L5 --> L6
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

## Pod 身份认证与授权 (AuthN / AuthZ)

Kubernetes 中 Pod 的安全访问控制涉及两个互补的体系：**Kubernetes RBAC** 控制 Pod 对集群内部资源的访问权限，**云平台 IAM** 控制 Pod 对云服务（如 S3、DynamoDB）的访问权限。传统做法是所有 Pod 共享 Node 的 IAM Role，这严重违反了最小权限原则。

```{mermaid}
sequenceDiagram
    autonumber
    participant Pod
    participant K8sAPI as K8s API Server
    participant OIDC as OIDC Identity Provider<br/>(K8s 集群公钥)
    participant STS as AWS STS
    participant S3 as AWS S3

    Note over Pod: 启动时由 kubelet 挂载<br/>Projected ServiceAccount Token

    Pod->>K8sAPI: 使用 SA Token 认证<br/>(RBAC 控制集群内权限)
    K8sAPI-->>Pod: 允许/拒绝集群操作

    Note over Pod: 需要访问 AWS 资源时

    Pod->>STS: sts:AssumeRoleWithWebIdentity<br/>(携带 SA Token + IAM Role ARN)
    STS->>OIDC: 验证 Token 签名<br/>(使用集群公钥 JWKS)
    OIDC-->>STS: Token 合法，确认 Pod 身份
    STS-->>Pod: 签发临时 AWS 凭证<br/>(AccessKey + SecretKey + SessionToken)
    Pod->>S3: 使用临时凭证访问 S3
    S3-->>Pod: 返回数据
```

### 问题：Node 级别权限共享

在传统模型中，EC2 节点上的所有 Pod 共享同一个 Instance Profile 的 IAM 权限。攻击者只需攻破任意一个 Pod 就能获取整个节点的云资源权限：

```text
┌─── EC2 Node (IAM Role: node-role) ───────────────┐
│                                                    │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐        │
│  │ Pod A    │  │ Pod B    │  │ Pod C    │        │
│  │ 只需读S3 │  │ 需写DDB  │  │ 无需AWS  │        │
│  └──────────┘  └──────────┘  └──────────┘        │
│                                                    │
│  所有 Pod 共享 node-role 的全部 IAM 权限 ⚠️        │
└────────────────────────────────────────────────────┘
```

### 解决方案：IAM Roles for Service Accounts (IRSA)

IRSA 将 **Pod 提升为 IAM 中的一等公民**，每个 Pod 通过自己的 ServiceAccount 获取独立的、最小权限的 IAM 凭证。其核心机制链路如下：

1. **ServiceAccount Token Volume Projection** — kubelet 利用集群私钥为 Pod 签发一个合法的 OIDC JWT Token，并以 Projected Volume 的形式挂载到 Pod 内
2. **OIDC Identity Provider** — 在 AWS 中注册 K8s 集群为 OpenID Connect Provider，AWS 使用集群公钥（JWKS 端点）验证上述 Token 的签名
3. **STS AssumeRoleWithWebIdentity** — Token 验证通过后，STS 签发临时 AWS 凭证（AccessKeyId / SecretAccessKey / SessionToken），Pod 凭此访问 AWS 资源
4. **IAM Trust Policy** — IAM Role 的信任策略限定了只有特定 namespace/ServiceAccount 才能 Assume 该 Role

```text
┌─── EC2 Node ─────────────────────────────────────────┐
│                                                       │
│  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐  │
│  │ Pod A        │ │ Pod B        │ │ Pod C        │  │
│  │ SA: s3-reader│ │ SA: ddb-write│ │ SA: default  │  │
│  │ → S3 只读    │ │ → DDB 读写   │ │ → 无 AWS 权限│  │
│  └──────────────┘ └──────────────┘ └──────────────┘  │
│                                                       │
│  每个 Pod 仅获取自身 ServiceAccount 绑定的 IAM 权限 ✅  │
└───────────────────────────────────────────────────────┘
```

### 第一步：启用 OIDC Provider

```bash
# 使用 eksctl 创建集群时自动启用 OIDC
eksctl create cluster --name my-cluster --region us-west-2

# 为已有集群关联 OIDC Provider
eksctl utils associate-iam-oidc-provider \
  --name my-cluster \
  --approve

# 验证 OIDC Provider
aws eks describe-cluster --name my-cluster \
  --query "cluster.identity.oidc.issuer" --output text
# 输出: https://oidc.eks.us-west-2.amazonaws.com/id/EXAMPLED539D4633E53DE1B71EXAMPLE
```

OIDC Provider 建立后，K8s API Server 会暴露两个关键端点：

- `/.well-known/openid-configuration` — OIDC 发现文档
- `/openid/v1/jwks` — JSON Web Key Set（集群公钥），供 STS 验证 Token 签名

### 第二步：创建 IAM Role 并绑定 ServiceAccount

```bash
# 一条命令完成 IAM Role + K8s ServiceAccount 的创建与绑定
eksctl create iamserviceaccount \
  --name s3-reader \
  --namespace production \
  --cluster my-cluster \
  --attach-policy-arn arn:aws:iam::aws:policy/AmazonS3ReadOnlyAccess \
  --approve
```

上述命令实际完成了两件事：

**1) 创建 IAM Role 并配置 Trust Policy：**

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Federated": "arn:aws:iam::ACCOUNT_ID:oidc-provider/oidc.eks.REGION.amazonaws.com/id/CLUSTER_ID"
      },
      "Action": "sts:AssumeRoleWithWebIdentity",
      "Condition": {
        "StringEquals": {
          "oidc.eks.REGION.amazonaws.com/id/CLUSTER_ID:sub": "system:serviceaccount:production:s3-reader",
          "oidc.eks.REGION.amazonaws.com/id/CLUSTER_ID:aud": "sts.amazonaws.com"
        }
      }
    }
  ]
}
```

Trust Policy 中的 `Condition` 确保了只有 `production` 命名空间下名为 `s3-reader` 的 ServiceAccount 才能 Assume 该 Role——这是 IRSA 安全模型的核心约束。

**2) 创建 K8s ServiceAccount 并添加 Annotation：**

```yaml
apiVersion: v1
kind: ServiceAccount
metadata:
  name: s3-reader
  namespace: production
  annotations:
    eks.amazonaws.com/role-arn: arn:aws:iam::123456789012:role/eksctl-my-cluster-addon-iamsa-production-s3-reader-Role1-XXXXX
```

### 第三步：Pod 使用 IRSA 访问 AWS 资源

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: data-processor
  namespace: production
spec:
  replicas: 2
  selector:
    matchLabels:
      app: data-processor
  template:
    metadata:
      labels:
        app: data-processor
    spec:
      serviceAccountName: s3-reader   # 引用带有 IAM Role 注解的 SA
      containers:
        - name: app
          image: registry.example.com/data-processor:1.0
          env:
            - name: AWS_DEFAULT_REGION
              value: us-west-2
          securityContext:
            readOnlyRootFilesystem: true
            runAsNonRoot: true
            runAsUser: 1001
            allowPrivilegeEscalation: false
            capabilities:
              drop:
                - ALL
```

EKS 的 Pod Identity Webhook（Mutating Admission Controller）会自动为 Pod 注入以下内容：

```yaml
# Webhook 自动注入的环境变量
env:
  - name: AWS_ROLE_ARN
    value: arn:aws:iam::123456789012:role/s3-reader-role
  - name: AWS_WEB_IDENTITY_TOKEN_FILE
    value: /var/run/secrets/eks.amazonaws.com/serviceaccount/token

# Webhook 自动注入的 Volume
volumes:
  - name: aws-iam-token
    projected:
      sources:
        - serviceAccountToken:
            audience: sts.amazonaws.com
            expirationSeconds: 86400
            path: token
volumeMounts:
  - mountPath: /var/run/secrets/eks.amazonaws.com/serviceaccount
    name: aws-iam-token
    readOnly: true
```

AWS SDK（Go/Java/Python/Node）会自动识别 `AWS_WEB_IDENTITY_TOKEN_FILE` 和 `AWS_ROLE_ARN` 环境变量，完成 `sts:AssumeRoleWithWebIdentity` 调用，应用代码无需任何修改。

### ServiceAccount Token 深入解析

Projected ServiceAccount Token 本质上是一个由 K8s API Server 签发的 OIDC JWT，与传统的 ServiceAccount Secret Token 有本质区别：

| 特性 | 传统 SA Secret Token | Projected SA Token (OIDC JWT) |
|------|----------------------|-------------------------------|
| 生命周期 | 永不过期，直到手动删除 | 有限期（默认 24h），自动轮转 |
| 绑定范围 | 绑定到 ServiceAccount | 绑定到特定 Pod + Node |
| 签名方式 | API Server 内部 | 集群私钥签名，可被外部 OIDC 验证 |
| 作废时机 | 需手动删除 Secret | Pod 删除后 60 秒内自动失效 |
| audience | 固定为 API Server | 可指定任意受众（如 `sts.amazonaws.com`） |

Token 的 JWT payload 示例：

```json
{
  "aud": ["sts.amazonaws.com"],
  "exp": 1731613413,
  "iat": 1700077413,
  "iss": "https://oidc.eks.us-west-2.amazonaws.com/id/CLUSTER_ID",
  "sub": "system:serviceaccount:production:s3-reader",
  "kubernetes.io": {
    "namespace": "production",
    "pod": {
      "name": "data-processor-7b8f9c6d4-x2k9m",
      "uid": "778a530c-b3f4-47c0-9cd5-ab018fb64f33"
    },
    "serviceaccount": {
      "name": "s3-reader",
      "uid": "a087d5a0-e1dd-43ec-93ac-f13d89cd13af"
    }
  }
}
```

`sub` 字段 `system:serviceaccount:production:s3-reader` 与 IAM Trust Policy 中的 Condition 匹配，STS 据此决定是否允许 Assume Role。

### kubelet 的 Token 轮转机制

kubelet 负责 Token 的生命周期管理：

- Token 到达 80% 有效期时主动轮转
- Token 超过 24 小时未刷新时强制轮转
- Pod 删除后 Token 在 60 秒内失效
- 应用应定期重新读取 Token 文件（建议每 5 分钟）

### 禁用不必要的 ServiceAccount Token 挂载

对于不需要访问 K8s API 和云资源的 Pod，应显式禁用 Token 自动挂载以收窄攻击面：

```yaml
# 方式一：在 ServiceAccount 级别禁用
apiVersion: v1
kind: ServiceAccount
metadata:
  name: no-api-access
automountServiceAccountToken: false

---
# 方式二：在 Pod 级别禁用（优先级更高）
apiVersion: v1
kind: Pod
metadata:
  name: static-web
spec:
  serviceAccountName: default
  automountServiceAccountToken: false
  containers:
    - name: nginx
      image: nginx:1.27-alpine
```

### RBAC 与 IRSA 协同：完整的最小权限模型

一个同时需要访问 K8s API 和 AWS 资源的 Pod，其权限应从两个维度配置：

```yaml
# K8s RBAC：控制 Pod 对集群资源的访问
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  name: configmap-reader
  namespace: production
rules:
  - apiGroups: [""]
    resources: ["configmaps"]
    verbs: ["get", "list", "watch"]
---
apiVersion: rbac.authorization.k8s.io/v1
kind: RoleBinding
metadata:
  name: data-processor-cm-reader
  namespace: production
subjects:
  - kind: ServiceAccount
    name: s3-reader
    namespace: production
roleRef:
  kind: Role
  name: configmap-reader
  apiGroup: rbac.authorization.k8s.io
```

```text
                    data-processor Pod
                    (SA: s3-reader)
                          │
              ┌───────────┴───────────┐
              ▼                       ▼
     K8s RBAC (集群内)         AWS IAM (云资源)
     ─────────────────        ────────────────
     Role: configmap-reader   Policy: S3ReadOnly
     → configmaps: get/list   → s3:GetObject
     → 其他资源: 拒绝          → 其他服务: 拒绝
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
