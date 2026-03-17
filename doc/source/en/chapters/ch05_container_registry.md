# 5. Container Registry

```{mermaid}
flowchart LR
    Dev[Developer] -->|docker push| CI[CI Pipeline]
    CI -->|build & scan| Registry[(Container Registry)]
    Registry -->|docker pull| K8s[Kubernetes Cluster]
    CI -->|sign| Cosign[Cosign / Notary]
    CI -->|scan| Trivy[Trivy / Snyk]
    Cosign --> Registry
```

## Registry Options

| Registry | Type | Features |
|----------|------|----------|
| **Docker Hub** | Public SaaS | Free public repos, rate limits |
| **Harbor** | Self-hosted | Vulnerability scanning, RBAC, replication |
| **AWS ECR** | Cloud | IAM integration, lifecycle policies |
| **GCR / Artifact Registry** | Cloud | GCP integration, multi-region |
| **GitHub Container Registry** | SaaS | GitHub Actions integration |
| **Azure ACR** | Cloud | Azure AD, geo-replication |

## Image Tagging Strategies

```bash
# Semantic versioning (recommended for releases)
docker tag myapp:latest registry.example.com/myapp:1.2.3
docker tag myapp:latest registry.example.com/myapp:1.2
docker tag myapp:latest registry.example.com/myapp:1

# Git SHA (recommended for CI/CD)
docker tag myapp:latest registry.example.com/myapp:$(git rev-parse --short HEAD)

# Branch + timestamp
docker tag myapp:latest registry.example.com/myapp:main-20260317

# Never use :latest in production
```

## Docker Hub

```bash
# Login
docker login

# Push
docker tag myapp:1.0 myuser/myapp:1.0
docker push myuser/myapp:1.0

# Pull
docker pull myuser/myapp:1.0
```

## Self-hosted Registry (Docker)

```bash
# Quick start
docker run -d -p 5000:5000 --name registry \
  -v registry_data:/var/lib/registry \
  registry:2

# Push to local registry
docker tag myapp:1.0 localhost:5000/myapp:1.0
docker push localhost:5000/myapp:1.0

# List repositories
curl http://localhost:5000/v2/_catalog
```

## Harbor Setup

```yaml
# docker-compose.yml for Harbor (simplified)
# Download full installer from https://goharbor.io
services:
  harbor-core:
    image: goharbor/harbor-core:v2.10
    environment:
      - CONFIG_PATH=/etc/core/app.conf
    volumes:
      - ./config/core:/etc/core
    depends_on:
      - harbor-db
      - redis

  harbor-db:
    image: goharbor/harbor-db:v2.10
    volumes:
      - harbor_db:/var/lib/postgresql/data
    environment:
      POSTGRES_PASSWORD: root123

  redis:
    image: goharbor/redis-photon:v2.10
    volumes:
      - harbor_redis:/var/lib/redis

  registry:
    image: goharbor/registry-photon:v2.10
    volumes:
      - harbor_registry:/storage

  nginx:
    image: goharbor/nginx-photon:v2.10
    ports:
      - "80:8080"
      - "443:8443"

volumes:
  harbor_db:
  harbor_redis:
  harbor_registry:
```

```bash
# Harbor installation (recommended way)
wget https://github.com/goharbor/harbor/releases/download/v2.10.0/harbor-online-installer-v2.10.0.tgz
tar xzf harbor-online-installer-v2.10.0.tgz
cd harbor

# Edit harbor.yml
# Set hostname, https certificate, admin password
cp harbor.yml.tmpl harbor.yml
vim harbor.yml

# Install
./install.sh --with-trivy
```

## Image Scanning

### Trivy

```bash
# Scan an image
trivy image myapp:1.0

# Scan with severity filter
trivy image --severity HIGH,CRITICAL myapp:1.0

# Output as JSON
trivy image -f json -o results.json myapp:1.0

# Scan in CI (fail on critical)
trivy image --exit-code 1 --severity CRITICAL myapp:1.0
```

### Snyk

```bash
snyk container test myapp:1.0
snyk container monitor myapp:1.0
```

## Image Signing (Cosign)

```bash
# Install cosign
brew install cosign

# Generate key pair
cosign generate-key-pair

# Sign an image
cosign sign --key cosign.key registry.example.com/myapp:1.0

# Verify signature
cosign verify --key cosign.pub registry.example.com/myapp:1.0

# Keyless signing (with OIDC)
cosign sign registry.example.com/myapp:1.0
cosign verify --certificate-identity=user@example.com \
  --certificate-oidc-issuer=https://accounts.google.com \
  registry.example.com/myapp:1.0
```

## AWS ECR

```bash
# Login
aws ecr get-login-password --region us-east-1 | \
  docker login --username AWS --password-stdin 123456789.dkr.ecr.us-east-1.amazonaws.com

# Create repository
aws ecr create-repository --repository-name myapp

# Push
docker tag myapp:1.0 123456789.dkr.ecr.us-east-1.amazonaws.com/myapp:1.0
docker push 123456789.dkr.ecr.us-east-1.amazonaws.com/myapp:1.0

# Lifecycle policy (keep last 10 images)
aws ecr put-lifecycle-policy --repository-name myapp \
  --lifecycle-policy-text '{"rules":[{"rulePriority":1,"selection":{"tagStatus":"any","countType":"imageCountMoreThan","countNumber":10},"action":{"type":"expire"}}]}'
```

## GitHub Container Registry

```bash
# Login
echo $GITHUB_TOKEN | docker login ghcr.io -u USERNAME --password-stdin

# Push
docker tag myapp:1.0 ghcr.io/myuser/myapp:1.0
docker push ghcr.io/myuser/myapp:1.0
```
