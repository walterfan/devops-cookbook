# 28. Security Best Practices

```{mermaid}
flowchart TB
    subgraph Supply["Supply Chain Security"]
        SBOM[SBOM Generation<br/>Syft]
        Sign[Image Signing<br/>Cosign / Sigstore]
        Verify[Admission Verification<br/>Kyverno / Connaisseur]
    end
    subgraph Image["Image Security"]
        Minimal[Minimal Base Image<br/>distroless / scratch]
        Scan[Vulnerability Scanning<br/>Trivy / Grype]
        NoRoot[Non-root User]
    end
    subgraph Container["Container Runtime Security"]
        RO[Read-only Filesystem]
        Seccomp[Seccomp Profile]
        AppArmor[AppArmor Profile]
        NoCap[Drop ALL Capabilities]
    end
    subgraph K8s["Kubernetes Security"]
        RBAC[RBAC<br/>Least Privilege]
        PSS[Pod Security Standards<br/>restricted]
        NetPol[Network Policy<br/>Default Deny]
        OPA[OPA Gatekeeper<br/>Policy Enforcement]
    end
    subgraph PodAuth["Pod AuthN/AuthZ"]
        SA[ServiceAccount<br/>Token Projection]
        IRSA[IAM Roles for<br/>Service Accounts]
        OIDC_P[OIDC Identity<br/>Provider]
    end
    subgraph Secrets["Secrets Management"]
        Vault[HashiCorp Vault]
        Sealed[Sealed Secrets]
        ESO[External Secrets Operator]
    end
    Supply --> Image --> Container --> K8s --> PodAuth --> Secrets
```

## Container Security

Container security starts at build time. A secure container image minimizes the attack surface and runs with the least privileges necessary.

### Non-root User

Never run containers as root. Create a dedicated user in the Dockerfile:

```dockerfile
# Python example
FROM python:3.12-slim AS builder
WORKDIR /app
COPY requirements.txt .
RUN pip install --no-cache-dir --prefix=/install -r requirements.txt

FROM python:3.12-slim
RUN groupadd -r app && useradd -r -g app -d /app -s /sbin/nologin app
WORKDIR /app
COPY --from=builder /install /usr/local
COPY --chown=app:app . .
USER app
EXPOSE 8080
CMD ["python", "-m", "uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8080"]
```

```dockerfile
# Go example — scratch image (no shell, no user database)
FROM golang:1.22-alpine AS builder
WORKDIR /app
COPY . .
RUN CGO_ENABLED=0 go build -ldflags="-s -w" -o /server .

FROM scratch
COPY --from=builder /etc/ssl/certs/ca-certificates.crt /etc/ssl/certs/
COPY --from=builder /server /server
USER 65534:65534    # nobody user
EXPOSE 8080
ENTRYPOINT ["/server"]
```

### Read-only Filesystem and Security Context

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: secure-app
spec:
  replicas: 3
  selector:
    matchLabels:
      app: secure-app
  template:
    metadata:
      labels:
        app: secure-app
    spec:
      securityContext:
        runAsNonRoot: true
        runAsUser: 1001
        runAsGroup: 1001
        fsGroup: 1001
        seccompProfile:
          type: RuntimeDefault
      containers:
        - name: app
          image: registry.example.com/secure-app:1.0.0
          ports:
            - containerPort: 8080
          securityContext:
            readOnlyRootFilesystem: true
            allowPrivilegeEscalation: false
            capabilities:
              drop:
                - ALL
            # Only add specific capabilities if absolutely needed:
            # capabilities:
            #   add: ["NET_BIND_SERVICE"]  # for binding to ports < 1024
          volumeMounts:
            - name: tmp
              mountPath: /tmp
            - name: cache
              mountPath: /app/.cache
          resources:
            requests:
              cpu: "100m"
              memory: "128Mi"
            limits:
              cpu: "500m"
              memory: "256Mi"
      volumes:
        - name: tmp
          emptyDir:
            sizeLimit: 100Mi
        - name: cache
          emptyDir:
            sizeLimit: 50Mi
      automountServiceAccountToken: false    # disable if not needed
```

### Seccomp and AppArmor Profiles

```yaml
# Custom seccomp profile (restrict syscalls)
# Place at /var/lib/kubelet/seccomp/profiles/restricted.json on each node
{
  "defaultAction": "SCMP_ACT_ERRNO",
  "architectures": ["SCMP_ARCH_X86_64"],
  "syscalls": [
    {
      "names": [
        "accept4", "access", "arch_prctl", "bind", "brk", "clone",
        "close", "connect", "epoll_create1", "epoll_ctl", "epoll_pwait",
        "execve", "exit", "exit_group", "fcntl", "fstat", "futex",
        "getdents64", "getpid", "getsockname", "getsockopt", "listen",
        "madvise", "mmap", "mprotect", "munmap", "nanosleep", "newfstatat",
        "openat", "pipe2", "pread64", "read", "recvfrom", "rt_sigaction",
        "rt_sigprocmask", "rt_sigreturn", "sched_getaffinity", "sched_yield",
        "sendto", "set_robust_list", "set_tid_address", "setsockopt",
        "sigaltstack", "socket", "tgkill", "write", "writev"
      ],
      "action": "SCMP_ACT_ALLOW"
    }
  ]
}
```

```yaml
# Pod using custom seccomp profile
spec:
  securityContext:
    seccompProfile:
      type: Localhost
      localhostProfile: profiles/restricted.json
  containers:
    - name: app
      # AppArmor annotation (per-container)
      # Note: AppArmor uses annotations, not securityContext
  # AppArmor annotation
  metadata:
    annotations:
      container.apparmor.security.beta.kubernetes.io/app: runtime/default
```

## Pod Security Standards

Kubernetes Pod Security Standards (PSS) replace the deprecated PodSecurityPolicy. They define three levels: `privileged`, `baseline`, and `restricted`.

```yaml
# Enforce restricted policy on namespace
apiVersion: v1
kind: Namespace
metadata:
  name: production
  labels:
    # Enforce: reject pods that violate the policy
    pod-security.kubernetes.io/enforce: restricted
    pod-security.kubernetes.io/enforce-version: latest
    # Audit: log violations but allow
    pod-security.kubernetes.io/audit: restricted
    pod-security.kubernetes.io/audit-version: latest
    # Warn: show warnings to users
    pod-security.kubernetes.io/warn: restricted
    pod-security.kubernetes.io/warn-version: latest
```

### Compliant Pod (Passes `restricted` Level)

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: compliant-pod
  namespace: production
spec:
  securityContext:
    runAsNonRoot: true
    runAsUser: 1001
    fsGroup: 1001
    seccompProfile:
      type: RuntimeDefault
  containers:
    - name: app
      image: registry.example.com/myapp:1.0.0
      securityContext:
        allowPrivilegeEscalation: false
        readOnlyRootFilesystem: true
        runAsNonRoot: true
        capabilities:
          drop:
            - ALL
      ports:
        - containerPort: 8080
      resources:
        limits:
          cpu: "500m"
          memory: "256Mi"
        requests:
          cpu: "100m"
          memory: "128Mi"
  automountServiceAccountToken: false
```

### Verify PSS Compliance

```bash
# Dry-run to check if a pod would be admitted
kubectl label --dry-run=server --overwrite ns production \
  pod-security.kubernetes.io/enforce=restricted

# Check existing violations in a namespace
kubectl get pods -n production -o json | \
  kubectl apply --dry-run=server -f - 2>&1 | grep -i "forbidden"
```

## RBAC (Role-Based Access Control)

Follow the principle of least privilege. Never grant `cluster-admin` to applications or developers.

```yaml
# Namespace-scoped Role: read-only access to pods and logs
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  name: pod-reader
  namespace: production
rules:
  - apiGroups: [""]
    resources: ["pods", "pods/log", "pods/status"]
    verbs: ["get", "list", "watch"]
  - apiGroups: ["apps"]
    resources: ["deployments", "replicasets"]
    verbs: ["get", "list"]
  - apiGroups: [""]
    resources: ["events"]
    verbs: ["list"]
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
# Deployer role: can manage deployments but not secrets
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  name: deployer
  namespace: production
rules:
  - apiGroups: ["apps"]
    resources: ["deployments", "replicasets"]
    verbs: ["get", "list", "watch", "update", "patch"]
  - apiGroups: [""]
    resources: ["pods", "services", "configmaps"]
    verbs: ["get", "list", "watch"]
  - apiGroups: [""]
    resources: ["pods/exec", "pods/portforward"]
    verbs: []    # explicitly deny
---
# Service account for CI/CD (scoped to specific namespace)
apiVersion: v1
kind: ServiceAccount
metadata:
  name: ci-deployer
  namespace: production
---
apiVersion: rbac.authorization.k8s.io/v1
kind: RoleBinding
metadata:
  name: ci-deployer-binding
  namespace: production
subjects:
  - kind: ServiceAccount
    name: ci-deployer
    namespace: production
roleRef:
  kind: Role
  name: deployer
  apiGroup: rbac.authorization.k8s.io
```

### RBAC Audit Commands

```bash
# Check who can perform an action
kubectl auth can-i create deployments --namespace production --as developer@example.com
kubectl auth can-i delete secrets --namespace production --as system:serviceaccount:production:ci-deployer

# List all roles and bindings
kubectl get roles,rolebindings -n production
kubectl get clusterroles,clusterrolebindings

# Find overly permissive roles
kubectl get clusterrolebindings -o json | \
  jq '.items[] | select(.roleRef.name == "cluster-admin") | .subjects[]'
```

## Pod Authentication & Authorization (AuthN / AuthZ)

Access control for Pods in Kubernetes involves two complementary systems: **Kubernetes RBAC** governs Pod access to in-cluster resources, while **cloud IAM** governs Pod access to cloud services (e.g., S3, DynamoDB). The traditional approach — all Pods sharing the Node's IAM Role — is a serious violation of the principle of least privilege.

```{mermaid}
sequenceDiagram
    autonumber
    participant Pod
    participant K8sAPI as K8s API Server
    participant OIDC as OIDC Identity Provider<br/>(K8s cluster public key)
    participant STS as AWS STS
    participant S3 as AWS S3

    Note over Pod: On startup, kubelet mounts<br/>Projected ServiceAccount Token

    Pod->>K8sAPI: Authenticate with SA Token<br/>(RBAC governs in-cluster access)
    K8sAPI-->>Pod: Allow / Deny cluster operations

    Note over Pod: When accessing AWS resources

    Pod->>STS: sts:AssumeRoleWithWebIdentity<br/>(SA Token + IAM Role ARN)
    STS->>OIDC: Verify token signature<br/>(using cluster JWKS public key)
    OIDC-->>STS: Token valid, Pod identity confirmed
    STS-->>Pod: Issue temporary AWS credentials<br/>(AccessKey + SecretKey + SessionToken)
    Pod->>S3: Access S3 with temporary credentials
    S3-->>Pod: Return data
```

### The Problem: Node-Level Permission Sharing

In the traditional model, every Pod on an EC2 node shares the same Instance Profile IAM permissions. An attacker who compromises any single Pod gains access to all cloud resources available to the entire node:

```text
┌─── EC2 Node (IAM Role: node-role) ───────────────┐
│                                                    │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐        │
│  │ Pod A    │  │ Pod B    │  │ Pod C    │        │
│  │ needs S3 │  │ needs DDB│  │ no AWS   │        │
│  │ read     │  │ write    │  │ access   │        │
│  └──────────┘  └──────────┘  └──────────┘        │
│                                                    │
│  All Pods share ALL IAM permissions of node-role ⚠️│
└────────────────────────────────────────────────────┘
```

### The Solution: IAM Roles for Service Accounts (IRSA)

IRSA makes **Pods first-class citizens in IAM**. Each Pod obtains independent, least-privilege IAM credentials through its own ServiceAccount. The mechanism chain works as follows:

1. **ServiceAccount Token Volume Projection** — kubelet uses the cluster's private key to sign an OIDC-compliant JWT for the Pod and mounts it as a projected volume
2. **OIDC Identity Provider** — The K8s cluster is registered as an OpenID Connect Provider in AWS. AWS uses the cluster's public key (JWKS endpoint) to verify the token signature
3. **STS AssumeRoleWithWebIdentity** — Once the token is verified, STS issues temporary AWS credentials (AccessKeyId / SecretAccessKey / SessionToken) for the Pod
4. **IAM Trust Policy** — The trust policy on the IAM Role restricts which namespace/ServiceAccount combinations are allowed to assume it

```text
┌─── EC2 Node ─────────────────────────────────────────┐
│                                                       │
│  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐  │
│  │ Pod A        │ │ Pod B        │ │ Pod C        │  │
│  │ SA: s3-reader│ │ SA: ddb-write│ │ SA: default  │  │
│  │ → S3 read    │ │ → DDB r/w   │ │ → no AWS     │  │
│  └──────────────┘ └──────────────┘ └──────────────┘  │
│                                                       │
│  Each Pod gets ONLY the IAM permissions of its SA ✅   │
└───────────────────────────────────────────────────────┘
```

### Step 1: Enable the OIDC Provider

```bash
# eksctl enables OIDC automatically when creating a cluster
eksctl create cluster --name my-cluster --region us-west-2

# Associate OIDC provider with an existing cluster
eksctl utils associate-iam-oidc-provider \
  --name my-cluster \
  --approve

# Verify the OIDC provider
aws eks describe-cluster --name my-cluster \
  --query "cluster.identity.oidc.issuer" --output text
# Output: https://oidc.eks.us-west-2.amazonaws.com/id/EXAMPLED539D4633E53DE1B71EXAMPLE
```

Once established, the K8s API Server exposes two critical endpoints:

- `/.well-known/openid-configuration` — OIDC discovery document
- `/openid/v1/jwks` — JSON Web Key Set (cluster public keys) used by STS to verify token signatures

### Step 2: Create IAM Role and Bind to ServiceAccount

```bash
# Single command to create both the IAM Role and K8s ServiceAccount
eksctl create iamserviceaccount \
  --name s3-reader \
  --namespace production \
  --cluster my-cluster \
  --attach-policy-arn arn:aws:iam::aws:policy/AmazonS3ReadOnlyAccess \
  --approve
```

This command performs two actions under the hood:

**1) Creates an IAM Role with a Trust Policy:**

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

The `Condition` in the Trust Policy ensures that only the ServiceAccount named `s3-reader` in the `production` namespace can assume this role — this is the core security constraint of IRSA.

**2) Creates a K8s ServiceAccount with an IAM Role annotation:**

```yaml
apiVersion: v1
kind: ServiceAccount
metadata:
  name: s3-reader
  namespace: production
  annotations:
    eks.amazonaws.com/role-arn: arn:aws:iam::123456789012:role/eksctl-my-cluster-addon-iamsa-production-s3-reader-Role1-XXXXX
```

### Step 3: Configure Pods to Use IRSA

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
      serviceAccountName: s3-reader   # Reference the SA with IAM Role annotation
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

The EKS Pod Identity Webhook (a Mutating Admission Controller) automatically injects the following into the Pod:

```yaml
# Environment variables injected by the webhook
env:
  - name: AWS_ROLE_ARN
    value: arn:aws:iam::123456789012:role/s3-reader-role
  - name: AWS_WEB_IDENTITY_TOKEN_FILE
    value: /var/run/secrets/eks.amazonaws.com/serviceaccount/token

# Projected volume injected by the webhook
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

AWS SDKs (Go / Java / Python / Node) automatically detect the `AWS_WEB_IDENTITY_TOKEN_FILE` and `AWS_ROLE_ARN` environment variables and perform the `sts:AssumeRoleWithWebIdentity` call transparently — no application code changes required.

### ServiceAccount Token Deep Dive

The Projected ServiceAccount Token is an OIDC-compliant JWT signed by the K8s API Server. It differs fundamentally from the legacy ServiceAccount Secret token:

| Property | Legacy SA Secret Token | Projected SA Token (OIDC JWT) |
|----------|------------------------|-------------------------------|
| Lifetime | Never expires until manually deleted | Time-bound (default 24h), auto-rotated |
| Binding scope | Bound to ServiceAccount only | Bound to specific Pod + Node |
| Signing | API Server internal | Cluster private key; externally verifiable via OIDC |
| Invalidation | Must manually delete Secret | Auto-invalidated 60s after Pod deletion |
| Audience | Fixed to API Server | Configurable (e.g., `sts.amazonaws.com`) |

Example JWT payload:

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

The `sub` field `system:serviceaccount:production:s3-reader` is matched against the IAM Trust Policy `Condition` to determine whether the Pod is authorized to assume the role.

### kubelet Token Rotation

kubelet manages the full token lifecycle:

- Proactively rotates the token when it reaches 80% of its TTL
- Forces rotation if the token is older than 24 hours
- Token is invalidated within 60 seconds after Pod deletion
- Applications should periodically re-read the token file (every 5 minutes is recommended)

### Disable Unnecessary ServiceAccount Token Mounts

For Pods that don't need access to the K8s API or cloud resources, explicitly disable token automounting to reduce the attack surface:

```yaml
# Option 1: Disable at the ServiceAccount level
apiVersion: v1
kind: ServiceAccount
metadata:
  name: no-api-access
automountServiceAccountToken: false

---
# Option 2: Disable at the Pod level (takes precedence)
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

### RBAC + IRSA Together: Complete Least-Privilege Model

A Pod that needs access to both K8s API resources and AWS services should have permissions configured along both axes:

```yaml
# K8s RBAC: governs access to in-cluster resources
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
     K8s RBAC (in-cluster)     AWS IAM (cloud)
     ─────────────────────     ────────────────
     Role: configmap-reader    Policy: S3ReadOnly
     → configmaps: get/list    → s3:GetObject
     → everything else: deny   → everything else: deny
```

## NetworkPolicy

Network policies implement microsegmentation — default deny all traffic, then explicitly allow only what's needed.

```yaml
# 1. Default deny ALL ingress and egress in the namespace
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: default-deny-all
  namespace: production
spec:
  podSelector: {}
  policyTypes:
    - Ingress
    - Egress
---
# 2. Allow DNS resolution (required for all pods)
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
    - to:
        - namespaceSelector:
            matchLabels:
              kubernetes.io/metadata.name: kube-system
          podSelector:
            matchLabels:
              k8s-app: kube-dns
      ports:
        - port: 53
          protocol: UDP
        - port: 53
          protocol: TCP
---
# 3. Allow ingress-nginx → frontend
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: allow-ingress-to-frontend
  namespace: production
spec:
  podSelector:
    matchLabels:
      app: frontend
  policyTypes:
    - Ingress
  ingress:
    - from:
        - namespaceSelector:
            matchLabels:
              kubernetes.io/metadata.name: ingress-nginx
      ports:
        - port: 8080
          protocol: TCP
---
# 4. Allow frontend → api
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: allow-frontend-to-api
  namespace: production
spec:
  podSelector:
    matchLabels:
      app: api
  policyTypes:
    - Ingress
  ingress:
    - from:
        - podSelector:
            matchLabels:
              app: frontend
      ports:
        - port: 8080
          protocol: TCP
---
# 5. Allow api → database
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: allow-api-to-db
  namespace: production
spec:
  podSelector:
    matchLabels:
      app: postgres
  policyTypes:
    - Ingress
  ingress:
    - from:
        - podSelector:
            matchLabels:
              app: api
      ports:
        - port: 5432
          protocol: TCP
---
# 6. Allow api egress to external APIs (specific CIDR)
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: allow-api-external
  namespace: production
spec:
  podSelector:
    matchLabels:
      app: api
  policyTypes:
    - Egress
  egress:
    - to:
        - ipBlock:
            cidr: 0.0.0.0/0
            except:
              - 10.0.0.0/8
              - 172.16.0.0/12
              - 192.168.0.0/16
      ports:
        - port: 443
          protocol: TCP
```

## OPA Gatekeeper

OPA (Open Policy Agent) Gatekeeper enforces custom policies as admission webhooks. It prevents non-compliant resources from being created.

```bash
# Install Gatekeeper
helm repo add gatekeeper https://open-policy-agent.github.io/gatekeeper/charts
helm install gatekeeper gatekeeper/gatekeeper \
  -n gatekeeper-system --create-namespace \
  --set replicas=3 \
  --set audit.replicas=2
```

### Constraint Template: Require Resource Limits

```yaml
apiVersion: templates.gatekeeper.sh/v1
kind: ConstraintTemplate
metadata:
  name: k8srequiredresources
spec:
  crd:
    spec:
      names:
        kind: K8sRequiredResources
  targets:
    - target: admission.k8s.gatekeeper.sh
      rego: |
        package k8srequiredresources

        violation[{"msg": msg}] {
          container := input.review.object.spec.containers[_]
          not container.resources.limits.cpu
          msg := sprintf("Container '%v' must have CPU limits", [container.name])
        }

        violation[{"msg": msg}] {
          container := input.review.object.spec.containers[_]
          not container.resources.limits.memory
          msg := sprintf("Container '%v' must have memory limits", [container.name])
        }

        violation[{"msg": msg}] {
          container := input.review.object.spec.containers[_]
          not container.resources.requests.cpu
          msg := sprintf("Container '%v' must have CPU requests", [container.name])
        }

        violation[{"msg": msg}] {
          container := input.review.object.spec.containers[_]
          not container.resources.requests.memory
          msg := sprintf("Container '%v' must have memory requests", [container.name])
        }
---
apiVersion: constraints.gatekeeper.sh/v1beta1
kind: K8sRequiredResources
metadata:
  name: require-resource-limits
spec:
  match:
    kinds:
      - apiGroups: ["apps"]
        kinds: ["Deployment", "StatefulSet", "DaemonSet"]
    namespaces: ["production", "staging"]
  enforcementAction: deny
```

### Constraint Template: Block Latest Tag

```yaml
apiVersion: templates.gatekeeper.sh/v1
kind: ConstraintTemplate
metadata:
  name: k8sblocklatesttag
spec:
  crd:
    spec:
      names:
        kind: K8sBlockLatestTag
  targets:
    - target: admission.k8s.gatekeeper.sh
      rego: |
        package k8sblocklatesttag

        violation[{"msg": msg}] {
          container := input.review.object.spec.containers[_]
          endswith(container.image, ":latest")
          msg := sprintf("Container '%v' uses ':latest' tag — use a specific version", [container.name])
        }

        violation[{"msg": msg}] {
          container := input.review.object.spec.containers[_]
          not contains(container.image, ":")
          msg := sprintf("Container '%v' has no tag — use a specific version", [container.name])
        }
---
apiVersion: constraints.gatekeeper.sh/v1beta1
kind: K8sBlockLatestTag
metadata:
  name: block-latest-tag
spec:
  match:
    kinds:
      - apiGroups: ["apps"]
        kinds: ["Deployment", "StatefulSet"]
    namespaces: ["production"]
```

### Constraint Template: Require Labels

```yaml
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
apiVersion: constraints.gatekeeper.sh/v1beta1
kind: K8sRequiredLabels
metadata:
  name: require-standard-labels
spec:
  match:
    kinds:
      - apiGroups: ["apps"]
        kinds: ["Deployment"]
  parameters:
    labels:
      - "app.kubernetes.io/name"
      - "app.kubernetes.io/version"
      - "app.kubernetes.io/managed-by"
      - "owner"
```

## Image Scanning with Trivy

```bash
# Scan a container image
trivy image --severity HIGH,CRITICAL registry.example.com/myapp:1.0.0

# Scan and fail on critical vulnerabilities
trivy image --exit-code 1 --severity CRITICAL --ignore-unfixed myapp:latest

# Scan filesystem (source code dependencies)
trivy fs --severity HIGH,CRITICAL .

# Scan Kubernetes cluster
trivy k8s --report summary cluster

# Scan a Helm chart
trivy config ./chart/

# Generate SBOM
trivy image --format spdx-json --output sbom.json myapp:1.0.0
```

```yaml
# CI integration — GitHub Actions
- name: Scan image with Trivy
  uses: aquasecurity/trivy-action@master
  with:
    image-ref: ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}:${{ github.sha }}
    format: 'sarif'
    output: 'trivy-results.sarif'
    severity: 'CRITICAL,HIGH'
    exit-code: '1'
    ignore-unfixed: true

- name: Upload scan results to GitHub Security
  uses: github/codeql-action/upload-sarif@v3
  if: always()
  with:
    sarif_file: 'trivy-results.sarif'
```

## Secrets Management

### Sealed Secrets (GitOps-friendly)

Sealed Secrets encrypts secrets so they can be safely stored in Git.

```bash
# Install sealed-secrets controller
helm repo add sealed-secrets https://bitnami-labs.github.io/sealed-secrets
helm install sealed-secrets sealed-secrets/sealed-secrets -n kube-system

# Install kubeseal CLI
brew install kubeseal

# Create a regular secret, then seal it
kubectl create secret generic db-credentials \
  --from-literal=username=admin \
  --from-literal=password=s3cur3p@ss \
  --dry-run=client -o yaml | \
  kubeseal --format yaml > sealed-db-credentials.yaml

# The sealed secret can be committed to Git safely
cat sealed-db-credentials.yaml
```

```yaml
# sealed-db-credentials.yaml (safe to commit to Git)
apiVersion: bitnami.com/v1alpha1
kind: SealedSecret
metadata:
  name: db-credentials
  namespace: production
spec:
  encryptedData:
    username: AgBy3i4OJSWK+PiTySYZZA9rO43cGDEq...
    password: AgCtr8OJSWK+PiTySYZZA9rO43cGDEq...
  template:
    metadata:
      name: db-credentials
      namespace: production
```

### External Secrets Operator (Vault / AWS / GCP)

```bash
# Install External Secrets Operator
helm repo add external-secrets https://charts.external-secrets.io
helm install external-secrets external-secrets/external-secrets -n external-secrets --create-namespace
```

```yaml
# SecretStore pointing to HashiCorp Vault
apiVersion: external-secrets.io/v1beta1
kind: SecretStore
metadata:
  name: vault-backend
  namespace: production
spec:
  provider:
    vault:
      server: "https://vault.example.com"
      path: "secret"
      version: "v2"
      auth:
        kubernetes:
          mountPath: "kubernetes"
          role: "production-role"
          serviceAccountRef:
            name: vault-auth
---
# ExternalSecret — syncs Vault secret to K8s Secret
apiVersion: external-secrets.io/v1beta1
kind: ExternalSecret
metadata:
  name: db-credentials
  namespace: production
spec:
  refreshInterval: 1h
  secretStoreRef:
    name: vault-backend
    kind: SecretStore
  target:
    name: db-credentials
    creationPolicy: Owner
  data:
    - secretKey: username
      remoteRef:
        key: production/database
        property: username
    - secretKey: password
      remoteRef:
        key: production/database
        property: password
```

## Supply Chain Security

### SBOM Generation and Image Signing

```bash
# Generate SBOM with Syft
syft registry.example.com/myapp:1.0.0 -o spdx-json > sbom.spdx.json
syft registry.example.com/myapp:1.0.0 -o cyclonedx-json > sbom.cdx.json

# Sign image with Cosign (keyless — uses Sigstore/Fulcio)
cosign sign registry.example.com/myapp:1.0.0

# Sign with a key pair
cosign generate-key-pair
cosign sign --key cosign.key registry.example.com/myapp:1.0.0

# Verify signature
cosign verify --key cosign.pub registry.example.com/myapp:1.0.0

# Attach SBOM to image
cosign attach sbom --sbom sbom.spdx.json registry.example.com/myapp:1.0.0
```

### Kyverno Policy: Require Signed Images

```yaml
apiVersion: kyverno.io/v1
kind: ClusterPolicy
metadata:
  name: verify-image-signature
spec:
  validationFailureAction: Enforce
  background: false
  webhookTimeoutSeconds: 30
  rules:
    - name: verify-cosign-signature
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
                      MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE...
                      -----END PUBLIC KEY-----
```

## Security Checklist

| Category | Check | Priority |
|----------|-------|----------|
| **Image** | Use minimal base image (distroless/scratch) | High |
| **Image** | Scan for CVEs in CI pipeline | Critical |
| **Image** | No secrets baked into image layers | Critical |
| **Image** | Pin image digests in production | High |
| **Container** | Run as non-root | Critical |
| **Container** | Read-only root filesystem | High |
| **Container** | Drop ALL capabilities | High |
| **Container** | Set seccomp profile to RuntimeDefault | Medium |
| **K8s** | RBAC with least privilege | Critical |
| **K8s** | Pod Security Standards: restricted | High |
| **K8s** | NetworkPolicy: default deny | High |
| **K8s** | Disable automountServiceAccountToken | Medium |
| **K8s** | Resource limits on all containers | High |
| **Secrets** | External secrets management (Vault/ESO) | High |
| **Secrets** | Rotate secrets regularly | Medium |
| **Supply Chain** | Sign images with Cosign | Medium |
| **Supply Chain** | Generate and store SBOM | Medium |
| **Audit** | Enable K8s audit logging | High |
| **Audit** | Monitor with Falco for runtime threats | Medium |
