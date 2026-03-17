# etcd 分布式存储

```{mermaid}
flowchart TB
    subgraph Cluster["etcd Cluster (Raft Consensus)"]
        L[Leader] <-->|replicate| F1[Follower 1]
        L <-->|replicate| F2[Follower 2]
    end
    Client[K8s API Server] -->|read/write| L
    Client -.->|read| F1
    Client -.->|read| F2
```

## Overview

etcd is a distributed, reliable key-value store used by Kubernetes to store all cluster data. It uses the **Raft consensus algorithm** to ensure data consistency across nodes.

## etcdctl Commands

```bash
# Set API version
export ETCDCTL_API=3

# Put / Get / Delete
etcdctl put /mykey "myvalue"
etcdctl get /mykey
etcdctl get /mykey --print-value-only
etcdctl del /mykey

# Get with prefix (list all keys under a path)
etcdctl get /registry/ --prefix --keys-only

# Watch for changes
etcdctl watch /mykey
etcdctl watch /myprefix/ --prefix

# List members
etcdctl member list --write-out=table

# Cluster health
etcdctl endpoint health --cluster
etcdctl endpoint status --cluster --write-out=table
```

### Connecting to K8s etcd

```bash
# From control plane node
ETCDCTL_API=3 etcdctl \
  --endpoints=https://127.0.0.1:2379 \
  --cacert=/etc/kubernetes/pki/etcd/ca.crt \
  --cert=/etc/kubernetes/pki/etcd/server.crt \
  --key=/etc/kubernetes/pki/etcd/server.key \
  member list --write-out=table

# List K8s resources stored in etcd
ETCDCTL_API=3 etcdctl \
  --endpoints=https://127.0.0.1:2379 \
  --cacert=/etc/kubernetes/pki/etcd/ca.crt \
  --cert=/etc/kubernetes/pki/etcd/server.crt \
  --key=/etc/kubernetes/pki/etcd/server.key \
  get /registry/ --prefix --keys-only | head -20
```

## Backup and Restore

### Backup

```bash
# Snapshot save
ETCDCTL_API=3 etcdctl snapshot save /backup/etcd-$(date +%Y%m%d-%H%M%S).db \
  --endpoints=https://127.0.0.1:2379 \
  --cacert=/etc/kubernetes/pki/etcd/ca.crt \
  --cert=/etc/kubernetes/pki/etcd/server.crt \
  --key=/etc/kubernetes/pki/etcd/server.key

# Verify snapshot
etcdctl snapshot status /backup/etcd-snapshot.db --write-out=table
```

### Automated Backup Script

```bash
#!/bin/bash
# etcd-backup.sh
BACKUP_DIR="/backup/etcd"
RETENTION_DAYS=7
TIMESTAMP=$(date +%Y%m%d-%H%M%S)

mkdir -p $BACKUP_DIR

ETCDCTL_API=3 etcdctl snapshot save \
  "${BACKUP_DIR}/etcd-${TIMESTAMP}.db" \
  --endpoints=https://127.0.0.1:2379 \
  --cacert=/etc/kubernetes/pki/etcd/ca.crt \
  --cert=/etc/kubernetes/pki/etcd/server.crt \
  --key=/etc/kubernetes/pki/etcd/server.key

# Verify
etcdctl snapshot status "${BACKUP_DIR}/etcd-${TIMESTAMP}.db" --write-out=table

# Cleanup old backups
find $BACKUP_DIR -name "etcd-*.db" -mtime +$RETENTION_DAYS -delete

echo "Backup completed: etcd-${TIMESTAMP}.db"
```

### Restore

```bash
# Stop kube-apiserver and etcd
# On control plane node:
mv /etc/kubernetes/manifests/kube-apiserver.yaml /tmp/
mv /etc/kubernetes/manifests/etcd.yaml /tmp/

# Restore snapshot
ETCDCTL_API=3 etcdctl snapshot restore /backup/etcd-snapshot.db \
  --data-dir=/var/lib/etcd-restored \
  --name=master \
  --initial-cluster=master=https://127.0.0.1:2380 \
  --initial-advertise-peer-urls=https://127.0.0.1:2380

# Update etcd data directory
mv /var/lib/etcd /var/lib/etcd.bak
mv /var/lib/etcd-restored /var/lib/etcd

# Restart etcd and kube-apiserver
mv /tmp/etcd.yaml /etc/kubernetes/manifests/
mv /tmp/kube-apiserver.yaml /etc/kubernetes/manifests/
```

## etcd Cluster Setup (Docker Compose)

```yaml
services:
  etcd1:
    image: quay.io/coreos/etcd:v3.5.12
    command:
      - etcd
      - --name=etcd1
      - --data-dir=/etcd-data
      - --listen-client-urls=http://0.0.0.0:2379
      - --advertise-client-urls=http://etcd1:2379
      - --listen-peer-urls=http://0.0.0.0:2380
      - --initial-advertise-peer-urls=http://etcd1:2380
      - --initial-cluster=etcd1=http://etcd1:2380,etcd2=http://etcd2:2380,etcd3=http://etcd3:2380
      - --initial-cluster-state=new
    volumes:
      - etcd1_data:/etcd-data
    ports:
      - "2379:2379"

  etcd2:
    image: quay.io/coreos/etcd:v3.5.12
    command:
      - etcd
      - --name=etcd2
      - --data-dir=/etcd-data
      - --listen-client-urls=http://0.0.0.0:2379
      - --advertise-client-urls=http://etcd2:2379
      - --listen-peer-urls=http://0.0.0.0:2380
      - --initial-advertise-peer-urls=http://etcd2:2380
      - --initial-cluster=etcd1=http://etcd1:2380,etcd2=http://etcd2:2380,etcd3=http://etcd3:2380
      - --initial-cluster-state=new
    volumes:
      - etcd2_data:/etcd-data

  etcd3:
    image: quay.io/coreos/etcd:v3.5.12
    command:
      - etcd
      - --name=etcd3
      - --data-dir=/etcd-data
      - --listen-client-urls=http://0.0.0.0:2379
      - --advertise-client-urls=http://etcd3:2379
      - --listen-peer-urls=http://0.0.0.0:2380
      - --initial-advertise-peer-urls=http://etcd3:2380
      - --initial-cluster=etcd1=http://etcd1:2380,etcd2=http://etcd2:2380,etcd3=http://etcd3:2380
      - --initial-cluster-state=new
    volumes:
      - etcd3_data:/etcd-data

volumes:
  etcd1_data:
  etcd2_data:
  etcd3_data:
```

## Performance Tuning

```bash
# Compaction (remove old revisions)
etcdctl compact $(etcdctl endpoint status --write-out=json | jq '.[0].Status.header.revision')

# Defragmentation (reclaim disk space after compaction)
etcdctl defrag --cluster

# Set auto-compaction
# In etcd config: --auto-compaction-retention=1h

# Monitor metrics
curl -s http://localhost:2379/metrics | grep etcd_server
```

| Metric | Description | Alert Threshold |
|--------|-------------|-----------------|
| `etcd_server_has_leader` | Has leader | 0 = no leader |
| `etcd_disk_wal_fsync_duration_seconds` | WAL fsync latency | > 10ms |
| `etcd_disk_backend_commit_duration_seconds` | Backend commit latency | > 25ms |
| `etcd_server_proposals_failed_total` | Failed proposals | Increasing |
| `etcd_mvcc_db_total_size_in_bytes` | Database size | > 8GB (default limit) |
