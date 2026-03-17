# Helm 包管理

```{mermaid}
flowchart LR
    Dev[Developer] -->|helm install| Helm[Helm CLI]
    Helm -->|render templates| API[K8s API Server]
    Helm -->|fetch charts| Repo[(Chart Repository)]
    API --> Deploy[Deployment]
    API --> Svc[Service]
    API --> CM[ConfigMap]
    API --> Ing[Ingress]
```

## Helm 3 Architecture

Helm 3 removed Tiller (server-side component). The Helm CLI communicates directly with the K8s API server. Release information is stored as Secrets in the target namespace.

## Essential Commands

```bash
# Repository management
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo add prometheus https://prometheus-community.github.io/helm-charts
helm repo update
helm repo list
helm search repo nginx
helm search hub wordpress    # search Artifact Hub

# Install
helm install my-release bitnami/nginx
helm install my-release bitnami/nginx -f values.yaml
helm install my-release bitnami/nginx --set replicaCount=3
helm install my-release bitnami/nginx -n my-namespace --create-namespace

# Upgrade
helm upgrade my-release bitnami/nginx --set replicaCount=5
helm upgrade my-release bitnami/nginx -f values-prod.yaml
helm upgrade --install my-release bitnami/nginx    # install if not exists

# Rollback
helm rollback my-release 1    # rollback to revision 1
helm history my-release

# List releases
helm list
helm list -A    # all namespaces

# Uninstall
helm uninstall my-release

# Debug / dry-run
helm template my-release bitnami/nginx -f values.yaml    # render locally
helm install my-release bitnami/nginx --dry-run --debug   # server-side dry-run
helm lint ./my-chart                                       # validate chart
```

## Chart Structure

```
my-chart/
├── Chart.yaml          # Chart metadata
├── values.yaml         # Default values
├── charts/             # Dependencies
├── templates/          # K8s manifest templates
│   ├── _helpers.tpl    # Template helpers
│   ├── deployment.yaml
│   ├── service.yaml
│   ├── ingress.yaml
│   ├── configmap.yaml
│   ├── hpa.yaml
│   ├── serviceaccount.yaml
│   ├── NOTES.txt       # Post-install notes
│   └── tests/
│       └── test-connection.yaml
└── .helmignore
```

### Chart.yaml

```yaml
apiVersion: v2
name: my-webapp
description: A web application Helm chart
type: application
version: 0.1.0        # chart version
appVersion: "1.0.0"   # app version
dependencies:
  - name: mysql
    version: "9.x.x"
    repository: https://charts.bitnami.com/bitnami
    condition: mysql.enabled
```

### values.yaml

```yaml
replicaCount: 2

image:
  repository: myapp
  tag: "1.0.0"
  pullPolicy: IfNotPresent

service:
  type: ClusterIP
  port: 80

ingress:
  enabled: true
  className: nginx
  hosts:
    - host: myapp.example.com
      paths:
        - path: /
          pathType: Prefix
  tls:
    - secretName: myapp-tls
      hosts:
        - myapp.example.com

resources:
  requests:
    cpu: 250m
    memory: 256Mi
  limits:
    cpu: 500m
    memory: 512Mi

mysql:
  enabled: true
  auth:
    rootPassword: "rootpass"
    database: mydb
```

### templates/deployment.yaml

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: {{ include "my-webapp.fullname" . }}
  labels:
    {{- include "my-webapp.labels" . | nindent 4 }}
spec:
  replicas: {{ .Values.replicaCount }}
  selector:
    matchLabels:
      {{- include "my-webapp.selectorLabels" . | nindent 6 }}
  template:
    metadata:
      labels:
        {{- include "my-webapp.selectorLabels" . | nindent 8 }}
    spec:
      containers:
        - name: {{ .Chart.Name }}
          image: "{{ .Values.image.repository }}:{{ .Values.image.tag }}"
          imagePullPolicy: {{ .Values.image.pullPolicy }}
          ports:
            - containerPort: 8080
          {{- with .Values.resources }}
          resources:
            {{- toYaml . | nindent 12 }}
          {{- end }}
          livenessProbe:
            httpGet:
              path: /healthz
              port: 8080
          readinessProbe:
            httpGet:
              path: /readyz
              port: 8080
```

### templates/_helpers.tpl

```yaml
{{- define "my-webapp.fullname" -}}
{{- printf "%s-%s" .Release.Name .Chart.Name | trunc 63 | trimSuffix "-" }}
{{- end }}

{{- define "my-webapp.labels" -}}
helm.sh/chart: {{ .Chart.Name }}-{{ .Chart.Version }}
app.kubernetes.io/name: {{ .Chart.Name }}
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{- define "my-webapp.selectorLabels" -}}
app.kubernetes.io/name: {{ .Chart.Name }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}
```

## Hooks

```yaml
apiVersion: batch/v1
kind: Job
metadata:
  name: {{ .Release.Name }}-db-migrate
  annotations:
    "helm.sh/hook": pre-upgrade,pre-install
    "helm.sh/hook-weight": "0"
    "helm.sh/hook-delete-policy": hook-succeeded
spec:
  template:
    spec:
      containers:
        - name: migrate
          image: "{{ .Values.image.repository }}:{{ .Values.image.tag }}"
          command: ["./migrate", "--up"]
      restartPolicy: Never
```

## Create a New Chart

```bash
# Scaffold
helm create my-webapp

# Build dependencies
helm dependency build ./my-webapp

# Package
helm package ./my-webapp

# Push to OCI registry
helm push my-webapp-0.1.0.tgz oci://registry.example.com/charts
```
