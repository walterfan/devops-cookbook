# 29. 故障排查手册

```{mermaid}
flowchart TB
    Start[Pod Issue] --> Status{Pod Status?}
    Status -->|Pending| Pending[Check Events<br/>Resources / Scheduling]
    Status -->|CrashLoopBackOff| Crash[Check Logs<br/>Previous Container]
    Status -->|ImagePullBackOff| Image[Check Image Name<br/>Registry Auth]
    Status -->|OOMKilled| OOM[Increase Memory<br/>Check for Leaks]
    Status -->|Running but unhealthy| Health[Check Probes<br/>Port / Path]
    Pending --> Fix[Fix & Redeploy]
    Crash --> Fix
    Image --> Fix
    OOM --> Fix
    Health --> Fix
```

## Pod 卡在 Pending 状态

Pod 处于 Pending 状态意味着调度器(Scheduler)无法将其分配到任何节点上。常见原因包括资源不足、节点亲和性不匹配或 PVC 未绑定：

```bash
# Check events
kubectl describe pod <pod-name>
kubectl get events --sort-by=.lastTimestamp

# Common causes:
# 1. Insufficient resources
kubectl describe node <node-name> | grep -A 5 "Allocated resources"
kubectl top nodes

# 2. No matching node (taints/tolerations, nodeSelector, affinity)
kubectl get nodes --show-labels
kubectl describe node <node-name> | grep Taints

# 3. PVC not bound
kubectl get pvc
kubectl describe pvc <pvc-name>
```

## CrashLoopBackOff

CrashLoopBackOff 表示容器反复启动又崩溃。Kubernetes 会以指数退避(Exponential Backoff)的方式不断重启容器：

```bash
# Check current logs
kubectl logs <pod-name>

# Check previous container logs (after crash)
kubectl logs <pod-name> --previous

# Check container exit code
kubectl describe pod <pod-name> | grep -A 10 "Last State"

# Common causes:
# - Application error (check logs)
# - Missing config/secret (check env vars)
# - Wrong command/entrypoint
# - Health check failing too early (increase initialDelaySeconds)
# - Permission denied (check securityContext)

# Debug with ephemeral container
kubectl debug -it <pod-name> --image=busybox --target=<container-name>
```

## ImagePullBackOff

ImagePullBackOff 表示 Kubernetes 无法拉取容器镜像。通常是镜像名称错误、标签不存在或缺少私有仓库的认证信息：

```bash
# Check events for details
kubectl describe pod <pod-name> | grep -A 5 "Events"

# Common causes:
# 1. Wrong image name/tag
# 2. Private registry without imagePullSecrets
kubectl get secret regcred -o yaml
# 3. Registry unreachable

# Fix: add imagePullSecrets
kubectl create secret docker-registry regcred \
  --docker-server=registry.example.com \
  --docker-username=user \
  --docker-password=pass
```

## OOMKilled

OOMKilled 表示容器因超出内存限制(Memory Limit)而被内核强制终止。需要排查是内存限制设置过低还是应用存在内存泄漏：

```bash
# Check if OOMKilled
kubectl describe pod <pod-name> | grep OOMKilled

# Check resource usage
kubectl top pod <pod-name>

# Solutions:
# 1. Increase memory limits
# 2. Fix memory leaks in application
# 3. For Java: set -XX:MaxRAMPercentage=75.0
# 4. For Go: set GOMEMLIMIT
```

## 节点 NotReady

节点处于 NotReady 状态意味着 kubelet 无法正常与控制平面(Control Plane)通信。需要登录节点检查 kubelet 服务状态：

```bash
# Check node status
kubectl get nodes
kubectl describe node <node-name>

# Check kubelet
ssh <node> systemctl status kubelet
ssh <node> journalctl -u kubelet -f

# Common causes:
# - Disk pressure
# - Memory pressure
# - PID pressure
# - Network issues
# - kubelet crashed

# Check conditions
kubectl get node <node-name> -o jsonpath='{.status.conditions[*]}' | jq
```

## Service 无法访问

当 Service 无法访问时，需要逐步排查 Service 定义、Endpoints、Pod 标签匹配以及网络策略(NetworkPolicy)：

```bash
# 1. Check service exists and has endpoints
kubectl get svc <service-name>
kubectl get endpoints <service-name>

# 2. Check pod labels match service selector
kubectl get pods --show-labels
kubectl describe svc <service-name>

# 3. Test from within cluster
kubectl run debug --rm -it --image=nicolaka/netshoot -- bash
# Inside debug pod:
curl http://<service-name>.<namespace>.svc.cluster.local
nslookup <service-name>
nc -zv <service-name> <port>

# 4. Check NetworkPolicy
kubectl get networkpolicy -n <namespace>
```

## DNS 问题

集群内 DNS 解析失败通常与 CoreDNS 相关。需要检查 CoreDNS Pod 是否正常运行以及配置是否正确：

```bash
# Test DNS resolution
kubectl run dns-test --rm -it --image=busybox -- nslookup kubernetes.default

# Check CoreDNS pods
kubectl get pods -n kube-system -l k8s-app=kube-dns
kubectl logs -n kube-system -l k8s-app=kube-dns

# Check CoreDNS config
kubectl get configmap coredns -n kube-system -o yaml
```

## Ingress 502/504 错误

Ingress 返回 502 或 504 错误通常意味着后端服务(Backend Service)不可用或响应超时：

```bash
# Check ingress controller logs
kubectl logs -n ingress-nginx -l app.kubernetes.io/name=ingress-nginx

# Check backend service
kubectl describe ingress <ingress-name>

# Common causes:
# 502: Backend service not ready / wrong port
# 504: Backend timeout (increase proxy-read-timeout annotation)

# Test backend directly
kubectl port-forward svc/<backend-service> 8080:80
curl http://localhost:8080
```

## PVC 卡在 Pending 状态

PVC（持久卷声明）处于 Pending 状态意味着没有可用的 PV（持久卷）与之绑定。需要检查 StorageClass 和存储供应器(Provisioner)：

```bash
# Check PVC status
kubectl describe pvc <pvc-name>

# Common causes:
# 1. No matching PV (check StorageClass)
kubectl get sc
kubectl get pv

# 2. StorageClass provisioner not working
kubectl get pods -n kube-system | grep provisioner

# 3. Volume binding mode is WaitForFirstConsumer
# (PVC won't bind until a pod uses it)
```

## 调试工具

以下是 Kubernetes 环境中常用的调试工具和命令：

```bash
# Ephemeral debug container
kubectl debug -it <pod-name> --image=nicolaka/netshoot --target=<container>

# Debug node
kubectl debug node/<node-name> -it --image=ubuntu

# Network debugging
kubectl run netshoot --rm -it --image=nicolaka/netshoot -- bash
# tcpdump, dig, curl, nmap, iperf, etc.

# Copy files from pod
kubectl cp <pod-name>:/var/log/app.log ./app.log

# Port forward for debugging
kubectl port-forward pod/<pod-name> 8080:8080
kubectl port-forward svc/<service-name> 8080:80

# Resource usage
kubectl top pods --sort-by=cpu
kubectl top pods --sort-by=memory
kubectl top nodes
```

## 快速参考表

| 故障现象 | 首先检查 | 命令 |
|---------|---------|------|
| Pod Pending | 事件(Events) | `kubectl describe pod <name>` |
| CrashLoopBackOff | 日志 | `kubectl logs <name> --previous` |
| ImagePullBackOff | 镜像名称 | `kubectl describe pod <name>` |
| OOMKilled | 内存限制 | `kubectl top pod <name>` |
| Service 不可达 | Endpoints | `kubectl get endpoints <svc>` |
| DNS 解析失败 | CoreDNS | `kubectl logs -n kube-system -l k8s-app=kube-dns` |
| Ingress 502 | 后端健康状态 | `kubectl logs -n ingress-nginx -l app.kubernetes.io/name=ingress-nginx` |
| 节点 NotReady | kubelet | `ssh node journalctl -u kubelet` |
