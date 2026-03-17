# 27. ArgoCD and GitOps

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

## GitOps Principles

GitOps is an operational framework that applies DevOps best practices (version control, collaboration, CI/CD) to infrastructure automation. The core principles are:

1. **Declarative**: The entire system is described declaratively in YAML, Helm charts, or Kustomize overlays
2. **Versioned and Immutable**: The desired state is stored in Git as the single source of truth; every change is a Git commit
3. **Pulled Automatically**: Approved changes are automatically pulled and applied by software agents (not pushed by CI)
4. **Continuously Reconciled**: Software agents continuously observe the actual state and correct drift to match the desired state

### GitOps vs Traditional CI/CD

| Aspect | Traditional CI/CD | GitOps |
|--------|-------------------|--------|
| Deployment trigger | CI pipeline pushes to cluster | Agent pulls from Git |
| Source of truth | CI system / scripts | Git repository |
| Drift detection | None (manual) | Automatic reconciliation |
| Rollback | Re-run pipeline / manual | `git revert` |
| Audit trail | CI logs | Git history |
| Credentials | CI needs cluster access | Only agent needs cluster access |

## ArgoCD Architecture

ArgoCD is a declarative, GitOps continuous delivery tool for Kubernetes. Its key components:

- **API Server**: Exposes the gRPC/REST API and Web UI
- **Repository Server**: Clones Git repos, generates Kubernetes manifests from Helm/Kustomize/plain YAML
- **Application Controller**: Continuously monitors running applications and compares live state against desired state
- **Redis**: Caching layer for application state and repo data
- **Dex**: Optional OIDC provider for SSO integration

### Core CRDs

| CRD | Purpose |
|-----|---------|
| `Application` | Defines a single application: source repo, path, destination cluster/namespace |
| `AppProject` | Groups applications with RBAC, source/destination restrictions |
| `ApplicationSet` | Templated generation of Applications across clusters/environments |

## Installation

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

## GitOps Repository Structure

A well-organized GitOps repository separates applications from infrastructure and uses Kustomize overlays for environment-specific configuration:

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

### Basic Application with Kustomize

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

### Application with Helm

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

### Kustomize Base and Overlay

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

## App-of-Apps Pattern

The app-of-apps pattern uses a single root Application that manages all other Applications. This bootstraps the entire cluster from one entry point.

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

## ApplicationSet (Multi-Cluster / Multi-Environment)

ApplicationSet automates the generation of ArgoCD Applications using generators (list, git, cluster, matrix, merge).

### Git Directory Generator

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

### Multi-Cluster with List Generator

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

## AppProject RBAC

AppProjects restrict which repositories, clusters, and namespaces an Application can use.

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

## SSO Configuration

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

## Notifications

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

## ArgoCD CLI Commands

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

## CI Pipeline Integration

The CI pipeline should update the image tag in the GitOps repo after a successful build. This is the bridge between CI and CD:

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
