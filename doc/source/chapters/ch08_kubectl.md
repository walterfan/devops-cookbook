# kubectl 命令大全

```{mermaid}
flowchart LR
    subgraph "kubectl Workflow"
        A[Configure Context] --> B[Get / Describe]
        B --> C[Create / Apply]
        C --> D[Debug / Logs]
        D --> E[Scale / Update]
        E --> F[Delete / Cleanup]
    end
```

## Configuration

```bash
# View current config
kubectl config view

# List contexts
kubectl config get-contexts

# Switch context
kubectl config use-context my-cluster

# Set default namespace
kubectl config set-context --current --namespace=dev

# Merge kubeconfig files
export KUBECONFIG=~/.kube/config:~/.kube/config-prod
kubectl config view --merge --flatten > ~/.kube/config-merged
```

## CRUD Operations

### Get Resources

```bash
# Basic get
kubectl get pods
kubectl get pods -o wide          # more columns
kubectl get pods -o yaml          # full YAML
kubectl get pods -o json          # full JSON
kubectl get pods -A               # all namespaces
kubectl get pods --show-labels    # show labels

# JSONPath output
kubectl get pods -o jsonpath='{.items[*].metadata.name}'
kubectl get nodes -o jsonpath='{range .items[*]}{.metadata.name}{"\t"}{.status.capacity.cpu}{"\n"}{end}'

# Custom columns
kubectl get pods -o custom-columns='NAME:.metadata.name,STATUS:.status.phase,IP:.status.podIP'

# Sort
kubectl get pods --sort-by=.metadata.creationTimestamp

# Watch
kubectl get pods -w
```

### Describe (detailed info)

```bash
kubectl describe pod myapp-xyz
kubectl describe node worker-1
kubectl describe svc myapp
```

### Create / Apply

```bash
# Imperative create
kubectl create deployment web --image=nginx:1.25 --replicas=3
kubectl create service clusterip web --tcp=80:80
kubectl create configmap myconfig --from-literal=key1=value1
kubectl create secret generic mysecret --from-literal=password=s3cret

# Declarative apply (recommended)
kubectl apply -f deployment.yaml
kubectl apply -f ./manifests/        # apply all files in directory
kubectl apply -f https://example.com/manifest.yaml

# Dry run + generate YAML
kubectl create deployment web --image=nginx --dry-run=client -o yaml > deployment.yaml
```

### Edit / Patch

```bash
# Edit in-place (opens $EDITOR)
kubectl edit deployment web

# Strategic merge patch
kubectl patch deployment web -p '{"spec":{"replicas":5}}'

# JSON patch
kubectl patch deployment web --type='json' \
  -p='[{"op":"replace","path":"/spec/replicas","value":5}]'
```

### Delete

```bash
kubectl delete pod myapp-xyz
kubectl delete -f deployment.yaml
kubectl delete deployment web
kubectl delete pods --all -n dev
kubectl delete pods -l app=myapp

# Force delete (stuck pods)
kubectl delete pod myapp-xyz --grace-period=0 --force
```

## Debugging

```bash
# View logs
kubectl logs myapp-xyz
kubectl logs myapp-xyz -c sidecar    # specific container
kubectl logs myapp-xyz --previous     # previous instance (crash)
kubectl logs -f myapp-xyz             # follow
kubectl logs -l app=myapp --all-containers  # by label

# Execute command
kubectl exec -it myapp-xyz -- /bin/sh
kubectl exec myapp-xyz -- env
kubectl exec myapp-xyz -c sidecar -- cat /etc/config

# Port forward
kubectl port-forward pod/myapp-xyz 8080:80
kubectl port-forward svc/myapp 8080:80

# Copy files
kubectl cp myapp-xyz:/var/log/app.log ./app.log
kubectl cp ./config.yaml myapp-xyz:/etc/config/

# Resource usage
kubectl top nodes
kubectl top pods
kubectl top pods --sort-by=memory

# Debug with ephemeral container
kubectl debug -it myapp-xyz --image=nicolaka/netshoot --target=myapp

# Debug node
kubectl debug node/worker-1 -it --image=ubuntu
```

## Resource Management

```bash
# Labels
kubectl label pod myapp-xyz env=production
kubectl label pod myapp-xyz env-                    # remove
kubectl label pods --all env=staging -n dev

# Annotations
kubectl annotate pod myapp-xyz description="Main app"

# Taints and tolerations
kubectl taint nodes worker-1 dedicated=gpu:NoSchedule
kubectl taint nodes worker-1 dedicated-               # remove

# Cordon / Drain
kubectl cordon worker-1                               # mark unschedulable
kubectl drain worker-1 --ignore-daemonsets --delete-emptydir-data
kubectl uncordon worker-1                              # mark schedulable
```

## Cheat Sheet

| Task | Command |
|------|---------|
| List all resource types | `kubectl api-resources` |
| Explain a resource | `kubectl explain pod.spec.containers` |
| Get events | `kubectl get events --sort-by=.lastTimestamp` |
| Rollout status | `kubectl rollout status deployment/web` |
| Rollout history | `kubectl rollout history deployment/web` |
| Rollback | `kubectl rollout undo deployment/web` |
| Scale | `kubectl scale deployment web --replicas=5` |
| Autoscale | `kubectl autoscale deployment web --min=2 --max=10 --cpu-percent=80` |
| Run one-off pod | `kubectl run debug --rm -it --image=busybox -- /bin/sh` |
| Proxy to API server | `kubectl proxy --port=8001` |

## Productivity Tips

### Aliases (~/.bashrc or ~/.zshrc)

```bash
alias k='kubectl'
alias kgp='kubectl get pods'
alias kgs='kubectl get svc'
alias kgd='kubectl get deployments'
alias kga='kubectl get all'
alias kaf='kubectl apply -f'
alias kdf='kubectl delete -f'
alias kdp='kubectl describe pod'
alias kl='kubectl logs -f'
alias ke='kubectl exec -it'
alias kns='kubectl config set-context --current --namespace'
```

### kubectl Plugins (krew)

```bash
# Install krew
(
  set -x; cd "$(mktemp -d)" &&
  OS="$(uname | tr '[:upper:]' '[:lower:]')" &&
  ARCH="$(uname -m | sed -e 's/x86_64/amd64/' -e 's/aarch64/arm64/')" &&
  KREW="krew-${OS}_${ARCH}" &&
  curl -fsSLO "https://github.com/kubernetes-sigs/krew/releases/latest/download/${KREW}.tar.gz" &&
  tar zxvf "${KREW}.tar.gz" &&
  ./"${KREW}" install krew
)

# Useful plugins
kubectl krew install ctx       # switch contexts
kubectl krew install ns        # switch namespaces
kubectl krew install neat      # clean up YAML output
kubectl krew install tree      # show resource hierarchy
kubectl krew install images    # show container images
kubectl krew install stern     # multi-pod log tailing
```
