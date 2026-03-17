# 生产环境检查清单

```{mermaid}
mindmap
  root((Production<br/>Readiness))
    Resources
      CPU/Memory Requests & Limits
      HPA / VPA
      PodDisruptionBudget
      ResourceQuota & LimitRange
    Reliability
      Liveness / Readiness / Startup Probes
      Pod Anti-affinity
      Topology Spread Constraints
      Graceful Shutdown
      Rolling Update Strategy
    Security
      RBAC Least Privilege
      Pod Security Standards
      NetworkPolicy Default Deny
      Image Scanning & Signing
      Secrets Management
    Observability
      Prometheus Metrics
      Structured Logging
      Distributed Tracing
      Alerting Rules & SLOs
    Networking
      TLS Everywhere
      Ingress Configuration
      DNS & External DNS
      Rate Limiting & WAF
    Backup & DR
      etcd Snapshots
      PV Snapshots
      Database Backups
      Disaster Recovery Plan
    CI/CD
      GitOps Workflow
      Canary / Blue-Green
      Rollback Procedure
      Staging Parity
```

This chapter provides a comprehensive pre-production checklist organized by category. Use it as a gate before any production deployment. Every item should be verified and signed off by the responsible team.

## Resource Management

Proper resource configuration prevents scheduling failures, noisy-neighbor problems, and unexpected OOM kills.

| # | Item | Status | Priority | Notes |
|---|------|--------|----------|-------|
| 1 | CPU requests set for all containers | ☐ | **Critical** | Ensures fair scheduling |
| 2 | Memory requests set for all containers | ☐ | **Critical** | Prevents eviction |
| 3 | CPU limits set for all containers | ☐ | **High** | Prevents CPU starvation |
| 4 | Memory limits set for all containers | ☐ | **Critical** | Prevents OOMKill of other pods |
| 5 | HPA configured for stateless workloads | ☐ | **High** | Auto-scale on CPU/memory/custom metrics |
| 6 | VPA evaluated for stateful workloads | ☐ | **Medium** | Right-size resource requests |
| 7 | PodDisruptionBudget (PDB) set | ☐ | **High** | Ensures availability during node drain |
| 8 | ResourceQuota set per namespace | ☐ | **High** | Prevents resource exhaustion |
| 9 | LimitRange set per namespace | ☐ | **Medium** | Default limits for pods without explicit limits |
| 10 | No unbounded resource usage | ☐ | **High** | Every container has requests AND limits |

```yaml
# ResourceQuota — cap total resources per namespace
apiVersion: v1
kind: ResourceQuota
metadata:
  name: production-quota
  namespace: production
spec:
  hard:
    requests.cpu: "20"
    requests.memory: 40Gi
    limits.cpu: "40"
    limits.memory: 80Gi
    pods: "100"
    persistentvolumeclaims: "20"
    services.loadbalancers: "5"
---
# LimitRange — default limits for containers without explicit config
apiVersion: v1
kind: LimitRange
metadata:
  name: default-limits
  namespace: production
spec:
  limits:
    - default:
        cpu: "500m"
        memory: "512Mi"
      defaultRequest:
        cpu: "100m"
        memory: "128Mi"
      max:
        cpu: "4"
        memory: "8Gi"
      min:
        cpu: "50m"
        memory: "64Mi"
      type: Container
---
# PodDisruptionBudget — ensure minimum availability
apiVersion: policy/v1
kind: PodDisruptionBudget
metadata:
  name: myapp-pdb
  namespace: production
spec:
  minAvailable: 2    # or use maxUnavailable: 1
  selector:
    matchLabels:
      app: myapp
```

### Verification Commands

```bash
# Check all pods have resource requests/limits
kubectl get pods -n production -o json | \
  jq '.items[] | {name: .metadata.name, containers: [.spec.containers[] | {name: .name, resources: .resources}]}'

# Find pods WITHOUT resource limits
kubectl get pods -n production -o json | \
  jq -r '.items[] | .spec.containers[] | select(.resources.limits == null) | .name'

# Check HPA status
kubectl get hpa -n production

# Check PDB status
kubectl get pdb -n production
```

## Reliability

Reliability ensures your application survives failures, restarts, and infrastructure changes without downtime.

| # | Item | Status | Priority | Notes |
|---|------|--------|----------|-------|
| 1 | Liveness probe configured | ☐ | **Critical** | Restart unhealthy containers |
| 2 | Readiness probe configured | ☐ | **Critical** | Remove from service during issues |
| 3 | Startup probe for slow-starting apps | ☐ | **High** | Prevent premature liveness failures |
| 4 | Graceful shutdown implemented | ☐ | **Critical** | Handle SIGTERM, drain connections |
| 5 | `terminationGracePeriodSeconds` set | ☐ | **High** | Default 30s; increase for long-running requests |
| 6 | `preStop` hook if needed | ☐ | **Medium** | Delay for load balancer deregistration |
| 7 | Pod anti-affinity configured | ☐ | **High** | Spread pods across nodes |
| 8 | Topology spread constraints | ☐ | **High** | Even distribution across zones |
| 9 | Rolling update: `maxSurge=1, maxUnavailable=0` | ☐ | **High** | Zero-downtime deployments |
| 10 | Rollback plan documented and tested | ☐ | **Critical** | `kubectl rollout undo` or `helm rollback` |
| 11 | At least 2 replicas for critical services | ☐ | **Critical** | Single replica = single point of failure |
| 12 | Init containers for dependency checks | ☐ | **Medium** | Wait for DB/cache before starting |

```yaml
# Complete reliability configuration
apiVersion: apps/v1
kind: Deployment
metadata:
  name: myapp
  namespace: production
spec:
  replicas: 3
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 0
  selector:
    matchLabels:
      app: myapp
  template:
    metadata:
      labels:
        app: myapp
        version: "1.5.2"
    spec:
      terminationGracePeriodSeconds: 60
      affinity:
        podAntiAffinity:
          preferredDuringSchedulingIgnoredDuringExecution:
            - weight: 100
              podAffinityTerm:
                labelSelector:
                  matchExpressions:
                    - key: app
                      operator: In
                      values: ["myapp"]
                topologyKey: kubernetes.io/hostname
      topologySpreadConstraints:
        - maxSkew: 1
          topologyKey: topology.kubernetes.io/zone
          whenUnsatisfiable: DoNotSchedule
          labelSelector:
            matchLabels:
              app: myapp
      initContainers:
        - name: wait-for-db
          image: busybox:1.36
          command: ['sh', '-c', 'until nc -z postgres.production.svc.cluster.local 5432; do echo waiting for db; sleep 2; done']
      containers:
        - name: myapp
          image: registry.example.com/myapp:1.5.2
          ports:
            - containerPort: 8080
              name: http
          lifecycle:
            preStop:
              exec:
                command: ["/bin/sh", "-c", "sleep 5"]    # allow LB to deregister
          livenessProbe:
            httpGet:
              path: /healthz
              port: http
            initialDelaySeconds: 10
            periodSeconds: 15
            timeoutSeconds: 3
            failureThreshold: 3
          readinessProbe:
            httpGet:
              path: /readyz
              port: http
            initialDelaySeconds: 5
            periodSeconds: 10
            timeoutSeconds: 3
            failureThreshold: 3
          startupProbe:
            httpGet:
              path: /healthz
              port: http
            failureThreshold: 30
            periodSeconds: 10
          resources:
            requests:
              cpu: "200m"
              memory: "256Mi"
            limits:
              cpu: "1"
              memory: "512Mi"
```

### Verification Commands

```bash
# Check all deployments have probes
kubectl get deployments -n production -o json | \
  jq -r '.items[] | .spec.template.spec.containers[] | select(.livenessProbe == null) | .name' | \
  xargs -I{} echo "WARNING: container '{}' has no liveness probe"

# Check replica counts
kubectl get deployments -n production -o custom-columns=NAME:.metadata.name,REPLICAS:.spec.replicas,READY:.status.readyReplicas

# Check PDB coverage
kubectl get pdb -n production -o custom-columns=NAME:.metadata.name,MIN-AVAILABLE:.spec.minAvailable,ALLOWED-DISRUPTIONS:.status.disruptionsAllowed

# Test rollback
kubectl rollout history deployment/myapp -n production
kubectl rollout undo deployment/myapp -n production --to-revision=<N>
```

## Security

Security must be enforced at every layer — from image build to runtime.

| # | Item | Status | Priority | Notes |
|---|------|--------|----------|-------|
| 1 | RBAC configured (least privilege) | ☐ | **Critical** | No `cluster-admin` for apps |
| 2 | Service accounts scoped per workload | ☐ | **High** | Don't use `default` SA |
| 3 | `automountServiceAccountToken: false` | ☐ | **High** | Unless SA token is needed |
| 4 | Pod Security Standards: `restricted` | ☐ | **Critical** | Namespace-level enforcement |
| 5 | Containers run as non-root | ☐ | **Critical** | `runAsNonRoot: true` |
| 6 | Read-only root filesystem | ☐ | **High** | `readOnlyRootFilesystem: true` |
| 7 | No privilege escalation | ☐ | **Critical** | `allowPrivilegeEscalation: false` |
| 8 | All capabilities dropped | ☐ | **High** | `drop: ["ALL"]` |
| 9 | Seccomp profile: RuntimeDefault | ☐ | **High** | Restrict syscalls |
| 10 | NetworkPolicy: default deny | ☐ | **Critical** | Explicit allow rules only |
| 11 | Images scanned in CI | ☐ | **Critical** | Fail on CRITICAL CVEs |
| 12 | Images signed (Cosign) | ☐ | **Medium** | Verify in admission controller |
| 13 | No `:latest` tag in production | ☐ | **Critical** | Use specific version or SHA |
| 14 | Secrets from external store | ☐ | **High** | Vault / Sealed Secrets / ESO |
| 15 | No secrets in environment variables | ☐ | **Medium** | Use volume mounts instead |
| 16 | Image pull from private registry | ☐ | **High** | `imagePullSecrets` configured |
| 17 | OPA/Gatekeeper policies enforced | ☐ | **Medium** | Require labels, limits, no latest |

```yaml
# Namespace with Pod Security Standards enforced
apiVersion: v1
kind: Namespace
metadata:
  name: production
  labels:
    pod-security.kubernetes.io/enforce: restricted
    pod-security.kubernetes.io/enforce-version: latest
    pod-security.kubernetes.io/audit: restricted
    pod-security.kubernetes.io/warn: restricted
```

### Security Verification Commands

```bash
# Find pods running as root
kubectl get pods -n production -o json | \
  jq -r '.items[] | .spec.containers[] | select(.securityContext.runAsNonRoot != true) | .name'

# Find pods without security context
kubectl get pods -n production -o json | \
  jq -r '.items[] | select(.spec.securityContext == null) | .metadata.name'

# Check NetworkPolicies exist
kubectl get networkpolicy -n production
# Should have at least: default-deny-all, allow-dns, and app-specific rules

# Audit RBAC — find cluster-admin bindings
kubectl get clusterrolebindings -o json | \
  jq -r '.items[] | select(.roleRef.name == "cluster-admin") | {name: .metadata.name, subjects: .subjects}'

# Scan running images
kubectl get pods -n production -o jsonpath='{range .items[*]}{.spec.containers[*].image}{"\n"}{end}' | \
  sort -u | xargs -I{} trivy image --severity CRITICAL {}
```

## Observability

You cannot manage what you cannot measure. Observability covers metrics, logs, traces, and alerts.

| # | Item | Status | Priority | Notes |
|---|------|--------|----------|-------|
| 1 | Prometheus metrics exposed (`/metrics`) | ☐ | **Critical** | Application + infrastructure metrics |
| 2 | ServiceMonitor / PodMonitor configured | ☐ | **Critical** | Prometheus scraping |
| 3 | Grafana dashboards created | ☐ | **High** | RED metrics (Rate, Errors, Duration) |
| 4 | Alerting rules defined | ☐ | **Critical** | Error rate, latency, saturation |
| 5 | Alert routing configured | ☐ | **Critical** | PagerDuty / Slack / email |
| 6 | Structured logging (JSON) | ☐ | **High** | Parseable by log aggregator |
| 7 | Log aggregation configured | ☐ | **High** | Loki / ELK / CloudWatch |
| 8 | Log retention policy set | ☐ | **Medium** | 30-90 days depending on compliance |
| 9 | Distributed tracing enabled | ☐ | **High** | OpenTelemetry / Jaeger / Tempo |
| 10 | SLOs defined and monitored | ☐ | **High** | Availability, latency targets |
| 11 | Error budget tracking | ☐ | **Medium** | SLO-based alerting |
| 12 | On-call runbook documented | ☐ | **Critical** | Step-by-step for each alert |

### Must-Have Alerts

```yaml
# PrometheusRule — critical alerts
apiVersion: monitoring.coreos.com/v1
kind: PrometheusRule
metadata:
  name: myapp-alerts
  namespace: production
spec:
  groups:
    - name: myapp.rules
      rules:
        - alert: HighErrorRate
          expr: |
            sum(rate(http_requests_total{namespace="production",app="myapp",status=~"5.."}[5m]))
            /
            sum(rate(http_requests_total{namespace="production",app="myapp"}[5m]))
            > 0.05
          for: 5m
          labels:
            severity: critical
          annotations:
            summary: "High 5xx error rate (> 5%) for myapp"
            runbook_url: "https://wiki.example.com/runbooks/high-error-rate"

        - alert: HighLatency
          expr: |
            histogram_quantile(0.99, sum(rate(http_request_duration_seconds_bucket{namespace="production",app="myapp"}[5m])) by (le))
            > 1.0
          for: 5m
          labels:
            severity: warning
          annotations:
            summary: "P99 latency > 1s for myapp"

        - alert: PodCrashLooping
          expr: |
            increase(kube_pod_container_status_restarts_total{namespace="production"}[1h]) > 3
          for: 5m
          labels:
            severity: critical
          annotations:
            summary: "Pod {{ $labels.pod }} is crash looping"

        - alert: PodOOMKilled
          expr: |
            kube_pod_container_status_last_terminated_reason{namespace="production",reason="OOMKilled"} == 1
          for: 0m
          labels:
            severity: critical
          annotations:
            summary: "Pod {{ $labels.pod }} was OOMKilled"

        - alert: PVCAlmostFull
          expr: |
            kubelet_volume_stats_used_bytes{namespace="production"}
            /
            kubelet_volume_stats_capacity_bytes{namespace="production"}
            > 0.85
          for: 10m
          labels:
            severity: warning
          annotations:
            summary: "PVC {{ $labels.persistentvolumeclaim }} is > 85% full"

        - alert: CertificateExpiringSoon
          expr: |
            (certmanager_certificate_expiration_timestamp_seconds - time()) / 86400 < 14
          for: 1h
          labels:
            severity: warning
          annotations:
            summary: "Certificate {{ $labels.name }} expires in < 14 days"
```

## Networking

| # | Item | Status | Priority | Notes |
|---|------|--------|----------|-------|
| 1 | TLS termination at Ingress | ☐ | **Critical** | cert-manager + Let's Encrypt |
| 2 | HTTP → HTTPS redirect | ☐ | **Critical** | `ssl-redirect` annotation |
| 3 | TLS certificates auto-renewed | ☐ | **Critical** | cert-manager ClusterIssuer |
| 4 | Rate limiting configured | ☐ | **High** | Prevent abuse / DDoS |
| 5 | CORS configured (if API) | ☐ | **Medium** | Restrict allowed origins |
| 6 | Security headers set | ☐ | **High** | X-Frame-Options, CSP, HSTS |
| 7 | DNS records configured | ☐ | **Critical** | A/CNAME for all services |
| 8 | External DNS automated | ☐ | **Medium** | Auto-create DNS records |
| 9 | Health check endpoints excluded from auth | ☐ | **High** | `/healthz`, `/readyz` public |
| 10 | gRPC/WebSocket support configured | ☐ | **Medium** | If applicable |

```yaml
# Production Ingress with security headers and TLS
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: myapp
  namespace: production
  annotations:
    cert-manager.io/cluster-issuer: letsencrypt-prod
    nginx.ingress.kubernetes.io/ssl-redirect: "true"
    nginx.ingress.kubernetes.io/force-ssl-redirect: "true"
    nginx.ingress.kubernetes.io/proxy-body-size: "10m"
    nginx.ingress.kubernetes.io/rate-limit: "100"
    nginx.ingress.kubernetes.io/rate-limit-window: "1m"
    nginx.ingress.kubernetes.io/configuration-snippet: |
      more_set_headers "X-Frame-Options: DENY";
      more_set_headers "X-Content-Type-Options: nosniff";
      more_set_headers "X-XSS-Protection: 1; mode=block";
      more_set_headers "Referrer-Policy: strict-origin-when-cross-origin";
      more_set_headers "Strict-Transport-Security: max-age=31536000; includeSubDomains";
spec:
  ingressClassName: nginx
  tls:
    - hosts:
        - myapp.example.com
      secretName: myapp-tls
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

## Backup & Disaster Recovery

| # | Item | Status | Priority | Notes |
|---|------|--------|----------|-------|
| 1 | etcd backup automated (daily) | ☐ | **Critical** | Tested restore procedure |
| 2 | etcd backup stored off-cluster | ☐ | **Critical** | S3 / GCS / separate storage |
| 3 | PV snapshots configured | ☐ | **High** | VolumeSnapshot for databases |
| 4 | Database backup automated | ☐ | **Critical** | pg_dump / mysqldump + CronJob |
| 5 | Backup retention policy defined | ☐ | **High** | 7 daily, 4 weekly, 12 monthly |
| 6 | Restore procedure documented | ☐ | **Critical** | Step-by-step runbook |
| 7 | Restore tested quarterly | ☐ | **Critical** | Regular DR drills |
| 8 | Multi-region / multi-cluster DR plan | ☐ | **Medium** | For critical services |
| 9 | Velero or similar backup tool | ☐ | **High** | Full cluster backup |

```bash
# etcd backup CronJob
cat <<'EOF' | kubectl apply -f -
apiVersion: batch/v1
kind: CronJob
metadata:
  name: etcd-backup
  namespace: kube-system
spec:
  schedule: "0 2 * * *"    # daily at 2 AM
  jobTemplate:
    spec:
      template:
        spec:
          hostNetwork: true
          containers:
            - name: etcd-backup
              image: bitnami/etcd:3.5
              command:
                - /bin/sh
                - -c
                - |
                  etcdctl snapshot save /backup/etcd-$(date +%Y%m%d-%H%M%S).db \
                    --endpoints=https://127.0.0.1:2379 \
                    --cacert=/etc/kubernetes/pki/etcd/ca.crt \
                    --cert=/etc/kubernetes/pki/etcd/server.crt \
                    --key=/etc/kubernetes/pki/etcd/server.key
                  # Upload to S3
                  aws s3 cp /backup/ s3://my-etcd-backups/ --recursive
                  # Clean up local backups older than 7 days
                  find /backup -mtime +7 -delete
              volumeMounts:
                - name: etcd-certs
                  mountPath: /etc/kubernetes/pki/etcd
                  readOnly: true
                - name: backup
                  mountPath: /backup
          volumes:
            - name: etcd-certs
              hostPath:
                path: /etc/kubernetes/pki/etcd
            - name: backup
              hostPath:
                path: /var/lib/etcd-backups
          restartPolicy: OnFailure
          nodeSelector:
            node-role.kubernetes.io/control-plane: ""
          tolerations:
            - key: node-role.kubernetes.io/control-plane
              effect: NoSchedule
EOF
```

## CI/CD

| # | Item | Status | Priority | Notes |
|---|------|--------|----------|-------|
| 1 | CI pipeline runs unit + integration tests | ☐ | **Critical** | Every PR |
| 2 | Image built and scanned in CI | ☐ | **Critical** | Fail on critical CVEs |
| 3 | GitOps workflow configured | ☐ | **High** | ArgoCD / Flux |
| 4 | Staging environment mirrors production | ☐ | **High** | Same configs, smaller scale |
| 5 | Canary or blue-green deployment | ☐ | **Medium** | Progressive rollout |
| 6 | Rollback procedure documented | ☐ | **Critical** | One-command rollback |
| 7 | Deployment notifications | ☐ | **High** | Slack / Teams on deploy |
| 8 | Feature flags for risky changes | ☐ | **Medium** | Decouple deploy from release |
| 9 | Database migration strategy | ☐ | **High** | Backward-compatible migrations |
| 10 | Smoke tests after deployment | ☐ | **High** | Automated health verification |

## Pre-Deployment Verification Script

Run this script before every production deployment:

```bash
#!/bin/bash
# pre-deploy-check.sh — run before production deployment
set -euo pipefail

NAMESPACE="production"
RELEASE="myapp"
IMAGE_TAG="${1:?Usage: $0 <image-tag>}"
REGISTRY="registry.example.com"

echo "═══════════════════════════════════════════"
echo "  Pre-Deployment Checklist"
echo "═══════════════════════════════════════════"

# 1. Verify image exists
echo -n "✓ Image exists... "
docker manifest inspect ${REGISTRY}/${RELEASE}:${IMAGE_TAG} > /dev/null 2>&1 && echo "OK" || { echo "FAIL"; exit 1; }

# 2. Verify image was scanned
echo -n "✓ Image scan passed... "
trivy image --exit-code 1 --severity CRITICAL --quiet ${REGISTRY}/${RELEASE}:${IMAGE_TAG} && echo "OK" || { echo "FAIL — critical CVEs found"; exit 1; }

# 3. Dry-run the deployment
echo -n "✓ Helm dry-run... "
helm upgrade --install ${RELEASE} ./chart \
  -f chart/values-production.yaml \
  --set image.tag=${IMAGE_TAG} \
  --namespace ${NAMESPACE} \
  --dry-run > /dev/null 2>&1 && echo "OK" || { echo "FAIL"; exit 1; }

# 4. Check resource quotas
echo -n "✓ Resource quota available... "
kubectl describe resourcequota -n ${NAMESPACE} 2>/dev/null | grep -q "used" && echo "OK" || echo "WARN — no quota set"

# 5. Check PDB exists
echo -n "✓ PDB configured... "
kubectl get pdb -n ${NAMESPACE} -l app=${RELEASE} 2>/dev/null | grep -q ${RELEASE} && echo "OK" || echo "WARN — no PDB"

# 6. Check current deployment health
echo -n "✓ Current deployment healthy... "
kubectl rollout status deployment/${RELEASE} -n ${NAMESPACE} --timeout=10s > /dev/null 2>&1 && echo "OK" || echo "WARN — current deployment unhealthy"

echo ""
echo "═══════════════════════════════════════════"
echo "  All checks passed. Ready to deploy."
echo "═══════════════════════════════════════════"
```

## Post-Deployment Verification

```bash
#!/bin/bash
# post-deploy-verify.sh — run after production deployment
set -euo pipefail

NAMESPACE="production"
RELEASE="myapp"
URL="https://myapp.example.com"

echo "Waiting for rollout to complete..."
kubectl rollout status deployment/${RELEASE} -n ${NAMESPACE} --timeout=300s

echo "Running health checks..."
# Health endpoint
curl -sf ${URL}/healthz || { echo "FAIL: healthz"; exit 1; }
echo "✓ /healthz OK"

# Readiness endpoint
curl -sf ${URL}/readyz || { echo "FAIL: readyz"; exit 1; }
echo "✓ /readyz OK"

# Smoke test — basic API call
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" ${URL}/api/v1/status)
if [ "$HTTP_CODE" -eq 200 ]; then
  echo "✓ API returns 200"
else
  echo "FAIL: API returned ${HTTP_CODE}"
  echo "Rolling back..."
  kubectl rollout undo deployment/${RELEASE} -n ${NAMESPACE}
  exit 1
fi

# Check no pods are crash-looping
RESTARTS=$(kubectl get pods -n ${NAMESPACE} -l app=${RELEASE} -o jsonpath='{.items[*].status.containerStatuses[*].restartCount}' | tr ' ' '\n' | sort -rn | head -1)
if [ "${RESTARTS:-0}" -gt 2 ]; then
  echo "WARN: Pod restarts detected (${RESTARTS})"
fi

echo ""
echo "✅ Deployment verified successfully"
```

## Summary: The Golden Rules

1. **Every container** has resource requests AND limits
2. **Every deployment** has liveness, readiness, and startup probes
3. **Every namespace** has NetworkPolicy default-deny
4. **Every image** is scanned and uses a specific tag (never `:latest`)
5. **Every secret** comes from an external store (Vault / Sealed Secrets)
6. **Every service** has at least 2 replicas with PDB
7. **Every deployment** has a documented rollback procedure
8. **Every alert** has a runbook
9. **Every backup** has a tested restore procedure
10. **Every change** goes through Git (GitOps)
