# 26. CI/CD 流水线

```{mermaid}
flowchart LR
    subgraph Trigger["Trigger"]
        Push[Code Push]
        PR[Pull Request]
        Tag[Git Tag]
    end
    subgraph CI["Continuous Integration"]
        Build[Build]
        Lint[Lint & Format]
        Test[Unit & Integration Test]
        Scan[Security Scan<br/>SAST / SCA]
        Image[Build Container Image]
        Push2[Push to Registry]
    end
    subgraph CD["Continuous Delivery"]
        Staging[Deploy to Staging]
        Smoke[Smoke Tests]
        Approval{Manual Approval}
        Prod[Deploy to Production]
        Verify[Health Check & Rollback]
    end
    Push --> Build
    PR --> Build
    Tag --> Build
    Build --> Lint --> Test --> Scan --> Image --> Push2
    Push2 --> Staging --> Smoke --> Approval -->|approved| Prod --> Verify
    Approval -->|rejected| Rollback[Rollback]
```

## 流水线核心概念

CI/CD（持续集成 / 持续交付）是现代软件交付的基石。一条设计良好的流水线(Pipeline)能够将从代码提交到生产部署的整个过程自动化，确保每一次变更都经过一致的构建、测试、扫描和部署流程。

### 流水线阶段概览

| 阶段 | 工具 | 用途 |
|------|------|------|
| **构建(Build)** | `go build`, `mvn package`, `pip install` | 编译代码并解析依赖 |
| **代码检查(Lint)** | `golangci-lint`, `ruff`, `checkstyle`, `eslint` | 强制执行代码质量规范 |
| **测试(Test)** | `go test`, `pytest`, `JUnit`, `jest` | 单元测试、集成测试和端到端测试 |
| **静态安全扫描(SAST)** | SonarQube, Semgrep, CodeQL | 静态应用安全测试 |
| **依赖扫描(SCA)** | Trivy, Snyk, Dependabot | 依赖项漏洞扫描 |
| **镜像构建(Image Build)** | Docker, Kaniko, Jib, Buildpacks | 构建 OCI 容器镜像 |
| **镜像推送(Image Push)** | `docker push`, `crane push` | 推送至容器镜像仓库 |
| **部署预发布(Deploy Staging)** | `kubectl`, Helm, ArgoCD | 部署到预发布环境 |
| **冒烟测试(Smoke Test)** | `curl`, k6, Playwright | 验证部署健康状态 |
| **部署生产(Deploy Production)** | Helm, ArgoCD, Spinnaker | 部署到生产环境（需人工审批） |
| **验证(Verify)** | 健康检查, Prometheus 告警 | 确认生产环境稳定性 |

## GitHub Actions

GitHub Actions 是目前最流行的 CI/CD 平台，尤其适用于开源项目和 GitHub 托管的项目。工作流(Workflow)以 YAML 文件的形式定义在 `.github/workflows/` 目录下。

### 完整的 Go 项目流水线

```yaml
# .github/workflows/ci.yml
name: CI/CD Pipeline

on:
  push:
    branches: [main, develop]
    tags: ['v*']
  pull_request:
    branches: [main]

env:
  REGISTRY: ghcr.io
  IMAGE_NAME: ${{ github.repository }}
  GO_VERSION: '1.22'

permissions:
  contents: read
  packages: write
  security-events: write

jobs:
  lint:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-go@v5
        with:
          go-version: ${{ env.GO_VERSION }}
      - name: golangci-lint
        uses: golangci/golangci-lint-action@v4
        with:
          version: latest
          args: --timeout=5m

  test:
    runs-on: ubuntu-latest
    services:
      postgres:
        image: postgres:16
        env:
          POSTGRES_PASSWORD: testpass
          POSTGRES_DB: testdb
        ports: ['5432:5432']
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-go@v5
        with:
          go-version: ${{ env.GO_VERSION }}

      - name: Run tests
        env:
          DATABASE_URL: postgres://postgres:testpass@localhost:5432/testdb?sslmode=disable
        run: |
          go test -v -race -coverprofile=coverage.out -covermode=atomic ./...
          go tool cover -func=coverage.out

      - name: Upload coverage
        uses: codecov/codecov-action@v4
        with:
          files: coverage.out
          token: ${{ secrets.CODECOV_TOKEN }}

  security-scan:
    runs-on: ubuntu-latest
    needs: [lint, test]
    steps:
      - uses: actions/checkout@v4

      - name: Run Trivy vulnerability scanner (filesystem)
        uses: aquasecurity/trivy-action@master
        with:
          scan-type: 'fs'
          scan-ref: '.'
          format: 'sarif'
          output: 'trivy-fs-results.sarif'
          severity: 'CRITICAL,HIGH'

      - name: Upload Trivy scan results
        uses: github/codeql-action/upload-sarif@v3
        with:
          sarif_file: 'trivy-fs-results.sarif'

  build-and-push:
    needs: [lint, test, security-scan]
    runs-on: ubuntu-latest
    if: github.event_name == 'push'
    outputs:
      image-tag: ${{ steps.meta.outputs.tags }}
      image-digest: ${{ steps.build.outputs.digest }}
    steps:
      - uses: actions/checkout@v4

      - name: Set up Docker Buildx
        uses: docker/setup-buildx-action@v3

      - name: Log in to Container Registry
        uses: docker/login-action@v3
        with:
          registry: ${{ env.REGISTRY }}
          username: ${{ github.actor }}
          password: ${{ secrets.GITHUB_TOKEN }}

      - name: Extract metadata
        id: meta
        uses: docker/metadata-action@v5
        with:
          images: ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}
          tags: |
            type=sha,prefix=
            type=ref,event=branch
            type=semver,pattern={{version}}
            type=semver,pattern={{major}}.{{minor}}

      - name: Build and push
        id: build
        uses: docker/build-push-action@v5
        with:
          context: .
          push: true
          tags: ${{ steps.meta.outputs.tags }}
          labels: ${{ steps.meta.outputs.labels }}
          cache-from: type=gha
          cache-to: type=gha,mode=max
          platforms: linux/amd64,linux/arm64

      - name: Scan pushed image
        uses: aquasecurity/trivy-action@master
        with:
          image-ref: ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}:sha-${{ github.sha }}
          format: 'table'
          exit-code: '1'
          severity: 'CRITICAL'
          ignore-unfixed: true

  deploy-staging:
    needs: build-and-push
    runs-on: ubuntu-latest
    environment: staging
    steps:
      - uses: actions/checkout@v4

      - name: Deploy to staging
        uses: azure/setup-kubectl@v3
      - run: |
          echo "${{ secrets.KUBECONFIG_STAGING }}" | base64 -d > $HOME/.kube/config
          helm upgrade --install myapp ./chart \
            -f chart/values-staging.yaml \
            --set image.tag=sha-${{ github.sha }} \
            --namespace staging \
            --wait --timeout 300s

      - name: Smoke test
        run: |
          sleep 10
          curl -sf https://staging.myapp.example.com/healthz || exit 1
          curl -sf https://staging.myapp.example.com/readyz || exit 1

  deploy-production:
    needs: deploy-staging
    runs-on: ubuntu-latest
    environment: production    # requires manual approval in GitHub settings
    if: startsWith(github.ref, 'refs/tags/v')
    steps:
      - uses: actions/checkout@v4

      - name: Deploy to production
        uses: azure/setup-kubectl@v3
      - run: |
          echo "${{ secrets.KUBECONFIG_PROD }}" | base64 -d > $HOME/.kube/config
          helm upgrade --install myapp ./chart \
            -f chart/values-production.yaml \
            --set image.tag=sha-${{ github.sha }} \
            --namespace production \
            --wait --timeout 600s

      - name: Verify deployment
        run: |
          kubectl rollout status deployment/myapp -n production --timeout=300s
          curl -sf https://myapp.example.com/healthz || exit 1
```

### Python 项目流水线

```yaml
# .github/workflows/python-ci.yml
name: Python CI

on:
  push:
    branches: [main]
  pull_request:

jobs:
  test:
    runs-on: ubuntu-latest
    strategy:
      matrix:
        python-version: ['3.11', '3.12']
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-python@v5
        with:
          python-version: ${{ matrix.python-version }}

      - name: Install dependencies
        run: |
          pip install -e ".[dev]"

      - name: Lint
        run: |
          ruff check .
          ruff format --check .
          mypy src/

      - name: Test
        run: |
          pytest -v --cov=src --cov-report=xml --junitxml=junit.xml

      - name: Upload test results
        uses: actions/upload-artifact@v4
        if: always()
        with:
          name: test-results-${{ matrix.python-version }}
          path: junit.xml
```

## GitLab CI

GitLab CI 使用仓库根目录下的 `.gitlab-ci.yml` 文件进行配置，原生支持 Docker-in-Docker（DinD）和 Kaniko 来在 CI 环境中构建镜像。

### 完整的多语言流水线

```yaml
# .gitlab-ci.yml
stages:
  - test
  - build
  - scan
  - deploy-staging
  - deploy-production

variables:
  DOCKER_IMAGE: $CI_REGISTRY_IMAGE:$CI_COMMIT_SHORT_SHA
  DOCKER_IMAGE_LATEST: $CI_REGISTRY_IMAGE:latest

# Cache Go modules / Python packages
.go-cache: &go-cache
  cache:
    key: go-modules
    paths:
      - .go/pkg/mod/
    policy: pull-push

# ─── Test Stage ───────────────────────────────────────────────
test:go:
  stage: test
  image: golang:1.22-alpine
  <<: *go-cache
  variables:
    GOPATH: $CI_PROJECT_DIR/.go
  script:
    - go vet ./...
    - go test -v -race -coverprofile=coverage.out ./...
    - go tool cover -func=coverage.out
  coverage: '/total:\s+\(statements\)\s+(\d+\.\d+)%/'
  artifacts:
    reports:
      coverage_report:
        coverage_format: cobertura
        path: coverage.out

test:python:
  stage: test
  image: python:3.12-slim
  script:
    - pip install -e ".[dev]"
    - ruff check .
    - pytest -v --cov=src --cov-report=xml:coverage.xml --junitxml=report.xml
  artifacts:
    reports:
      junit: report.xml
      coverage_report:
        coverage_format: cobertura
        path: coverage.xml

# ─── Build Stage (Docker-in-Docker) ──────────────────────────
build:dind:
  stage: build
  image: docker:24
  services:
    - docker:24-dind
  variables:
    DOCKER_TLS_CERTDIR: "/certs"
  before_script:
    - docker login -u $CI_REGISTRY_USER -p $CI_REGISTRY_PASSWORD $CI_REGISTRY
  script:
    - docker build
        --build-arg BUILD_DATE=$(date -u +%Y-%m-%dT%H:%M:%SZ)
        --build-arg VCS_REF=$CI_COMMIT_SHORT_SHA
        -t $DOCKER_IMAGE
        -t $DOCKER_IMAGE_LATEST .
    - docker push $DOCKER_IMAGE
    - docker push $DOCKER_IMAGE_LATEST

# ─── Build Stage (Kaniko — no Docker daemon needed) ──────────
build:kaniko:
  stage: build
  image:
    name: gcr.io/kaniko-project/executor:v1.23.0-debug
    entrypoint: [""]
  script:
    - mkdir -p /kaniko/.docker
    - echo "{\"auths\":{\"$CI_REGISTRY\":{\"auth\":\"$(echo -n ${CI_REGISTRY_USER}:${CI_REGISTRY_PASSWORD} | base64)\"}}}" > /kaniko/.docker/config.json
    - /kaniko/executor
        --context $CI_PROJECT_DIR
        --dockerfile $CI_PROJECT_DIR/Dockerfile
        --destination $DOCKER_IMAGE
        --destination $DOCKER_IMAGE_LATEST
        --cache=true
        --cache-repo=$CI_REGISTRY_IMAGE/cache
        --snapshot-mode=redo
        --compressed-caching=false

# ─── Security Scan ────────────────────────────────────────────
scan:trivy:
  stage: scan
  image:
    name: aquasec/trivy:0.50.0
    entrypoint: [""]
  variables:
    TRIVY_USERNAME: $CI_REGISTRY_USER
    TRIVY_PASSWORD: $CI_REGISTRY_PASSWORD
  script:
    - trivy image --exit-code 0 --severity LOW,MEDIUM --format table $DOCKER_IMAGE
    - trivy image --exit-code 1 --severity HIGH,CRITICAL --format table $DOCKER_IMAGE
  allow_failure: false

scan:sonarqube:
  stage: scan
  image:
    name: sonarsource/sonar-scanner-cli:latest
    entrypoint: [""]
  variables:
    SONAR_USER_HOME: "${CI_PROJECT_DIR}/.sonar"
  script:
    - sonar-scanner
        -Dsonar.projectKey=$CI_PROJECT_NAME
        -Dsonar.sources=.
        -Dsonar.host.url=$SONAR_HOST_URL
        -Dsonar.token=$SONAR_TOKEN

# ─── Deploy Staging ───────────────────────────────────────────
deploy:staging:
  stage: deploy-staging
  image: alpine/helm:3.14
  environment:
    name: staging
    url: https://staging.myapp.example.com
  before_script:
    - apk add --no-cache kubectl
    - echo "$KUBECONFIG_STAGING" | base64 -d > /tmp/kubeconfig
    - export KUBECONFIG=/tmp/kubeconfig
  script:
    - helm upgrade --install myapp ./chart
        -f chart/values-staging.yaml
        --set image.repository=$CI_REGISTRY_IMAGE
        --set image.tag=$CI_COMMIT_SHORT_SHA
        --namespace staging
        --create-namespace
        --wait --timeout 300s
    - kubectl rollout status deployment/myapp -n staging --timeout=120s
  only:
    - main

# ─── Deploy Production (Manual Gate) ─────────────────────────
deploy:production:
  stage: deploy-production
  image: alpine/helm:3.14
  environment:
    name: production
    url: https://myapp.example.com
  when: manual
  before_script:
    - apk add --no-cache kubectl
    - echo "$KUBECONFIG_PROD" | base64 -d > /tmp/kubeconfig
    - export KUBECONFIG=/tmp/kubeconfig
  script:
    - helm upgrade --install myapp ./chart
        -f chart/values-production.yaml
        --set image.repository=$CI_REGISTRY_IMAGE
        --set image.tag=$CI_COMMIT_SHORT_SHA
        --namespace production
        --wait --timeout 600s
    - kubectl rollout status deployment/myapp -n production --timeout=300s
  only:
    - main
    - tags
```

## Jenkinsfile

Jenkins 使用 `Jenkinsfile`（Groovy DSL）来实现流水线即代码(Pipeline as Code)，支持声明式(Declarative)和脚本式(Scripted)两种流水线语法。

### Java（Maven）声明式流水线

```groovy
// Jenkinsfile
pipeline {
    agent {
        kubernetes {
            yaml '''
apiVersion: v1
kind: Pod
spec:
  containers:
    - name: maven
      image: maven:3.9-eclipse-temurin-21
      command: ['sleep', 'infinity']
      volumeMounts:
        - name: m2-cache
          mountPath: /root/.m2
    - name: kaniko
      image: gcr.io/kaniko-project/executor:v1.23.0-debug
      command: ['sleep', 'infinity']
      volumeMounts:
        - name: docker-config
          mountPath: /kaniko/.docker
    - name: trivy
      image: aquasec/trivy:0.50.0
      command: ['sleep', 'infinity']
  volumes:
    - name: m2-cache
      persistentVolumeClaim:
        claimName: maven-cache
    - name: docker-config
      secret:
        secretName: registry-credentials
        items:
          - key: .dockerconfigjson
            path: config.json
'''
        }
    }

    environment {
        REGISTRY    = 'registry.example.com'
        IMAGE_NAME  = 'myorg/java-api'
        IMAGE_TAG   = "${REGISTRY}/${IMAGE_NAME}:${env.BUILD_NUMBER}"
        SONAR_TOKEN = credentials('sonar-token')
    }

    options {
        timeout(time: 30, unit: 'MINUTES')
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '20'))
    }

    stages {
        stage('Build & Test') {
            steps {
                container('maven') {
                    sh '''
                        mvn clean verify \
                            -Dmaven.test.failure.ignore=false \
                            -Djacoco.destFile=target/jacoco.exec
                    '''
                }
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                    jacoco(execPattern: 'target/jacoco.exec')
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                container('maven') {
                    sh '''
                        mvn sonar:sonar \
                            -Dsonar.host.url=${SONAR_HOST_URL} \
                            -Dsonar.token=${SONAR_TOKEN}
                    '''
                }
            }
        }

        stage('Build Image') {
            steps {
                container('kaniko') {
                    sh """
                        /kaniko/executor \
                            --context=\$(pwd) \
                            --dockerfile=Dockerfile \
                            --destination=${IMAGE_TAG} \
                            --destination=${REGISTRY}/${IMAGE_NAME}:latest \
                            --cache=true \
                            --cache-repo=${REGISTRY}/${IMAGE_NAME}/cache
                    """
                }
            }
        }

        stage('Scan Image') {
            steps {
                container('trivy') {
                    sh """
                        trivy image \
                            --exit-code 1 \
                            --severity HIGH,CRITICAL \
                            --ignore-unfixed \
                            --format table \
                            ${IMAGE_TAG}
                    """
                }
            }
        }

        stage('Deploy to Staging') {
            when {
                branch 'main'
            }
            steps {
                sh """
                    helm upgrade --install java-api ./chart \
                        -f chart/values-staging.yaml \
                        --set image.tag=${env.BUILD_NUMBER} \
                        --namespace staging \
                        --wait --timeout 300s
                """
            }
        }

        stage('Deploy to Production') {
            when {
                buildingTag()
            }
            input {
                message "Deploy to production?"
                ok "Deploy"
                submitter "admin,release-managers"
            }
            steps {
                sh """
                    helm upgrade --install java-api ./chart \
                        -f chart/values-production.yaml \
                        --set image.tag=${env.BUILD_NUMBER} \
                        --namespace production \
                        --wait --timeout 600s
                """
            }
        }
    }

    post {
        success {
            slackSend(
                channel: '#deployments',
                color: 'good',
                message: "✅ ${env.JOB_NAME} #${env.BUILD_NUMBER} succeeded\n${env.BUILD_URL}"
            )
        }
        failure {
            slackSend(
                channel: '#ci-alerts',
                color: 'danger',
                message: "❌ ${env.JOB_NAME} #${env.BUILD_NUMBER} failed\n${env.BUILD_URL}"
            )
        }
    }
}
```

## Kaniko：无需 Docker 守护进程构建镜像

Kaniko 可以在 Kubernetes Pod 内部构建容器镜像，无需依赖 Docker 守护进程(Docker Daemon)。这对于禁止使用特权容器(Privileged Container)的安全敏感环境至关重要。

```yaml
# Kubernetes Job for Kaniko build
apiVersion: batch/v1
kind: Job
metadata:
  name: kaniko-build
spec:
  template:
    spec:
      containers:
        - name: kaniko
          image: gcr.io/kaniko-project/executor:v1.23.0
          args:
            - "--context=git://github.com/myorg/myapp.git#refs/heads/main"
            - "--dockerfile=Dockerfile"
            - "--destination=registry.example.com/myapp:latest"
            - "--cache=true"
            - "--cache-repo=registry.example.com/myapp/cache"
            - "--snapshot-mode=redo"
            - "--compressed-caching=false"
          volumeMounts:
            - name: docker-config
              mountPath: /kaniko/.docker
      restartPolicy: Never
      volumes:
        - name: docker-config
          secret:
            secretName: registry-credentials
```

## 制品管理

CI 过程中产生的制品(Artifact)——包括二进制文件、测试报告、SBOM（软件物料清单）、扫描结果等——都应当妥善存储并进行版本管理。

```yaml
# GitHub Actions — upload/download artifacts
- name: Upload build artifact
  uses: actions/upload-artifact@v4
  with:
    name: binary-${{ github.sha }}
    path: dist/
    retention-days: 30

- name: Generate SBOM
  run: syft dir:. -o spdx-json > sbom.json

- name: Upload SBOM
  uses: actions/upload-artifact@v4
  with:
    name: sbom
    path: sbom.json
```

## CI/CD 最佳实践

| 实践 | 说明 |
|------|------|
| **快速反馈(Fast Feedback)** | CI 总耗时控制在 10 分钟以内；尽量并行执行各阶段 |
| **快速失败(Fail Fast)** | 在耗时的镜像构建之前先运行代码检查和单元测试 |
| **积极缓存(Cache Aggressively)** | 缓存依赖项（`go mod`、`pip`、`mvn`、`npm`） |
| **不可变标签(Immutable Tags)** | 使用 commit SHA 或语义化版本号，生产环境禁止使用 `:latest` |
| **密钥管理(Secrets Management)** | 使用 CI 平台的密钥管理功能，绝不在代码中硬编码凭据 |
| **分支保护(Branch Protection)** | 合并前必须通过 CI 检查和代码评审 |
| **环境审批门禁(Environment Gates)** | 生产环境部署需人工审批 |
| **回滚预案(Rollback Plan)** | 始终准备好 `helm rollback` 或 `kubectl rollout undo` |
| **通知(Notifications)** | 失败时通过 Slack、Teams 或邮件发送告警 |
| **审计追踪(Audit Trail)** | 每次部署都可追溯到对应的 Git 提交 |
