# 25. Java 应用上 K8s

```{mermaid}
flowchart TB
    subgraph Build
        Code[Spring Boot] --> Jib[Jib / Buildpacks]
        Jib --> Image[JRE Image<br/>~200MB]
    end
    subgraph K8s["Kubernetes"]
        Deploy[Deployment] --> Pod1["Pod<br/>-XX:MaxRAMPercentage=75"]
        Deploy --> Pod2[Pod]
        Svc[Service] --> Pod1
        Svc --> Pod2
        Actuator["/actuator/health"] -.-> Pod1
    end
    Image --> Deploy
```

## Spring Boot 应用

### 健康检查（Actuator）

```yaml
# application.yml
server:
  port: 8080
  shutdown: graceful

spring:
  lifecycle:
    timeout-per-shutdown-phase: 30s

management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus,metrics
  endpoint:
    health:
      show-details: always
      probes:
        enabled: true    # enables /actuator/health/liveness and /readiness
  metrics:
    export:
      prometheus:
        enabled: true
    tags:
      application: ${spring.application.name}
```

### Prometheus 指标（Micrometer）

```xml
<!-- pom.xml -->
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

## 容器化方案

### 方案一：多阶段 Dockerfile

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

ENTRYPOINT ["java", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:+UseG1GC", \
  "-XX:+UseContainerSupport", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
```

### 方案二：Jib（无需 Dockerfile）

```xml
<!-- pom.xml -->
<plugin>
    <groupId>com.google.cloud.tools</groupId>
    <artifactId>jib-maven-plugin</artifactId>
    <version>3.4.0</version>
    <configuration>
        <from>
            <image>eclipse-temurin:21-jre-alpine</image>
        </from>
        <to>
            <image>registry.example.com/myapp</image>
            <tags>
                <tag>${project.version}</tag>
                <tag>latest</tag>
            </tags>
        </to>
        <container>
            <jvmFlags>
                <jvmFlag>-XX:MaxRAMPercentage=75.0</jvmFlag>
                <jvmFlag>-XX:+UseG1GC</jvmFlag>
            </jvmFlags>
            <ports>
                <port>8080</port>
            </ports>
            <user>1001</user>
        </container>
    </configuration>
</plugin>
```

```bash
# Build and push (no Docker daemon needed!)
mvn compile jib:build

# Build to local Docker daemon
mvn compile jib:dockerBuild
```

### 方案三：Cloud Native Buildpacks

```bash
# Spring Boot 2.3+ has built-in support
./gradlew bootBuildImage --imageName=registry.example.com/myapp:1.0

# Or with pack CLI
pack build myapp --builder paketobuildpacks/builder-jammy-base
```

## 容器环境下的 JVM 调优

```bash
# Key JVM flags for containers
java \
  -XX:MaxRAMPercentage=75.0 \       # Use 75% of container memory limit
  -XX:+UseContainerSupport \          # Detect container limits (default since JDK 10)
  -XX:+UseG1GC \                      # G1 garbage collector
  -XX:+ExitOnOutOfMemoryError \       # Exit on OOM (let K8s restart)
  -XX:+HeapDumpOnOutOfMemoryError \   # Dump heap on OOM
  -XX:HeapDumpPath=/tmp/heapdump.hprof \
  -jar app.jar
```

| 参数 | 用途 |
|------|------|
| `-XX:MaxRAMPercentage=75.0` | 堆内存占容器内存限制的 75% |
| `-XX:+UseContainerSupport` | 自动检测 cgroup 内存限制 |
| `-XX:+UseG1GC` | 使用 G1 低延迟垃圾回收器 |
| `-XX:+ExitOnOutOfMemoryError` | OOM 时退出进程，交由 K8s 重启 |
| `-Xss256k` | 减小线程栈大小 |

## Kubernetes 资源清单

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: java-api
spec:
  replicas: 3
  selector:
    matchLabels:
      app: java-api
  template:
    metadata:
      labels:
        app: java-api
      annotations:
        prometheus.io/scrape: "true"
        prometheus.io/port: "8080"
        prometheus.io/path: "/actuator/prometheus"
    spec:
      containers:
        - name: java-api
          image: registry.example.com/java-api:1.0.0
          ports:
            - containerPort: 8080
          env:
            - name: SPRING_PROFILES_ACTIVE
              value: "production"
            - name: JAVA_OPTS
              value: "-XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"
            - name: SPRING_DATASOURCE_URL
              valueFrom:
                configMapKeyRef:
                  name: java-api-config
                  key: datasource-url
            - name: SPRING_DATASOURCE_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: java-api-secret
                  key: db-password
          resources:
            requests:
              cpu: "500m"
              memory: "512Mi"
            limits:
              cpu: "2"
              memory: "1Gi"
          livenessProbe:
            httpGet:
              path: /actuator/health/liveness
              port: 8080
            initialDelaySeconds: 30
            periodSeconds: 15
            failureThreshold: 3
          readinessProbe:
            httpGet:
              path: /actuator/health/readiness
              port: 8080
            initialDelaySeconds: 20
            periodSeconds: 10
          startupProbe:
            httpGet:
              path: /actuator/health
              port: 8080
            failureThreshold: 30
            periodSeconds: 10
      terminationGracePeriodSeconds: 30
---
apiVersion: v1
kind: Service
metadata:
  name: java-api
spec:
  selector:
    app: java-api
  ports:
    - port: 80
      targetPort: 8080
---
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: java-api
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: java-api
  minReplicas: 2
  maxReplicas: 10
  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: 70
```

## Java vs Go vs Python 在 K8s 上的对比

| 指标 | Java (Spring Boot) | Go | Python (FastAPI) |
|------|--------------------|----|------------------|
| 镜像大小 | 约 200-300MB | 约 10-15MB | 约 150-200MB |
| 启动时间 | 3-10 秒 | < 100ms | 1-3 秒 |
| 空闲内存占用 | 约 200-400MB | 约 10-30MB | 约 50-100MB |
| CPU 效率 | 高（JIT 编译优化） | 高 | 中等 |
| 生态系统 | 非常成熟 | 快速增长中 | 丰富 |
| 最佳适用场景 | 企业级复杂应用 | 微服务、基础设施工具 | AI/ML、快速原型开发 |
