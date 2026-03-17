# 4. Podman 容器引擎

```{mermaid}
flowchart LR
    subgraph Docker
        DC[docker CLI] --> DD[dockerd<br/>daemon]
        DD --> C1[Container]
    end
    subgraph Podman
        PC[podman CLI] --> R[conmon + runc<br/>no daemon]
        R --> C2[Container]
        PC --> Pod[Pod<br/>shared namespace]
        Pod --> C3[Container A]
        Pod --> C4[Container B]
    end
    style DD fill:#f99
    style R fill:#9f9
```

## Podman 与 Docker 对比

| 特性 | Docker | Podman |
|------|--------|--------|
| 架构 | 客户端-服务器（需要守护进程） | 无守护进程（fork-exec 模型） |
| 是否需要 root 权限 | 默认需要 | 默认无需 root（Rootless） |
| Pod 支持 | 不支持（仅通过 Compose 编排） | 原生支持 Pod |
| Systemd 集成 | 有限 | 原生支持（Quadlet） |
| 生成 K8s YAML | 不支持 | `podman generate kube` |
| CLI 兼容性 | — | 可作为 Docker 的直接替代 |
| Compose 支持 | 原生支持 | 通过 podman-compose 或 docker-compose |

## 安装

```bash
# Ubuntu
sudo apt-get install podman

# macOS
brew install podman
podman machine init
podman machine start

# Alias for Docker compatibility
alias docker=podman
```

## 基本命令（兼容 Docker）

```bash
# Run a container
podman run -d --name web -p 8080:80 nginx:1.25-alpine

# List containers
podman ps -a

# Logs
podman logs -f web

# Exec
podman exec -it web /bin/sh

# Stop and remove
podman stop web && podman rm web

# Build image
podman build -t myapp:1.0 .

# Push to registry
podman push myapp:1.0 docker.io/myuser/myapp:1.0
```

Podman 的命令行接口与 Docker 几乎完全一致，大多数情况下只需将 `docker` 替换为 `podman` 即可。

## 无根容器(Rootless Containers)

```bash
# Podman runs rootless by default
podman run -d --name web -p 8080:80 nginx:1.25-alpine

# Check user namespace mapping
podman unshare cat /proc/self/uid_map

# Rootless networking uses slirp4netns or pasta
podman run --network pasta -p 8080:80 nginx:1.25-alpine
```

Podman 默认以非 root 用户运行容器，通过用户命名空间(User Namespace)实现权限隔离。无根模式下的网络由 slirp4netns 或 pasta 提供支持。

## Pod（类似 Kubernetes 的分组机制）

```bash
# Create a pod with shared network namespace
podman pod create --name myapp -p 8080:80 -p 3306:3306

# Add containers to the pod
podman run -d --pod myapp --name web nginx:1.25-alpine
podman run -d --pod myapp --name db \
  -e MYSQL_ROOT_PASSWORD=secret mysql:8.0

# Containers in the same pod share localhost
# web can reach db at localhost:3306

# List pods
podman pod ls

# Stop/remove pod (and all containers)
podman pod stop myapp
podman pod rm myapp
```

同一个 Pod 内的容器共享网络命名空间，彼此之间可以通过 `localhost` 直接通信，这与 Kubernetes 中 Pod 的行为一致。

## 生成 Kubernetes YAML

```bash
# Generate K8s YAML from a running pod
podman generate kube myapp > myapp-k8s.yaml

# Play K8s YAML with podman
podman play kube myapp-k8s.yaml

# Tear down
podman play kube --down myapp-k8s.yaml
```

这一功能使得开发者可以在本地用 Podman 调试，然后将配置直接导出为 Kubernetes YAML 部署到集群中，实现从开发到生产的平滑过渡。

## Systemd 集成（Quadlet）

在 `~/.config/containers/systemd/`（无根模式）或 `/etc/containers/systemd/`（root 模式）下创建 `.container` 文件：

```ini
# ~/.config/containers/systemd/web.container
[Container]
Image=nginx:1.25-alpine
PublishPort=8080:80
Volume=web_data:/usr/share/nginx/html:ro

[Service]
Restart=always

[Install]
WantedBy=default.target
```

```bash
# Reload and start
systemctl --user daemon-reload
systemctl --user start web
systemctl --user enable web
systemctl --user status web
```

Quadlet 是 Podman 提供的 Systemd 原生集成方案，可以像管理系统服务一样管理容器，支持开机自启、自动重启等特性。

## Podman Compose

```bash
# Install podman-compose
pip install podman-compose

# Use existing docker-compose.yml
podman-compose up -d
podman-compose down

# Or use docker-compose with podman socket
systemctl --user start podman.socket
export DOCKER_HOST=unix://$XDG_RUNTIME_DIR/podman/podman.sock
docker-compose up -d
```

## 从 Docker 迁移到 Podman

1. **安装 Podman** 并设置别名 `alias docker=podman`
2. **替换 Docker Socket**：启用 `podman.socket`，为依赖 Docker Socket 的工具提供兼容接口
3. **更新 CI/CD 流水线**：将 `docker` 命令替换为 `podman`
4. **Systemd 服务迁移**：将 Docker 的 Systemd 单元文件转换为 Quadlet 文件
5. **Compose 文件**：使用 `podman-compose` 或通过 Podman Socket 运行 `docker-compose`
6. **测试无根模式**：确保所有工作负载在非 root 环境下正常运行

```bash
# Verify compatibility
podman info
podman system connection list
```
