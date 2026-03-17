# 12. ConfigMap 与 Secret

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

### 创建方式

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

### ConfigMap YAML 定义

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

### 以环境变量方式使用 ConfigMap

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

可以选择引用单个键，也可以通过 `envFrom` 将 ConfigMap 中的所有键值对一次性注入为环境变量。`prefix` 字段可为所有注入的变量名添加统一前缀，避免命名冲突。

### 以卷挂载方式使用 ConfigMap

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

以卷方式挂载时，ConfigMap 中的每个键会成为挂载目录下的一个文件。使用 `subPath` 可以只挂载单个文件，而不会覆盖目标目录中的其他内容。

## Secret

### Secret 类型

| 类型 | 用途 |
|------|------|
| `Opaque` | 通用键值数据（默认类型） |
| `kubernetes.io/dockerconfigjson` | Docker 镜像仓库凭据 |
| `kubernetes.io/tls` | TLS 证书和私钥 |
| `kubernetes.io/basic-auth` | HTTP Basic 认证凭据 |
| `kubernetes.io/ssh-auth` | SSH 私钥 |

### 创建方式

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

### Secret YAML 定义

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

`data` 字段中的值必须经过 Base64 编码；如果使用 `stringData` 字段，则可以直接填写明文，Kubernetes 会在存储时自动进行编码。需要注意的是，Base64 只是编码而非加密，Secret 本身并不提供强安全保障。

### 使用 Secret

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

Secret 的使用方式与 ConfigMap 类似，可以作为环境变量注入或以卷方式挂载。`imagePullSecrets` 用于指定拉取私有镜像仓库镜像时所需的认证凭据。

## 不可变 ConfigMap 与 Secret

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: app-config-v2
immutable: true    # cannot be updated, must create new version
data:
  DB_HOST: "mysql-svc"
```

将 `immutable` 设为 `true` 后，ConfigMap 或 Secret 将无法被修改，只能创建新版本来替换。这样做有两个好处：一是防止意外修改导致的配置漂移，二是提升性能——kubelet 不再需要持续监听变更。

## 外部密钥管理

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

Sealed Secrets 通过非对称加密将 Secret 加密为 SealedSecret 资源。加密后的 SealedSecret 可以安全地提交到 Git 仓库，只有集群中的 Sealed Secrets 控制器才能解密还原为原始 Secret。

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

External Secrets Operator 可以将外部密钥管理系统（如 HashiCorp Vault、AWS Secrets Manager、Azure Key Vault 等）中的密钥自动同步为 Kubernetes Secret。通过 `refreshInterval` 设置同步周期，确保集群中的 Secret 与外部密钥源保持一致。
