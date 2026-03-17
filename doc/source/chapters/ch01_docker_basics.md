# Docker 基础

```{mermaid}
flowchart LR
    Client[Docker Client] -->|REST API| Daemon[Docker Daemon]
    Daemon --> Images[(Images)]
    Daemon --> Containers[Containers]
    Daemon --> Volumes[(Volumes)]
    Daemon --> Networks[Networks]
    Daemon -->|pull/push| Registry[Registry]
```

## Architecture

Docker uses a client-server architecture. The Docker **daemon** (`dockerd`) manages images, containers, networks, and volumes. The Docker **client** (`docker`) sends commands to the daemon via REST API.

## Installation

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

## Core Concepts

| Concept | Description |
|---------|-------------|
| **Image** | Read-only template with instructions for creating a container |
| **Container** | Runnable instance of an image |
| **Volume** | Persistent data storage |
| **Network** | Communication channel between containers |
| **Registry** | Storage for Docker images (Docker Hub, Harbor, etc.) |

## Container Lifecycle

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

## Essential Commands

### Image Management

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

### Container Operations

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

### Volume Management

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

### Network Management

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

## Practical Examples

### Run Nginx

```bash
docker run -d --name nginx \
  -p 80:80 \
  -v $(pwd)/nginx.conf:/etc/nginx/nginx.conf:ro \
  -v $(pwd)/html:/usr/share/nginx/html:ro \
  nginx:1.25-alpine
```

### Run MySQL

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

### Run Redis

```bash
docker run -d --name redis \
  -p 6379:6379 \
  -v redis_data:/data \
  redis:7-alpine redis-server --appendonly yes
```

## Image Layers

Docker images are built in layers. Each instruction in a Dockerfile creates a new layer. Layers are cached and shared between images, which saves disk space and speeds up builds.

```bash
# View image layers
docker history nginx:1.25-alpine

# View image size
docker images --format "{{.Repository}}:{{.Tag}} {{.Size}}"
```

## Cleanup

```bash
# Remove all stopped containers
docker container prune

# Remove all unused resources (containers, networks, images, volumes)
docker system prune -a --volumes

# Check disk usage
docker system df
```
