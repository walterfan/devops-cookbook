# 存储管理

```{mermaid}
flowchart LR
    Pod[Pod] -->|mount| PVC[PersistentVolumeClaim]
    PVC -->|bind| PV[PersistentVolume]
    PV -->|provision| Storage[(Storage Backend<br/>NFS / EBS / Ceph)]
    SC[StorageClass] -->|dynamic provision| PV
    PVC -->|request| SC
```

## Volume Types

| Type | Lifecycle | Use Case |
|------|-----------|----------|
| `emptyDir` | Pod lifetime | Temp files, cache, inter-container sharing |
| `hostPath` | Node lifetime | Node-level logs, Docker socket |
| `configMap` | ConfigMap lifetime | Configuration files |
| `secret` | Secret lifetime | Credentials, TLS certs |
| `persistentVolumeClaim` | PVC lifetime | Databases, stateful apps |
| `nfs` | External | Shared storage across pods |

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

## PersistentVolume and PersistentVolumeClaim

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

## Access Modes

| Mode | Abbreviation | Description |
|------|-------------|-------------|
| ReadWriteOnce | RWO | Single node read-write |
| ReadOnlyMany | ROX | Multiple nodes read-only |
| ReadWriteMany | RWX | Multiple nodes read-write |
| ReadWriteOncePod | RWOP | Single pod read-write (K8s 1.27+) |

## StorageClass (Dynamic Provisioning)

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

## NFS Storage

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

## Volume Snapshots

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
