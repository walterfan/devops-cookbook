# ConfigMap 与 Secret

```{mermaid}
flowchart LR
    CM[ConfigMap] -->|env vars| Pod[Pod]
    CM -->|volume mount| Pod
    Sec[Secret] -->|env vars| Pod
    Sec -->|volume mount| Pod
    ESO[External Secrets<br/>Operator] -->|sync| Sec
    Vault[(HashiCorp Vault)] --> ESO
```

## ConfigMap

### Creation

```bash
# From literal values
kubectl create configmap app-config \
  --from-literal=DB_HOST=mysql-svc \
  --from-literal=DB_PORT=3306 \
  --from-literal=LOG_LEVEL=info

# From file
kubectl create configmap nginx-config --from-file=nginx.conf

# From directory
kubectl create configmap configs --from-file=./config-dir/

# From env file
kubectl create configmap app-env --from-env-file=.env
```

### ConfigMap YAML

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: app-config
data:
  DB_HOST: "mysql-svc"
  DB_PORT: "3306"
  LOG_LEVEL: "info"
  # Multi-line config file
  app.properties: |
    server.port=8080
    spring.datasource.url=jdbc:mysql://mysql-svc:3306/mydb
    logging.level.root=INFO
```

### Using ConfigMap as Environment Variables

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: myapp
spec:
  containers:
    - name: app
      image: myapp:1.0
      # Individual keys
      env:
        - name: DATABASE_HOST
          valueFrom:
            configMapKeyRef:
              name: app-config
              key: DB_HOST
      # All keys as env vars
      envFrom:
        - configMapRef:
            name: app-config
          prefix: APP_    # optional prefix
```

### Using ConfigMap as Volume

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: myapp
spec:
  containers:
    - name: app
      image: myapp:1.0
      volumeMounts:
        - name: config
          mountPath: /etc/app
          readOnly: true
        - name: nginx-config
          mountPath: /etc/nginx/nginx.conf
          subPath: nginx.conf    # mount single file
  volumes:
    - name: config
      configMap:
        name: app-config
    - name: nginx-config
      configMap:
        name: nginx-config
```

## Secret

### Secret Types

| Type | Usage |
|------|-------|
| `Opaque` | Arbitrary key-value data (default) |
| `kubernetes.io/dockerconfigjson` | Docker registry credentials |
| `kubernetes.io/tls` | TLS certificate and key |
| `kubernetes.io/basic-auth` | Basic authentication |
| `kubernetes.io/ssh-auth` | SSH private key |

### Creation

```bash
# Opaque secret
kubectl create secret generic db-secret \
  --from-literal=username=admin \
  --from-literal=password='s3cret!'

# Docker registry secret
kubectl create secret docker-registry regcred \
  --docker-server=registry.example.com \
  --docker-username=user \
  --docker-password=pass

# TLS secret
kubectl create secret tls tls-secret \
  --cert=tls.crt \
  --key=tls.key

# From file
kubectl create secret generic ssh-key --from-file=ssh-privatekey=~/.ssh/id_rsa
```

### Secret YAML

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: db-secret
type: Opaque
data:
  # base64 encoded: echo -n 'admin' | base64
  username: YWRtaW4=
  password: czNjcmV0IQ==
---
# Using stringData (plain text, auto-encoded)
apiVersion: v1
kind: Secret
metadata:
  name: db-secret
type: Opaque
stringData:
  username: admin
  password: "s3cret!"
```

### Using Secrets

```yaml
spec:
  containers:
    - name: app
      image: myapp:1.0
      env:
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: db-secret
              key: password
      volumeMounts:
        - name: tls
          mountPath: /etc/tls
          readOnly: true
  volumes:
    - name: tls
      secret:
        secretName: tls-secret
  imagePullSecrets:
    - name: regcred
```

## Immutable ConfigMaps and Secrets

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: app-config-v2
immutable: true    # cannot be updated, must create new version
data:
  DB_HOST: "mysql-svc"
```

Benefits: protects against accidental updates, improves performance (kubelet doesn't watch for changes).

## External Secrets Management

### Sealed Secrets

```bash
# Install sealed-secrets controller
helm repo add sealed-secrets https://bitnami-labs.github.io/sealed-secrets
helm install sealed-secrets sealed-secrets/sealed-secrets -n kube-system

# Install kubeseal CLI
brew install kubeseal

# Seal a secret
kubectl create secret generic db-secret \
  --from-literal=password=s3cret --dry-run=client -o yaml | \
  kubeseal --format yaml > sealed-secret.yaml

# Apply sealed secret (safe to commit to git)
kubectl apply -f sealed-secret.yaml
```

### External Secrets Operator

```bash
# Install
helm repo add external-secrets https://charts.external-secrets.io
helm install external-secrets external-secrets/external-secrets -n external-secrets --create-namespace
```

```yaml
# SecretStore (connects to Vault)
apiVersion: external-secrets.io/v1beta1
kind: SecretStore
metadata:
  name: vault-store
spec:
  provider:
    vault:
      server: "https://vault.example.com"
      path: "secret"
      auth:
        kubernetes:
          mountPath: "kubernetes"
          role: "myapp"
---
# ExternalSecret (syncs from Vault to K8s Secret)
apiVersion: external-secrets.io/v1beta1
kind: ExternalSecret
metadata:
  name: db-secret
spec:
  refreshInterval: 1h
  secretStoreRef:
    name: vault-store
    kind: SecretStore
  target:
    name: db-secret
  data:
    - secretKey: password
      remoteRef:
        key: myapp/database
        property: password
```
