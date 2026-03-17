# Minikube 本地开发

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

## Installation

```bash
# macOS
brew install minikube

# Linux (amd64)
curl -LO https://storage.googleapis.com/minikube/releases/latest/minikube-linux-amd64
sudo install minikube-linux-amd64 /usr/local/bin/minikube

# Verify
minikube version
```

## Cluster Management

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

## Driver Options

| Driver | Platform | Notes |
|--------|----------|-------|
| `docker` | All | Recommended, runs K8s in Docker containers |
| `hyperkit` | macOS | Lightweight hypervisor |
| `virtualbox` | All | Cross-platform VM |
| `qemu` | Linux/macOS | KVM-based |
| `none` | Linux | Runs directly on host (requires root) |

## Addons

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

## Accessing Services

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

## Using Local Docker Images

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

## Resource Configuration

```bash
# View current config
minikube config view

# Set defaults
minikube config set cpus 4
minikube config set memory 8192
minikube config set driver docker
```

## Alternatives Comparison

| Tool | Nodes | Speed | Resource Usage | Best For |
|------|-------|-------|----------------|----------|
| **minikube** | 1-N | Medium | Medium | General local dev |
| **kind** | 1-N | Fast | Low | CI/CD, testing |
| **k3d** | 1-N | Fast | Low | Lightweight, edge |
| **Docker Desktop** | 1 | Slow | High | Docker + K8s combo |

### kind (Kubernetes IN Docker)

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

### k3d (k3s in Docker)

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
