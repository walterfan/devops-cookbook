# 2. Dockerfile 最佳实践

```{mermaid}
flowchart LR
    subgraph "Multi-stage Build"
        A[Stage 1: Build] -->|COPY binary| B[Stage 2: Runtime]
    end
    B --> C[Minimal Image<br/>scratch / distroless / alpine]
    style A fill:#f9f,stroke:#333
    style B fill:#9f9,stroke:#333
    style C fill:#ff9,stroke:#333
```

## Dockerfile 指令参考

| 指令 | 用途 | 示例 |
|------|------|------|
| `FROM` | 指定基础镜像 | `FROM golang:1.22-alpine AS builder` |
| `RUN` | 执行命令 | `RUN apt-get update && apt-get install -y curl` |
| `COPY` | 从构建上下文复制文件 | `COPY . /app` |
| `ADD` | 复制文件，支持自动解压归档和远程 URL | `ADD app.tar.gz /app` |
| `WORKDIR` | 设置工作目录 | `WORKDIR /app` |
| `ENV` | 设置环境变量 | `ENV APP_PORT=8080` |
| `ARG` | 定义构建时变量 | `ARG VERSION=1.0` |
| `EXPOSE` | 声明容器监听的端口 | `EXPOSE 8080` |
| `VOLUME` | 创建挂载点 | `VOLUME /data` |
| `USER` | 指定运行时用户 | `USER 1001` |
| `CMD` | 默认启动命令（可被覆盖） | `CMD ["./app"]` |
| `ENTRYPOINT` | 固定入口命令 | `ENTRYPOINT ["./app"]` |
| `HEALTHCHECK` | 容器健康检查 | `HEALTHCHECK CMD curl -f http://localhost:8080/health` |

## 多阶段构建(Multi-stage Build)

多阶段构建能够将构建依赖与运行时环境分离，从而大幅缩减最终镜像的体积。

### Go 应用

```dockerfile
# Stage 1: Build
FROM golang:1.22-alpine AS builder
WORKDIR /app
COPY go.mod go.sum ./
RUN go mod download
COPY . .
RUN CGO_ENABLED=0 GOOS=linux go build -ldflags="-s -w" -o /app/server ./cmd/server

# Stage 2: Runtime
FROM scratch
COPY --from=builder /app/server /server
COPY --from=builder /etc/ssl/certs/ca-certificates.crt /etc/ssl/certs/
EXPOSE 8080
ENTRYPOINT ["/server"]
```

### Python 应用

```dockerfile
# Stage 1: Build dependencies
FROM python:3.12-slim AS builder
WORKDIR /app
RUN pip install --no-cache-dir poetry
COPY pyproject.toml poetry.lock ./
RUN poetry export -f requirements.txt -o requirements.txt --without-hashes
RUN pip install --no-cache-dir --prefix=/install -r requirements.txt

# Stage 2: Runtime
FROM python:3.12-slim
WORKDIR /app
COPY --from=builder /install /usr/local
COPY . .
RUN useradd -r -s /bin/false appuser
USER appuser
EXPOSE 8000
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]
```

### Java 应用（Spring Boot）

```dockerfile
# Stage 1: Build
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app
COPY gradle/ gradle/
COPY gradlew build.gradle.kts settings.gradle.kts ./
RUN ./gradlew dependencies --no-daemon
COPY src/ src/
RUN ./gradlew bootJar --no-daemon

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S app && adduser -S app -G app
COPY --from=builder /app/build/libs/*.jar app.jar
USER app
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=3s \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
```

## 层缓存优化

```dockerfile
# BAD: Invalidates cache on any code change
COPY . /app
RUN pip install -r requirements.txt

# GOOD: Dependencies cached separately
COPY requirements.txt /app/
RUN pip install -r requirements.txt
COPY . /app
```

合理利用层缓存可以显著加快构建速度。核心原则是：**将变化频率低的内容（如依赖声明文件）放在前面，变化频率高的内容（如业务代码）放在后面**。上面的反面示例中，任何代码改动都会导致依赖重新安装；而正面示例中，只要 `requirements.txt` 没有变化，依赖安装层就会命中缓存。

## .dockerignore

务必创建 `.dockerignore` 文件来排除不需要的文件，以减小构建上下文(Build Context)的体积：

```text
.git
.gitignore
.env
*.md
LICENSE
docker-compose*.yml
Dockerfile*
__pycache__
*.pyc
node_modules
.vscode
.idea
build/
dist/
target/
```

## 安全最佳实践

```dockerfile
# 1. Use specific image tags (never :latest in production)
FROM python:3.12.3-slim

# 2. Run as non-root user
RUN groupadd -r app && useradd -r -g app app
USER app

# 3. Read-only filesystem where possible
# (set at runtime: docker run --read-only)

# 4. No unnecessary packages
RUN apt-get update && apt-get install -y --no-install-recommends \
    curl \
    && rm -rf /var/lib/apt/lists/*

# 5. Use COPY instead of ADD (unless you need tar extraction)
COPY app.py /app/

# 6. Scan images
# docker scout cves myimage:1.0
# trivy image myimage:1.0
```

安全方面的关键要点：

1. **使用明确的镜像标签**——生产环境中切勿使用 `:latest`
2. **以非 root 用户运行**——降低容器逃逸后的风险
3. **尽量使用只读文件系统**——运行时通过 `--read-only` 参数启用
4. **不安装多余的软件包**——减小攻击面
5. **优先使用 `COPY` 而非 `ADD`**——除非确实需要自动解压功能
6. **定期扫描镜像漏洞**——使用 Trivy、Docker Scout 等工具

## 镜像瘦身技巧

| 技巧 | 效果 |
|------|------|
| 使用 Alpine 基础镜像 | 约 5MB，相比 Debian 的约 120MB |
| 多阶段构建 | 最终镜像中不包含构建工具 |
| `--no-install-recommends` | 跳过 apt 的可选推荐包 |
| `rm -rf /var/lib/apt/lists/*` | 清除 apt 缓存 |
| `--no-cache-dir`（pip） | 跳过 pip 缓存 |
| 合并 RUN 指令 | 减少镜像层数 |
| `.dockerignore` | 缩小构建上下文体积 |

## 健康检查(HEALTHCHECK)

```dockerfile
# HTTP health check
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD curl -f http://localhost:8080/health || exit 1

# TCP health check (no curl needed)
HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
  CMD nc -z localhost 8080 || exit 1
```
