# 1. Docker 基础

```{mermaid}
flowchart LR
    Client[Docker Client] -->|REST API| Daemon[Docker Daemon]
    Daemon --> Images[(Images)]
    Daemon --> Containers[Containers]
    Daemon --> Volumes[(Volumes)]
    Daemon --> Networks[Networks]
    Daemon -->|pull/push| Registry[Registry]
```

## 架构概述

Docker 采用客户端-服务器(Client-Server)架构。Docker **守护进程(Daemon)**（`dockerd`）负责管理镜像(Image)、容器(Container)、网络(Network)和数据卷(Volume)。Docker **客户端(Client)**（`docker`）通过 REST API 向守护进程发送命令。

## 安装

### Ubuntu

```bash
# Remove old versions
sudo apt-get remove docker docker-engine docker.io containerd runc

# Install prerequisites
sudo apt-get update
sudo apt-get install ca-certificates curl gnupg

# Add Docker GPG key and repo
sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] \
  https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | \
  sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# Install Docker Engine
sudo apt-get update
sudo apt-get install docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# Post-install: run without sudo
sudo usermod -aG docker $USER
newgrp docker
```

### macOS

```bash
# Using Homebrew
brew install --cask docker
# Or download Docker Desktop from https://www.docker.com/products/docker-desktop
```

## 核心概念

| 概念 | 说明 |
|------|------|
| **镜像(Image)** | 只读模板，包含创建容器所需的指令和文件系统 |
| **容器(Container)** | 镜像的可运行实例 |
| **数据卷(Volume)** | 持久化数据存储 |
| **网络(Network)** | 容器之间的通信通道 |
| **镜像仓库(Registry)** | 镜像的存储与分发服务（如 Docker Hub、Harbor 等） |

## 容器生命周期

```{mermaid}
stateDiagram-v2
    [*] --> Created: docker create
    Created --> Running: docker start
    Running --> Paused: docker pause
    Paused --> Running: docker unpause
    Running --> Stopped: docker stop
    Stopped --> Running: docker start
    Stopped --> Removed: docker rm
    Running --> Removed: docker rm -f
    Removed --> [*]
```

## 常用命令

### 镜像管理

```bash
# Pull an image
docker pull nginx:1.25-alpine

# List images
docker images

# Build an image
docker build -t myapp:1.0 .

# Remove an image
docker rmi myapp:1.0

# Prune unused images
docker image prune -a
```

### 容器操作

```bash
# Run a container (detached, with port mapping and name)
docker run -d --name web -p 8080:80 nginx:1.25-alpine

# List running containers
docker ps

# List all containers (including stopped)
docker ps -a

# View logs
docker logs -f web

# Execute command in running container
docker exec -it web /bin/sh

# Inspect container details
docker inspect web

# View resource usage
docker stats web

# Stop and remove
docker stop web
docker rm web
```

### 数据卷管理

```bash
# Create a named volume
docker volume create mydata

# Run with volume mount
docker run -d --name db \
  -v mydata:/var/lib/mysql \
  -e MYSQL_ROOT_PASSWORD=secret \
  mysql:8.0

# Bind mount (host directory)
docker run -d --name web \
  -v $(pwd)/html:/usr/share/nginx/html:ro \
  -p 8080:80 nginx:1.25-alpine

# List volumes
docker volume ls

# Remove unused volumes
docker volume prune
```

### 网络管理

```bash
# Create a network
docker network create mynet

# Run containers on the same network
docker run -d --name db --network mynet mysql:8.0
docker run -d --name app --network mynet -p 8080:8080 myapp:1.0

# Containers on the same network can reach each other by name
# e.g., app can connect to db:3306

# List networks
docker network ls

# Inspect network
docker network inspect mynet
```

## 实战示例

### 运行 Nginx

```bash
docker run -d --name nginx \
  -p 80:80 \
  -v $(pwd)/nginx.conf:/etc/nginx/nginx.conf:ro \
  -v $(pwd)/html:/usr/share/nginx/html:ro \
  nginx:1.25-alpine
```

### 运行 MySQL

```bash
docker run -d --name mysql \
  -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=rootpass \
  -e MYSQL_DATABASE=mydb \
  -e MYSQL_USER=app \
  -e MYSQL_PASSWORD=apppass \
  -v mysql_data:/var/lib/mysql \
  mysql:8.0
```

### 运行 Redis

```bash
docker run -d --name redis \
  -p 6379:6379 \
  -v redis_data:/data \
  redis:7-alpine redis-server --appendonly yes
```

## 镜像分层机制

Docker 镜像采用分层(Layer)结构构建。Dockerfile 中的每条指令都会创建一个新的层。各层之间可以被缓存和共享，从而节省磁盘空间并加快构建速度。

```bash
# View image layers
docker history nginx:1.25-alpine

# View image size
docker images --format "{{.Repository}}:{{.Tag}} {{.Size}}"
```

## 清理资源

```bash
# Remove all stopped containers
docker container prune

# Remove all unused resources (containers, networks, images, volumes)
docker system prune -a --volumes

# Check disk usage
docker system df
```
