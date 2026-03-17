# 7. Minikube 本地开发

```{mermaid}
flowchart TB
    subgraph Host["Host Machine"]
        MK[minikube CLI]
        KC[kubectl]
    end
    subgraph VM["Minikube VM / Container"]
        CP[Control Plane]
        W[Worker]
        CP --> ETCD[(etcd)]
        CP --> API[API Server]
        W --> KL[kubelet]
        W --> CR[containerd]
        CR --> P1[Pod]
        CR --> P2[Pod]
    end
    MK --> VM
    KC --> API
```

## 安装

```bash
# macOS
brew install minikube

# Linux (amd64)
curl -LO https://storage.googleapis.com/minikube/releases/latest/minikube-linux-amd64
sudo install minikube-linux-amd64 /usr/local/bin/minikube

# Verify
minikube version
```

## 集群管理

```bash
# Start with default driver (auto-detect)
minikube start

# Start with specific driver and resources
minikube start --driver=docker --cpus=4 --memory=8192 --disk-size=40g

# Start with specific K8s version
minikube start --kubernetes-version=v1.29.0

# Multi-node cluster
minikube start --nodes=3

# Stop cluster (preserves state)
minikube stop

# Delete cluster
minikube delete

# Delete all clusters and profiles
minikube delete --all --purge

# Status
minikube status

# SSH into node
minikube ssh
```

## 驱动选项

| 驱动 | 支持平台 | 说明 |
|------|----------|------|
| `docker` | 全平台 | 推荐方式，在 Docker 容器中运行 K8s |
| `hyperkit` | macOS | 轻量级虚拟化引擎 |
| `virtualbox` | 全平台 | 跨平台虚拟机方案 |
| `qemu` | Linux/macOS | 基于 KVM 的虚拟化 |
| `none` | Linux | 直接在宿主机上运行（需要 root 权限） |

## 插件（Addons）

```bash
# List available addons
minikube addons list

# Enable essential addons
minikube addons enable dashboard
minikube addons enable ingress
minikube addons enable metrics-server
minikube addons enable registry

# Open dashboard
minikube dashboard

# Disable addon
minikube addons disable dashboard
```

## 访问服务

```bash
# Get service URL
minikube service myapp --url

# Open service in browser
minikube service myapp

# Tunnel for LoadBalancer services
minikube tunnel
# (runs in foreground, assigns external IPs to LoadBalancer services)

# Port forward (alternative)
kubectl port-forward svc/myapp 8080:80
```

## 使用本地 Docker 镜像

```bash
# Point shell to minikube's Docker daemon
eval $(minikube docker-env)

# Build image (now available inside minikube)
docker build -t myapp:dev .

# Use in K8s (set imagePullPolicy: Never)
kubectl run myapp --image=myapp:dev --image-pull-policy=Never

# Reset Docker env
eval $(minikube docker-env -u)
```

## 资源配置

```bash
# View current config
minikube config view

# Set defaults
minikube config set cpus 4
minikube config set memory 8192
minikube config set driver docker
```

## 同类工具对比

| 工具 | 节点数 | 启动速度 | 资源占用 | 适用场景 |
|------|--------|----------|----------|----------|
| **minikube** | 1-N | 中等 | 中等 | 通用本地开发 |
| **kind** | 1-N | 快 | 低 | CI/CD、自动化测试 |
| **k3d** | 1-N | 快 | 低 | 轻量级、边缘计算场景 |
| **Docker Desktop** | 1 | 慢 | 高 | Docker + K8s 一体化使用 |

### kind（Kubernetes IN Docker）

```bash
# Install
brew install kind

# Create cluster
kind create cluster --name dev

# Create multi-node cluster
cat <<EOF | kind create cluster --config=-
kind: Cluster
apiVersion: kind.x-k8s.io/v1alpha4
nodes:
- role: control-plane
- role: worker
- role: worker
EOF

# Load local image
kind load docker-image myapp:dev --name dev

# Delete
kind delete cluster --name dev
```

### k3d（k3s in Docker）

```bash
# Install
brew install k3d

# Create cluster
k3d cluster create dev --servers 1 --agents 2 -p "8080:80@loadbalancer"

# Import image
k3d image import myapp:dev -c dev

# Delete
k3d cluster delete dev
```
