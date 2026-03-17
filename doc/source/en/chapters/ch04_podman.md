# 4. Podman Container Engine

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

## Podman vs Docker

| Feature | Docker | Podman |
|---------|--------|--------|
| Architecture | Client-server (daemon) | Daemonless (fork-exec) |
| Root required | Yes (default) | No (rootless by default) |
| Pod support | No (compose only) | Native pods |
| Systemd integration | Limited | Native (quadlet) |
| K8s YAML generation | No | `podman generate kube` |
| CLI compatibility | — | Drop-in replacement |
| Compose support | Native | podman-compose / docker-compose |

## Installation

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

## Basic Commands (Docker-compatible)

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

## Rootless Containers

```bash
# Podman runs rootless by default
podman run -d --name web -p 8080:80 nginx:1.25-alpine

# Check user namespace mapping
podman unshare cat /proc/self/uid_map

# Rootless networking uses slirp4netns or pasta
podman run --network pasta -p 8080:80 nginx:1.25-alpine
```

## Pods (K8s-like grouping)

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

## Generate Kubernetes YAML

```bash
# Generate K8s YAML from a running pod
podman generate kube myapp > myapp-k8s.yaml

# Play K8s YAML with podman
podman play kube myapp-k8s.yaml

# Tear down
podman play kube --down myapp-k8s.yaml
```

## Systemd Integration (Quadlet)

Create a `.container` file in `~/.config/containers/systemd/` (rootless) or `/etc/containers/systemd/` (root):

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

## Migration from Docker to Podman

1. **Install Podman** and alias `docker=podman`
2. **Replace Docker socket**: enable `podman.socket` for tools that need it
3. **Update CI/CD**: replace `docker` commands with `podman`
4. **Systemd services**: convert Docker systemd units to Quadlet files
5. **Compose files**: use `podman-compose` or `docker-compose` with podman socket
6. **Test rootless**: ensure your workloads work without root

```bash
# Verify compatibility
podman info
podman system connection list
```
