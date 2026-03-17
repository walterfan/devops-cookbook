# CI/CD 流水线

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

## Pipeline Concepts

CI/CD (Continuous Integration / Continuous Delivery) is the backbone of modern software delivery. A well-designed pipeline automates the journey from code commit to production deployment, ensuring every change is built, tested, scanned, and deployed consistently.

### Pipeline Stages Overview

| Stage | Tools | Purpose |
|-------|-------|---------|
| **Build** | `go build`, `mvn package`, `pip install` | Compile code and resolve dependencies |
| **Lint** | `golangci-lint`, `ruff`, `checkstyle`, `eslint` | Enforce code quality standards |
| **Test** | `go test`, `pytest`, `JUnit`, `jest` | Unit, integration, and e2e tests |
| **SAST** | SonarQube, Semgrep, CodeQL | Static application security testing |
| **SCA** | Trivy, Snyk, Dependabot | Dependency vulnerability scanning |
| **Image Build** | Docker, Kaniko, Jib, Buildpacks | Build OCI container image |
| **Image Push** | `docker push`, `crane push` | Push to container registry |
| **Deploy Staging** | `kubectl`, Helm, ArgoCD | Deploy to staging environment |
| **Smoke Test** | `curl`, k6, Playwright | Verify deployment health |
| **Deploy Production** | Helm, ArgoCD, Spinnaker | Deploy to production (manual gate) |
| **Verify** | Health checks, Prometheus alerts | Confirm production stability |

## GitHub Actions

GitHub Actions is the most popular CI/CD platform for open-source and GitHub-hosted projects. Workflows are defined in `.github/workflows/` as YAML files.

### Complete Go Project Pipeline

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

### Python Project Pipeline

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

GitLab CI uses `.gitlab-ci.yml` at the repository root. It supports Docker-in-Docker (DinD) and Kaniko for building images inside CI.

### Complete Multi-Language Pipeline

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

Jenkins uses a `Jenkinsfile` (Groovy DSL) for pipeline-as-code. It supports declarative and scripted pipelines.

### Declarative Pipeline for Java (Maven)

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

## Kaniko: Building Images Without Docker

Kaniko builds container images inside Kubernetes pods without requiring a Docker daemon. This is essential for security-conscious environments that prohibit privileged containers.

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

## Artifact Management

Artifacts produced during CI (binaries, test reports, SBOM, scan results) should be stored and versioned.

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

## CI/CD Best Practices

| Practice | Description |
|----------|-------------|
| **Fast feedback** | Keep CI under 10 minutes; parallelize stages |
| **Fail fast** | Run lint and unit tests before expensive image builds |
| **Cache aggressively** | Cache dependencies (`go mod`, `pip`, `mvn`, `npm`) |
| **Immutable tags** | Use commit SHA or semver, never `:latest` in production |
| **Secrets management** | Use CI platform secrets, never hardcode credentials |
| **Branch protection** | Require CI pass + code review before merge |
| **Environment gates** | Manual approval for production deployments |
| **Rollback plan** | Always have `helm rollback` or `kubectl rollout undo` ready |
| **Notifications** | Alert on failure via Slack, Teams, or email |
| **Audit trail** | Every deployment is traceable to a Git commit |
