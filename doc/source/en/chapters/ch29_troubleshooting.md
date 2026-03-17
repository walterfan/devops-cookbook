# 29. Troubleshooting Guide

```{mermaid}
flowchart TB
    Start[🔍 Issue Detected] --> Layer{Which Layer?}
    Layer -->|Pod| PodStatus{Pod Status?}
    Layer -->|Service| SvcCheck[Check Endpoints<br/>& Selectors]
    Layer -->|Network| NetCheck[DNS / NetworkPolicy<br/>/ CNI]
    Layer -->|Storage| StorCheck[PVC / StorageClass<br/>/ Provisioner]
    Layer -->|Node| NodeCheck[kubelet / Resources<br/>/ Disk / Memory]

    PodStatus -->|Pending| Pending[Events → Scheduling<br/>Resources / Taints / PVC]
    PodStatus -->|CrashLoopBackOff| Crash[Logs → Previous Container<br/>Exit Code / Config]
    PodStatus -->|ImagePullBackOff| ImgPull[Image Name / Tag<br/>Registry Auth / Network]
    PodStatus -->|OOMKilled| OOM[Memory Limits<br/>Application Profiling]
    PodStatus -->|Running ❌| Running[Probes / Port<br/>App-level Error]

    SvcCheck --> SvcFix[Fix Selector / Port<br/>/ NetworkPolicy]
    NetCheck --> NetFix[Fix DNS / Policy<br/>/ CNI Plugin]
    StorCheck --> StorFix[Fix StorageClass<br/>/ Provisioner / Quota]
    NodeCheck --> NodeFix[Drain / Restart kubelet<br/>/ Add Capacity]

    Pending --> Resolve[✅ Resolved]
    Crash --> Resolve
    ImgPull --> Resolve
    OOM --> Resolve
    Running --> Resolve
    SvcFix --> Resolve
    NetFix --> Resolve
    StorFix --> Resolve
    NodeFix --> Resolve
```

## General Debugging Workflow

Before diving into specific issues, follow this systematic approach:

```bash
# Step 1: Get the big picture
kubectl get pods -n <namespace> -o wide
kubectl get events -n <namespace> --sort-by=.lastTimestamp | tail -20

# Step 2: Describe the problematic resource
kubectl describe pod <pod-name> -n <namespace>

# Step 3: Check logs
kubectl logs <pod-name> -n <namespace>
kubectl logs <pod-name> -n <namespace> --previous    # after crash
kubectl logs <pod-name> -n <namespace> -c <container> # specific container

# Step 4: Check related resources
kubectl get svc,endpoints,ingress -n <namespace>
kubectl get pvc -n <namespace>
kubectl top pods -n <namespace>
kubectl top nodes
```

## Pod Stuck in Pending

A pod in `Pending` state means the scheduler cannot place it on any node.

### Diagnosis

```bash
# Check events for the reason
kubectl describe pod <pod-name> -n <namespace>
# Look for: "FailedScheduling" events

kubectl get events -n <namespace> --field-selector involvedObject.name=<pod-name>
```

### Common Causes and Solutions

**1. Insufficient CPU/Memory**

```bash
# Check node allocatable resources
kubectl describe nodes | grep -A 8 "Allocated resources"
kubectl top nodes

# Check if resource requests are too high
kubectl get pod <pod-name> -o jsonpath='{.spec.containers[*].resources}' | jq

# Solution: reduce requests, add nodes, or remove other workloads
kubectl scale deployment <other-deployment> --replicas=0 -n <namespace>
```

**2. Node Selector / Affinity Mismatch**

```bash
# Check pod's nodeSelector and affinity
kubectl get pod <pod-name> -o jsonpath='{.spec.nodeSelector}' | jq
kubectl get pod <pod-name> -o jsonpath='{.spec.affinity}' | jq

# Check available node labels
kubectl get nodes --show-labels

# Solution: add the required label to a node
kubectl label node <node-name> disktype=ssd
```

**3. Taints and Tolerations**

```bash
# Check node taints
kubectl describe node <node-name> | grep -A 3 Taints

# Check pod tolerations
kubectl get pod <pod-name> -o jsonpath='{.spec.tolerations}' | jq

# Solution: add toleration to pod or remove taint from node
kubectl taint nodes <node-name> key=value:NoSchedule-
```

**4. PVC Not Bound**

```bash
# Check PVC status
kubectl get pvc -n <namespace>
kubectl describe pvc <pvc-name> -n <namespace>

# Check if StorageClass exists and has a provisioner
kubectl get sc
kubectl describe sc <storage-class-name>

# Check if provisioner pods are running
kubectl get pods -n kube-system | grep -i provisioner
```

## CrashLoopBackOff

The container starts, crashes, and Kubernetes keeps restarting it with exponential backoff.

### Diagnosis

```bash
# Check current logs
kubectl logs <pod-name> -n <namespace>

# Check PREVIOUS container logs (crucial — current container may have no logs yet)
kubectl logs <pod-name> -n <namespace> --previous

# Check exit code and reason
kubectl describe pod <pod-name> -n <namespace> | grep -A 15 "Last State"
# Exit code 1: application error
# Exit code 137: OOMKilled (SIGKILL) or killed by kubelet
# Exit code 139: segfault (SIGSEGV)
# Exit code 143: SIGTERM (graceful shutdown failed)

# Check container command and args
kubectl get pod <pod-name> -o jsonpath='{.spec.containers[0].command}' | jq
kubectl get pod <pod-name> -o jsonpath='{.spec.containers[0].args}' | jq
```

### Common Causes and Solutions

**1. Application Error**

```bash
# Read the logs carefully
kubectl logs <pod-name> --previous | head -50

# Common issues:
# - Missing environment variable
# - Database connection refused
# - Config file not found
# - Permission denied

# Check environment variables
kubectl get pod <pod-name> -o jsonpath='{.spec.containers[0].env}' | jq

# Check mounted configmaps/secrets
kubectl get configmap <cm-name> -o yaml
kubectl get secret <secret-name> -o jsonpath='{.data}' | jq 'to_entries[] | {key: .key, value: (.value | @base64d)}'
```

**2. Health Check Failing Too Early**

```bash
# Check probe configuration
kubectl get pod <pod-name> -o jsonpath='{.spec.containers[0].livenessProbe}' | jq
kubectl get pod <pod-name> -o jsonpath='{.spec.containers[0].startupProbe}' | jq

# Solution: increase initialDelaySeconds or add a startupProbe
# For Java apps, startup can take 10-30 seconds
```

**3. Debug with Ephemeral Container**

```bash
# Attach a debug container to the running pod
kubectl debug -it <pod-name> --image=busybox --target=<container-name>

# Or create a copy of the pod with a different command
kubectl debug <pod-name> -it --copy-to=debug-pod --container=app -- sh

# Override the entrypoint to keep the container alive
kubectl run debug-pod --image=<same-image> --command -- sleep infinity
kubectl exec -it debug-pod -- sh
```

## ImagePullBackOff

Kubernetes cannot pull the container image.

### Diagnosis

```bash
# Check the exact error
kubectl describe pod <pod-name> | grep -A 5 "Events"
# Look for: "Failed to pull image" or "unauthorized"

# Common errors:
# - "manifest unknown" → wrong tag
# - "unauthorized" → missing or wrong credentials
# - "connection refused" → registry unreachable
# - "no such host" → wrong registry URL
```

### Solutions

```bash
# 1. Verify image exists
docker manifest inspect registry.example.com/myapp:1.0.0
# or: crane manifest registry.example.com/myapp:1.0.0

# 2. Check/create imagePullSecrets
kubectl get secret -n <namespace> | grep regcred
kubectl create secret docker-registry regcred \
  --docker-server=registry.example.com \
  --docker-username=user \
  --docker-password='p@ssw0rd' \
  --docker-email=user@example.com \
  -n <namespace>

# 3. Verify the secret is referenced in the pod
kubectl get pod <pod-name> -o jsonpath='{.spec.imagePullSecrets}' | jq

# 4. Test pulling from a debug pod
kubectl run pull-test --rm -it --image=registry.example.com/myapp:1.0.0 -- echo "pull succeeded"

# 5. For default service account, patch it
kubectl patch serviceaccount default -n <namespace> \
  -p '{"imagePullSecrets": [{"name": "regcred"}]}'
```

## OOMKilled

The container exceeded its memory limit and was killed by the kernel OOM killer.

### Diagnosis

```bash
# Confirm OOMKilled
kubectl describe pod <pod-name> | grep -i oom
kubectl get pod <pod-name> -o jsonpath='{.status.containerStatuses[0].lastState}' | jq

# Check current memory usage
kubectl top pod <pod-name>

# Check memory limits
kubectl get pod <pod-name> -o jsonpath='{.spec.containers[0].resources}' | jq

# Check node-level memory pressure
kubectl describe node <node-name> | grep -A 5 "Conditions"
```

### Solutions

```bash
# 1. Increase memory limits (if the app genuinely needs more)
# Edit deployment:
kubectl edit deployment <deployment-name>
# Change: resources.limits.memory: "1Gi" → "2Gi"

# 2. For Java: ensure JVM respects container limits
# Add to JAVA_OPTS:
# -XX:MaxRAMPercentage=75.0 -XX:+UseContainerSupport -XX:+ExitOnOutOfMemoryError

# 3. For Go: set GOMEMLIMIT
# env:
#   - name: GOMEMLIMIT
#     value: "800MiB"    # ~80% of container memory limit

# 4. For Python: check for memory leaks with tracemalloc
# import tracemalloc; tracemalloc.start()

# 5. Profile memory usage
kubectl exec -it <pod-name> -- cat /sys/fs/cgroup/memory/memory.usage_in_bytes
kubectl exec -it <pod-name> -- cat /sys/fs/cgroup/memory/memory.limit_in_bytes
# cgroup v2:
kubectl exec -it <pod-name> -- cat /sys/fs/cgroup/memory.current
kubectl exec -it <pod-name> -- cat /sys/fs/cgroup/memory.max
```

## Node NotReady

A node in `NotReady` state means the kubelet is not reporting to the API server.

### Diagnosis

```bash
# Check node status and conditions
kubectl get nodes
kubectl describe node <node-name>

# Check conditions in detail
kubectl get node <node-name> -o json | jq '.status.conditions'
# Look for: MemoryPressure, DiskPressure, PIDPressure, NetworkUnavailable

# SSH into the node and check kubelet
ssh <node-ip>
sudo systemctl status kubelet
sudo journalctl -u kubelet --since "10 minutes ago" -f

# Check disk space
df -h
# Check memory
free -h
# Check running containers
sudo crictl ps
sudo crictl pods
```

### Solutions

```bash
# 1. Restart kubelet
ssh <node-ip>
sudo systemctl restart kubelet

# 2. If disk pressure — clean up
sudo crictl rmi --prune          # remove unused images
sudo journalctl --vacuum-size=500M  # clean old logs
docker system prune -af          # if using Docker

# 3. If memory pressure — identify memory hogs
ps aux --sort=-%mem | head -20

# 4. Drain the node for maintenance
kubectl drain <node-name> --ignore-daemonsets --delete-emptydir-data --grace-period=60
# Fix the issue, then uncordon
kubectl uncordon <node-name>

# 5. If node is permanently dead — remove it
kubectl delete node <node-name>
```

## Service Not Reachable

### Diagnosis

```bash
# 1. Check service exists and has endpoints
kubectl get svc <service-name> -n <namespace>
kubectl get endpoints <service-name> -n <namespace>
# If endpoints list is EMPTY → selector doesn't match any running pods

# 2. Verify selector matches pod labels
kubectl describe svc <service-name> -n <namespace>
kubectl get pods -n <namespace> --show-labels | grep <app-label>

# 3. Check pod readiness (only Ready pods get endpoints)
kubectl get pods -n <namespace> -o wide
# Pods must be Running AND Ready (e.g., 1/1)

# 4. Test connectivity from within the cluster
kubectl run netshoot --rm -it --image=nicolaka/netshoot -- bash
# Inside the debug pod:
curl -v http://<service-name>.<namespace>.svc.cluster.local:<port>
nslookup <service-name>.<namespace>.svc.cluster.local
nc -zv <service-name>.<namespace>.svc.cluster.local <port>

# 5. Check if NetworkPolicy is blocking traffic
kubectl get networkpolicy -n <namespace>
kubectl describe networkpolicy -n <namespace>

# 6. Check kube-proxy
kubectl get pods -n kube-system -l k8s-app=kube-proxy
kubectl logs -n kube-system -l k8s-app=kube-proxy | tail -20
# Check iptables rules on the node:
ssh <node-ip> sudo iptables -t nat -L KUBE-SERVICES | grep <service-name>
```

## DNS Issues

### Diagnosis

```bash
# 1. Test DNS from a pod
kubectl run dns-test --rm -it --image=busybox:1.36 -- nslookup kubernetes.default
kubectl run dns-test --rm -it --image=busybox:1.36 -- nslookup <service-name>.<namespace>.svc.cluster.local

# 2. Check CoreDNS pods
kubectl get pods -n kube-system -l k8s-app=kube-dns
kubectl logs -n kube-system -l k8s-app=kube-dns --tail=50

# 3. Check CoreDNS ConfigMap
kubectl get configmap coredns -n kube-system -o yaml

# 4. Check if DNS service is reachable
kubectl get svc -n kube-system kube-dns
kubectl run dns-test --rm -it --image=busybox:1.36 -- nslookup kubernetes.default 10.96.0.10

# 5. Check pod's DNS config
kubectl exec <pod-name> -- cat /etc/resolv.conf

# 6. Check for DNS rate limiting / resource exhaustion
kubectl top pods -n kube-system -l k8s-app=kube-dns
```

### Common DNS Fixes

```bash
# Restart CoreDNS
kubectl rollout restart deployment coredns -n kube-system

# Scale up CoreDNS if under load
kubectl scale deployment coredns -n kube-system --replicas=3

# Check for ndots issue (too many DNS queries)
# In pod spec, reduce ndots:
# dnsConfig:
#   options:
#     - name: ndots
#       value: "2"
```

## Ingress 502 / 504 Errors

### Diagnosis

```bash
# 502 Bad Gateway — backend is not responding
# 504 Gateway Timeout — backend is too slow

# 1. Check ingress controller logs
kubectl logs -n ingress-nginx -l app.kubernetes.io/name=ingress-nginx --tail=100

# 2. Check ingress resource
kubectl describe ingress <ingress-name> -n <namespace>
# Verify: backend service name, port, and path

# 3. Check backend service and endpoints
kubectl get svc <backend-service> -n <namespace>
kubectl get endpoints <backend-service> -n <namespace>

# 4. Test backend directly (bypass ingress)
kubectl port-forward svc/<backend-service> -n <namespace> 8080:80
curl -v http://localhost:8080/healthz

# 5. Check if pods are ready
kubectl get pods -n <namespace> -l app=<app-label>

# 6. For 504 — increase timeout annotations
# nginx.ingress.kubernetes.io/proxy-read-timeout: "300"
# nginx.ingress.kubernetes.io/proxy-send-timeout: "300"
# nginx.ingress.kubernetes.io/proxy-connect-timeout: "60"
```

### Common Ingress Fixes

```yaml
# Fix 504 timeout
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: myapp
  annotations:
    nginx.ingress.kubernetes.io/proxy-read-timeout: "300"
    nginx.ingress.kubernetes.io/proxy-send-timeout: "300"
    nginx.ingress.kubernetes.io/proxy-body-size: "50m"
    nginx.ingress.kubernetes.io/proxy-buffering: "on"
spec:
  ingressClassName: nginx
  rules:
    - host: myapp.example.com
      http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: myapp
                port:
                  number: 80
```

## PVC Pending

```bash
# Check PVC status and events
kubectl describe pvc <pvc-name> -n <namespace>

# Common causes:
# 1. No matching StorageClass
kubectl get sc
kubectl get pvc <pvc-name> -o jsonpath='{.spec.storageClassName}'

# 2. Provisioner not running
kubectl get pods -A | grep -i provisioner
kubectl get pods -A | grep -i csi

# 3. WaitForFirstConsumer binding mode (normal — binds when pod is scheduled)
kubectl get sc <sc-name> -o jsonpath='{.volumeBindingMode}'

# 4. Quota exceeded
kubectl describe resourcequota -n <namespace>

# 5. For static provisioning — check PV availability
kubectl get pv
kubectl get pv -o jsonpath='{range .items[*]}{.metadata.name}{"\t"}{.status.phase}{"\t"}{.spec.capacity.storage}{"\n"}{end}'
```

## etcd Performance Issues

```bash
# Check etcd health
kubectl exec -it -n kube-system etcd-<node> -- etcdctl \
  --endpoints=https://127.0.0.1:2379 \
  --cacert=/etc/kubernetes/pki/etcd/ca.crt \
  --cert=/etc/kubernetes/pki/etcd/server.crt \
  --key=/etc/kubernetes/pki/etcd/server.key \
  endpoint health

# Check etcd metrics
kubectl exec -it -n kube-system etcd-<node> -- etcdctl \
  --endpoints=https://127.0.0.1:2379 \
  --cacert=/etc/kubernetes/pki/etcd/ca.crt \
  --cert=/etc/kubernetes/pki/etcd/server.crt \
  --key=/etc/kubernetes/pki/etcd/server.key \
  endpoint status --write-out=table

# Check disk latency (etcd needs fast disk — SSD recommended)
# WAL fsync duration should be < 10ms
# Backend commit duration should be < 25ms

# Defragment etcd (if DB size is large)
kubectl exec -it -n kube-system etcd-<node> -- etcdctl \
  --endpoints=https://127.0.0.1:2379 \
  --cacert=/etc/kubernetes/pki/etcd/ca.crt \
  --cert=/etc/kubernetes/pki/etcd/server.crt \
  --key=/etc/kubernetes/pki/etcd/server.key \
  defrag
```

## Advanced Debugging Tools

### kubectl debug (Ephemeral Containers)

```bash
# Debug a running pod (attach ephemeral container)
kubectl debug -it <pod-name> --image=nicolaka/netshoot --target=<container-name>

# Debug a pod by creating a copy with a different image
kubectl debug <pod-name> -it --copy-to=debug-copy --image=ubuntu --share-processes

# Debug a node
kubectl debug node/<node-name> -it --image=ubuntu
# Inside: chroot /host to access node filesystem
```

### Network Debugging with nsenter and tcpdump

```bash
# Find the pod's node and container ID
kubectl get pod <pod-name> -o jsonpath='{.status.hostIP}'
kubectl get pod <pod-name> -o jsonpath='{.status.containerStatuses[0].containerID}'

# SSH to the node, find the PID
ssh <node-ip>
PID=$(sudo crictl inspect <container-id> | jq .info.pid)

# Enter the pod's network namespace
sudo nsenter -t $PID -n -- ip addr
sudo nsenter -t $PID -n -- ss -tlnp
sudo nsenter -t $PID -n -- tcpdump -i eth0 -nn port 8080

# Or use kubectl debug node
kubectl debug node/<node-name> -it --image=nicolaka/netshoot
# Inside: nsenter -t 1 -n -- tcpdump -i any port 8080
```

## Quick Reference Table

| Symptom | First Command | What to Look For |
|---------|---------------|------------------|
| Pod Pending | `kubectl describe pod <name>` | Events: FailedScheduling |
| CrashLoopBackOff | `kubectl logs <name> --previous` | Application error, exit code |
| ImagePullBackOff | `kubectl describe pod <name>` | Image name, registry auth |
| OOMKilled | `kubectl top pod <name>` | Memory usage vs limits |
| Service unreachable | `kubectl get endpoints <svc>` | Empty endpoints = selector mismatch |
| DNS failure | `kubectl logs -n kube-system -l k8s-app=kube-dns` | CoreDNS errors |
| Ingress 502 | `kubectl logs -n ingress-nginx -l app.kubernetes.io/name=ingress-nginx` | Backend connection refused |
| Ingress 504 | Same as above | Upstream timed out |
| Node NotReady | `ssh <node> journalctl -u kubelet` | kubelet errors, disk/memory pressure |
| PVC Pending | `kubectl describe pvc <name>` | StorageClass, provisioner status |
| Slow API server | `kubectl get --raw /healthz` | etcd latency, API server load |
| Pod evicted | `kubectl describe pod <name>` | DiskPressure, MemoryPressure |
