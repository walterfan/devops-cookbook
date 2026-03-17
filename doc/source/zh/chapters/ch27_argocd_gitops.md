# 27. ArgoCD 与 GitOps

```{mermaid}
flowchart TB
    subgraph Developer["Developer Workflow"]
        Dev[Developer] -->|push code| CodeRepo[Code Repository]
        CodeRepo -->|triggers| CI[CI Pipeline<br/>Build, Test, Scan]
        CI -->|update image tag| ConfigRepo[GitOps Config Repo<br/>K8s Manifests / Helm / Kustomize]
    end
    subgraph ArgoCD["ArgoCD Controller"]
        Watcher[Repo Server<br/>Poll / Webhook] -->|detect drift| Reconciler[Application Controller]
        Reconciler -->|compare| Diff{Desired State<br/>vs<br/>Live State}
        Diff -->|out of sync| Sync[Sync Engine]
        Diff -->|in sync| OK[✓ Healthy]
    end
    subgraph Cluster["Kubernetes Cluster"]
        Sync -->|apply manifests| NS1[Namespace: staging]
        Sync -->|apply manifests| NS2[Namespace: production]
    end
    ConfigRepo -->|watch| Watcher
    Reconciler -->|monitor| NS1
    Reconciler -->|monitor| NS2
```

## GitOps 核心原则

GitOps 是一种运维框架，它将 DevOps 最佳实践（版本控制、协作、CI/CD）应用于基础设施自动化。其核心原则包括：

1. **声明式(Declarative)**：整个系统通过 YAML、Helm Chart 或 Kustomize Overlay 进行声明式描述
2. **版本化且不可变(Versioned and Immutable)**：期望状态存储在 Git 中，作为唯一可信源(Single Source of Truth)；每次变更都是一次 Git 提交
3. **自动拉取(Pulled Automatically)**：经过审批的变更由软件代理(Agent)自动拉取并应用，而非由 CI 推送
4. **持续调谐(Continuously Reconciled)**：软件代理持续监控实际状态，并自动修正偏差(Drift)以匹配期望状态

### GitOps 与传统 CI/CD 的对比

| 维度 | 传统 CI/CD | GitOps |
|------|-----------|--------|
| 部署触发方式 | CI 流水线推送到集群 | Agent 从 Git 拉取 |
| 可信源 | CI 系统 / 脚本 | Git 仓库 |
| 偏差检测 | 无（需人工检查） | 自动调谐 |
| 回滚方式 | 重新运行流水线 / 手动操作 | `git revert` |
| 审计追踪 | CI 日志 | Git 历史记录 |
| 凭据管理 | CI 需要集群访问权限 | 仅 Agent 需要集群访问权限 |

## ArgoCD 架构

ArgoCD 是一款面向 Kubernetes 的声明式 GitOps 持续交付工具。其核心组件包括：

- **API Server**：对外暴露 gRPC/REST API 和 Web UI
- **Repository Server**：克隆 Git 仓库，从 Helm/Kustomize/纯 YAML 生成 Kubernetes 清单(Manifest)
- **Application Controller**：持续监控运行中的应用，将实际状态与期望状态进行比对
- **Redis**：用于缓存应用状态和仓库数据
- **Dex**：可选的 OIDC 提供者，用于 SSO 单点登录集成

### 核心 CRD

| CRD | 用途 |
|-----|------|
| `Application` | 定义单个应用：源仓库、路径、目标集群/命名空间 |
| `AppProject` | 对应用进行分组管理，支持 RBAC 权限控制和源/目标限制 |
| `ApplicationSet` | 通过模板化方式跨集群/环境批量生成 Application |

## 安装

```bash
# Install ArgoCD into the cluster
kubectl create namespace argocd
kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml

# Install ArgoCD CLI
brew install argocd          # macOS
# or: curl -sSL -o argocd https://github.com/argoproj/argo-cd/releases/latest/download/argocd-linux-amd64

# Get initial admin password
argocd admin initial-password -n argocd

# Access the UI via port-forward
kubectl port-forward svc/argocd-server -n argocd 8080:443

# Login and change password
argocd login localhost:8080 --insecure
argocd account update-password

# Or expose via Ingress
cat <<EOF | kubectl apply -f -
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: argocd-server
  namespace: argocd
  annotations:
    nginx.ingress.kubernetes.io/ssl-passthrough: "true"
    nginx.ingress.kubernetes.io/backend-protocol: "HTTPS"
spec:
  ingressClassName: nginx
  rules:
    - host: argocd.example.com
      http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: argocd-server
                port:
                  number: 443
  tls:
    - hosts: [argocd.example.com]
      secretName: argocd-tls
EOF
```

## GitOps 仓库结构

一个组织良好的 GitOps 仓库应将应用与基础设施分离，并使用 Kustomize Overlay 来管理不同环境的差异化配置：

```
gitops-config/
├── apps/
│   ├── myapp/
│   │   ├── base/
│   │   │   ├── kustomization.yaml
│   │   │   ├── deployment.yaml
│   │   │   ├── service.yaml
│   │   │   ├── ingress.yaml
│   │   │   ├── hpa.yaml
│   │   │   └── pdb.yaml
│   │   └── overlays/
│   │       ├── dev/
│   │       │   ├── kustomization.yaml
│   │       │   └── patch-replicas.yaml
│   │       ├── staging/
│   │       │   ├── kustomization.yaml
│   │       │   ├── patch-replicas.yaml
│   │       │   └── patch-resources.yaml
│   │       └── production/
│   │           ├── kustomization.yaml
│   │           ├── patch-replicas.yaml
│   │           ├── patch-resources.yaml
│   │           └── patch-ingress.yaml
│   ├── api-gateway/
│   │   ├── base/
│   │   └── overlays/
│   └── worker/
│       ├── base/
│       └── overlays/
├── infrastructure/
│   ├── cert-manager/
│   │   ├── kustomization.yaml
│   │   ├── namespace.yaml
│   │   ├── helmrelease.yaml
│   │   └── clusterissuer.yaml
│   ├── ingress-nginx/
│   ├── monitoring/
│   │   ├── prometheus/
│   │   ├── grafana/
│   │   └── loki/
│   ├── sealed-secrets/
│   └── external-dns/
├── projects/
│   ├── apps-project.yaml
│   └── infra-project.yaml
├── applicationsets/
│   ├── apps-appset.yaml
│   └── infra-appset.yaml
└── root-app.yaml              # App-of-apps entry point
```

## Application CRD

### 基于 Kustomize 的基础 Application

```yaml
apiVersion: argoproj.io/v1alpha1
kind: Application
metadata:
  name: myapp-production
  namespace: argocd
  labels:
    team: backend
    env: production
  finalizers:
    - resources-finalizer.argocd.argoproj.io    # cascade delete
spec:
  project: apps
  source:
    repoURL: https://github.com/myorg/gitops-config.git
    targetRevision: main
    path: apps/myapp/overlays/production
  destination:
    server: https://kubernetes.default.svc
    namespace: production
  syncPolicy:
    automated:
      prune: true          # delete resources removed from Git
      selfHeal: true       # revert manual kubectl changes
      allowEmpty: false     # don't sync if manifests are empty
    syncOptions:
      - CreateNamespace=true
      - PrunePropagationPolicy=foreground
      - PruneLast=true
      - ServerSideApply=true
      - RespectIgnoreDifferences=true
    retry:
      limit: 5
      backoff:
        duration: 5s
        factor: 2
        maxDuration: 3m
  ignoreDifferences:
    - group: apps
      kind: Deployment
      jsonPointers:
        - /spec/replicas    # ignore HPA-managed replicas
```

### 基于 Helm 的 Application

```yaml
apiVersion: argoproj.io/v1alpha1
kind: Application
metadata:
  name: myapp-helm
  namespace: argocd
spec:
  project: apps
  source:
    repoURL: https://github.com/myorg/helm-charts.git
    targetRevision: main
    path: charts/myapp
    helm:
      releaseName: myapp
      valueFiles:
        - values.yaml
        - values-production.yaml
      parameters:
        - name: image.tag
          value: "1.5.2"
        - name: replicaCount
          value: "5"
      # Use values from a different repo
      # fileParameters:
      #   - name: config
      #     path: files/config.json
  destination:
    server: https://kubernetes.default.svc
    namespace: production
  syncPolicy:
    automated:
      prune: true
      selfHeal: true
```

### Kustomize Base 与 Overlay

```yaml
# apps/myapp/base/kustomization.yaml
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization
resources:
  - deployment.yaml
  - service.yaml
  - ingress.yaml
  - hpa.yaml
  - pdb.yaml
commonLabels:
  app.kubernetes.io/name: myapp
  app.kubernetes.io/managed-by: argocd
```

```yaml
# apps/myapp/overlays/production/kustomization.yaml
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization
namespace: production
resources:
  - ../../base
patches:
  - path: patch-replicas.yaml
  - path: patch-resources.yaml
  - path: patch-ingress.yaml
images:
  - name: myapp
    newName: registry.example.com/myapp
    newTag: "1.5.2"    # Updated by CI pipeline via: kustomize edit set image
```

```yaml
# apps/myapp/overlays/production/patch-replicas.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: myapp
spec:
  replicas: 5
```

```yaml
# apps/myapp/overlays/production/patch-resources.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: myapp
spec:
  template:
    spec:
      containers:
        - name: myapp
          resources:
            requests:
              cpu: "500m"
              memory: "512Mi"
            limits:
              cpu: "2"
              memory: "1Gi"
```

## App-of-Apps 模式

App-of-Apps 模式使用一个根 Application 来管理所有其他 Application。通过这种方式，只需一个入口点即可引导整个集群的配置。

```yaml
# root-app.yaml — the single entry point
apiVersion: argoproj.io/v1alpha1
kind: Application
metadata:
  name: root
  namespace: argocd
spec:
  project: default
  source:
    repoURL: https://github.com/myorg/gitops-config.git
    targetRevision: main
    path: applicationsets    # directory containing ApplicationSet YAMLs
  destination:
    server: https://kubernetes.default.svc
    namespace: argocd
  syncPolicy:
    automated:
      prune: true
      selfHeal: true
```

## ApplicationSet（多集群 / 多环境）

ApplicationSet 通过生成器(Generator)——包括 list、git、cluster、matrix、merge 等类型——自动批量生成 ArgoCD Application。

### Git 目录生成器

```yaml
# Automatically create an Application for each directory under apps/
apiVersion: argoproj.io/v1alpha1
kind: ApplicationSet
metadata:
  name: apps
  namespace: argocd
spec:
  generators:
    - git:
        repoURL: https://github.com/myorg/gitops-config.git
        revision: main
        directories:
          - path: apps/*/overlays/production
  template:
    metadata:
      name: '{{path[1]}}'    # e.g., "myapp"
    spec:
      project: apps
      source:
        repoURL: https://github.com/myorg/gitops-config.git
        targetRevision: main
        path: '{{path}}'
      destination:
        server: https://kubernetes.default.svc
        namespace: production
      syncPolicy:
        automated:
          prune: true
          selfHeal: true
        syncOptions:
          - CreateNamespace=true
```

### 基于 List 生成器的多集群部署

```yaml
apiVersion: argoproj.io/v1alpha1
kind: ApplicationSet
metadata:
  name: myapp-multi-cluster
  namespace: argocd
spec:
  generators:
    - list:
        elements:
          - cluster: staging
            url: https://staging-k8s.example.com
            namespace: staging
            values:
              replicas: "2"
              domain: staging.myapp.example.com
          - cluster: production-us
            url: https://prod-us-k8s.example.com
            namespace: production
            values:
              replicas: "5"
              domain: us.myapp.example.com
          - cluster: production-eu
            url: https://prod-eu-k8s.example.com
            namespace: production
            values:
              replicas: "5"
              domain: eu.myapp.example.com
  template:
    metadata:
      name: 'myapp-{{cluster}}'
    spec:
      project: apps
      source:
        repoURL: https://github.com/myorg/gitops-config.git
        targetRevision: main
        path: 'apps/myapp/overlays/{{cluster}}'
      destination:
        server: '{{url}}'
        namespace: '{{namespace}}'
      syncPolicy:
        automated:
          prune: true
          selfHeal: true
```

## AppProject 权限控制

AppProject 用于限制 Application 可以使用的仓库、集群和命名空间(Namespace)。

```yaml
apiVersion: argoproj.io/v1alpha1
kind: AppProject
metadata:
  name: apps
  namespace: argocd
spec:
  description: "Application workloads"
  sourceRepos:
    - 'https://github.com/myorg/gitops-config.git'
    - 'https://github.com/myorg/helm-charts.git'
  destinations:
    - namespace: 'staging'
      server: 'https://kubernetes.default.svc'
    - namespace: 'production'
      server: 'https://kubernetes.default.svc'
  clusterResourceWhitelist:
    - group: ''
      kind: Namespace
  namespaceResourceBlacklist:
    - group: ''
      kind: ResourceQuota
    - group: ''
      kind: LimitRange
  roles:
    - name: developer
      description: "Read-only access for developers"
      policies:
        - p, proj:apps:developer, applications, get, apps/*, allow
        - p, proj:apps:developer, applications, sync, apps/*, deny
      groups:
        - developers
    - name: deployer
      description: "Sync access for deployers"
      policies:
        - p, proj:apps:deployer, applications, *, apps/*, allow
      groups:
        - sre-team
```

## SSO 单点登录配置

```yaml
# argocd-cm ConfigMap — OIDC with Keycloak/Dex
apiVersion: v1
kind: ConfigMap
metadata:
  name: argocd-cm
  namespace: argocd
data:
  url: https://argocd.example.com
  oidc.config: |
    name: Keycloak
    issuer: https://keycloak.example.com/realms/myorg
    clientID: argocd
    clientSecret: $oidc.keycloak.clientSecret
    requestedScopes:
      - openid
      - profile
      - email
      - groups
```

## 通知配置

```yaml
# argocd-notifications-cm ConfigMap
apiVersion: v1
kind: ConfigMap
metadata:
  name: argocd-notifications-cm
  namespace: argocd
data:
  service.slack: |
    token: $slack-token
  trigger.on-sync-succeeded: |
    - send: [app-sync-succeeded]
      when: app.status.operationState.phase in ['Succeeded']
  trigger.on-sync-failed: |
    - send: [app-sync-failed]
      when: app.status.operationState.phase in ['Error', 'Failed']
  trigger.on-health-degraded: |
    - send: [app-health-degraded]
      when: app.status.health.status == 'Degraded'
  template.app-sync-succeeded: |
    slack:
      attachments: |
        [{
          "color": "#18be52",
          "title": "✅ {{.app.metadata.name}} synced successfully",
          "fields": [
            {"title": "Revision", "value": "{{.app.status.sync.revision}}", "short": true},
            {"title": "Environment", "value": "{{.app.spec.destination.namespace}}", "short": true}
          ]
        }]
  template.app-sync-failed: |
    slack:
      attachments: |
        [{
          "color": "#E96D76",
          "title": "❌ {{.app.metadata.name}} sync failed",
          "fields": [
            {"title": "Error", "value": "{{.app.status.operationState.message}}", "short": false}
          ]
        }]
```

## ArgoCD CLI 常用命令

```bash
# Application management
argocd app list
argocd app get myapp
argocd app sync myapp
argocd app sync myapp --prune --force
argocd app diff myapp
argocd app history myapp
argocd app rollback myapp <history-id>
argocd app delete myapp --cascade

# Wait for sync to complete
argocd app wait myapp --health --timeout 300

# Cluster management
argocd cluster add my-cluster-context
argocd cluster list

# Repository management
argocd repo add https://github.com/myorg/gitops-config.git \
  --username git --password $GITHUB_TOKEN
argocd repo list

# Project management
argocd proj list
argocd proj get apps

# Account management
argocd account list
argocd account update-password
argocd account generate-token --account ci-bot
```

## CI 流水线与 GitOps 的衔接

CI 流水线在成功构建镜像后，应更新 GitOps 仓库中的镜像标签(Image Tag)。这是连接 CI 和 CD 的桥梁：

```bash
#!/bin/bash
# update-image-tag.sh — called by CI after image push
set -euo pipefail

REPO="https://github.com/myorg/gitops-config.git"
APP_PATH="apps/myapp/overlays/production"
NEW_TAG="$1"

# Clone the GitOps repo
git clone "$REPO" /tmp/gitops
cd /tmp/gitops

# Update the image tag using kustomize
cd "$APP_PATH"
kustomize edit set image "myapp=registry.example.com/myapp:${NEW_TAG}"

# Commit and push
git add .
git commit -m "chore: update myapp image to ${NEW_TAG}"
git push origin main
```

```yaml
# GitHub Actions step to update GitOps repo
- name: Update GitOps repo
  env:
    GH_TOKEN: ${{ secrets.GITOPS_REPO_TOKEN }}
  run: |
    git clone https://x-access-token:${GH_TOKEN}@github.com/myorg/gitops-config.git /tmp/gitops
    cd /tmp/gitops/apps/myapp/overlays/production
    kustomize edit set image myapp=ghcr.io/myorg/myapp:${{ github.sha }}
    git config user.name "ci-bot"
    git config user.email "ci-bot@myorg.com"
    git add .
    git commit -m "chore: update myapp to ${{ github.sha }}"
    git push
```
