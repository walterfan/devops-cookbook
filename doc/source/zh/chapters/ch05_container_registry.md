# 5. 容器镜像仓库

```{mermaid}
flowchart LR
    Dev[Developer] -->|docker push| CI[CI Pipeline]
    CI -->|build & scan| Registry[(Container Registry)]
    Registry -->|docker pull| K8s[Kubernetes Cluster]
    CI -->|sign| Cosign[Cosign / Notary]
    CI -->|scan| Trivy[Trivy / Snyk]
    Cosign --> Registry
```

## 镜像仓库选型

| 镜像仓库 | 类型 | 特点 |
|----------|------|------|
| **Docker Hub** | 公有 SaaS | 免费公开仓库，有拉取频率限制 |
| **Harbor** | 自建部署 | 漏洞扫描、RBAC 权限控制、镜像复制 |
| **AWS ECR** | 云服务 | 与 IAM 集成，支持生命周期策略 |
| **GCR / Artifact Registry** | 云服务 | 与 GCP 深度集成，支持多区域部署 |
| **GitHub Container Registry** | SaaS | 与 GitHub Actions 无缝集成 |
| **Azure ACR** | 云服务 | 支持 Azure AD 认证和异地复制 |

## 镜像标签策略

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

合理的标签策略对于镜像的版本管理至关重要：

- **语义化版本号(Semantic Versioning)**——推荐用于正式发布，便于回滚和版本追踪
- **Git 提交哈希(SHA)**——推荐用于 CI/CD 流水线，确保每次构建可追溯
- **分支名 + 时间戳**——适用于开发和测试环境
- 生产环境中**切勿使用 `:latest` 标签**，因为它指向不确定的版本

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

## 自建镜像仓库（Docker Registry）

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

## Harbor 部署

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

Harbor 是目前最流行的企业级开源镜像仓库，内置漏洞扫描、RBAC 权限管理、镜像复制等功能，推荐通过官方安装脚本进行部署。

## 镜像安全扫描

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

建议在 CI/CD 流水线中集成镜像扫描步骤，对发现的高危(HIGH)和严重(CRITICAL)漏洞设置构建失败策略，从源头把控镜像安全。

## 镜像签名（Cosign）

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

镜像签名用于验证镜像的来源和完整性。Cosign 支持传统的密钥对签名方式，也支持基于 OIDC 的无密钥(Keyless)签名，后者无需管理密钥，更适合在 CI/CD 环境中使用。

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
