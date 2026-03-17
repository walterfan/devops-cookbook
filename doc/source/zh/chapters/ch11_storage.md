# 11. 存储管理

```{mermaid}
flowchart LR
    Pod[Pod] -->|mount| PVC[PersistentVolumeClaim]
    PVC -->|bind| PV[PersistentVolume]
    PV -->|provision| Storage[(Storage Backend<br/>NFS / EBS / Ceph)]
    SC[StorageClass] -->|dynamic provision| PV
    PVC -->|request| SC
```

## 卷类型（Volume Types）

| 类型 | 生命周期 | 适用场景 |
|------|----------|----------|
| `emptyDir` | 与 Pod 同生命周期 | 临时文件、缓存、容器间数据共享 |
| `hostPath` | 与节点同生命周期 | 节点级日志、Docker socket |
| `configMap` | 与 ConfigMap 同生命周期 | 配置文件 |
| `secret` | 与 Secret 同生命周期 | 凭据、TLS 证书 |
| `persistentVolumeClaim` | 与 PVC 同生命周期 | 数据库、有状态应用 |
| `nfs` | 外部存储 | 跨 Pod 共享存储 |

## emptyDir

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: shared-data
spec:
  containers:
    - name: writer
      image: busybox
      command: ['sh', '-c', 'echo "hello" > /data/message; sleep 3600']
      volumeMounts:
        - name: shared
          mountPath: /data
    - name: reader
      image: busybox
      command: ['sh', '-c', 'cat /data/message; sleep 3600']
      volumeMounts:
        - name: shared
          mountPath: /data
  volumes:
    - name: shared
      emptyDir: {}
      # emptyDir:
      #   medium: Memory    # tmpfs (RAM-backed)
      #   sizeLimit: 256Mi
```

`emptyDir` 在 Pod 被调度到节点时创建，初始为空目录。同一 Pod 内的所有容器可以共享该卷中的数据。Pod 被删除后，`emptyDir` 中的数据也会随之清除。设置 `medium: Memory` 可使用内存作为存储介质（tmpfs），性能更高但占用内存。

## PersistentVolume 与 PersistentVolumeClaim

```yaml
# PersistentVolume (admin creates)
apiVersion: v1
kind: PersistentVolume
metadata:
  name: nfs-pv
spec:
  capacity:
    storage: 10Gi
  accessModes:
    - ReadWriteMany
  persistentVolumeReclaimPolicy: Retain
  nfs:
    server: 192.168.1.100
    path: /exports/data
---
# PersistentVolumeClaim (user requests)
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: myapp-data
spec:
  accessModes:
    - ReadWriteMany
  resources:
    requests:
      storage: 5Gi
---
# Pod using PVC
apiVersion: v1
kind: Pod
metadata:
  name: myapp
spec:
  containers:
    - name: app
      image: myapp:1.0
      volumeMounts:
        - name: data
          mountPath: /app/data
  volumes:
    - name: data
      persistentVolumeClaim:
        claimName: myapp-data
```

Kubernetes 的持久化存储采用两层抽象：

- **PersistentVolume（PV）**：由集群管理员创建，代表一块实际的存储资源
- **PersistentVolumeClaim（PVC）**：由用户创建，声明所需的存储容量和访问模式

PVC 会自动绑定到满足条件的 PV 上。这种设计将存储的供给与消费解耦，用户无需关心底层存储的具体实现。

## 访问模式（Access Modes）

| 模式 | 缩写 | 说明 |
|------|------|------|
| ReadWriteOnce | RWO | 单节点读写 |
| ReadOnlyMany | ROX | 多节点只读 |
| ReadWriteMany | RWX | 多节点读写 |
| ReadWriteOncePod | RWOP | 单 Pod 读写（K8s 1.27+） |

## StorageClass（动态供给）

```yaml
# StorageClass for AWS EBS
apiVersion: storage.k8s.io/v1
kind: StorageClass
metadata:
  name: fast-ssd
provisioner: ebs.csi.aws.com
parameters:
  type: gp3
  iops: "3000"
  throughput: "125"
reclaimPolicy: Delete
volumeBindingMode: WaitForFirstConsumer
allowVolumeExpansion: true
---
# StorageClass for local storage
apiVersion: storage.k8s.io/v1
kind: StorageClass
metadata:
  name: local-storage
provisioner: kubernetes.io/no-provisioner
volumeBindingMode: WaitForFirstConsumer
---
# PVC using StorageClass
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: db-data
spec:
  storageClassName: fast-ssd
  accessModes:
    - ReadWriteOnce
  resources:
    requests:
      storage: 50Gi
```

StorageClass 实现了存储的动态供给（Dynamic Provisioning）。当用户创建 PVC 并指定 `storageClassName` 时，Kubernetes 会自动调用对应的存储供给器（Provisioner）创建 PV，无需管理员手动预创建。

关键参数说明：
- `reclaimPolicy`：PVC 删除后 PV 的回收策略，`Delete` 表示自动删除，`Retain` 表示保留
- `volumeBindingMode`：`WaitForFirstConsumer` 表示延迟绑定，等到 Pod 调度后再创建 PV，有助于保证数据局部性
- `allowVolumeExpansion`：是否允许在线扩容

## NFS 存储

```bash
# NFS server setup (Ubuntu)
sudo apt-get install nfs-kernel-server
sudo mkdir -p /exports/data
echo "/exports/data *(rw,sync,no_subtree_check,no_root_squash)" | sudo tee -a /etc/exports
sudo exportfs -ra
sudo systemctl restart nfs-kernel-server
```

```yaml
# NFS PV
apiVersion: v1
kind: PersistentVolume
metadata:
  name: nfs-pv
spec:
  capacity:
    storage: 100Gi
  accessModes:
    - ReadWriteMany
  nfs:
    server: nfs-server.example.com
    path: /exports/data
```

NFS 是一种常见的共享存储方案，支持 ReadWriteMany 访问模式，允许多个 Pod 同时读写同一存储卷。适合需要多 Pod 共享数据的场景。

## 卷快照（Volume Snapshots）

```yaml
# VolumeSnapshot
apiVersion: snapshot.storage.k8s.io/v1
kind: VolumeSnapshot
metadata:
  name: db-snapshot
spec:
  volumeSnapshotClassName: csi-snapclass
  source:
    persistentVolumeClaimName: db-data
---
# Restore from snapshot
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: db-data-restored
spec:
  storageClassName: fast-ssd
  dataSource:
    name: db-snapshot
    kind: VolumeSnapshot
    apiGroup: snapshot.storage.k8s.io
  accessModes:
    - ReadWriteOnce
  resources:
    requests:
      storage: 50Gi
```

卷快照（VolumeSnapshot）可以对 PVC 的数据创建时间点快照，用于数据备份和恢复。通过在新 PVC 的 `dataSource` 中引用快照，即可从快照恢复数据。此功能需要 CSI 驱动和 VolumeSnapshot CRD 的支持。
