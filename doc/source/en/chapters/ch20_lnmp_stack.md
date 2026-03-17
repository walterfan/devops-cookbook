# 20. LNMP Stack

```{mermaid}
flowchart LR
    Client[Client] --> Nginx[Nginx<br/>:80/:443]
    Nginx -->|proxy_pass| App[App Server<br/>PHP-FPM / Gunicorn]
    App --> MySQL[(MySQL<br/>:3306)]
    App --> Redis[(Redis<br/>:6379)]
    Nginx -->|static files| Static[/var/www/static]
```

## Architecture Overview

**LNMP** = Linux + Nginx + MySQL + PHP/Python. A classic web stack for serving dynamic web applications.

## Nginx Configuration

### Reverse Proxy

```nginx
# /etc/nginx/conf.d/myapp.conf
upstream app_backend {
    server app:8000 weight=3;
    server app2:8000 weight=1;
    keepalive 32;
}

server {
    listen 80;
    server_name myapp.example.com;
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name myapp.example.com;

    ssl_certificate     /etc/nginx/ssl/cert.pem;
    ssl_certificate_key /etc/nginx/ssl/key.pem;
    ssl_protocols       TLSv1.2 TLSv1.3;
    ssl_ciphers         HIGH:!aNULL:!MD5;

    # Static files
    location /static/ {
        alias /var/www/static/;
        expires 30d;
        add_header Cache-Control "public, immutable";
    }

    # Proxy to app
    location / {
        proxy_pass http://app_backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_connect_timeout 10s;
        proxy_read_timeout 30s;
    }

    # Health check
    location /nginx-health {
        access_log off;
        return 200 "OK";
    }
}
```

### Load Balancing

```nginx
upstream backend {
    # Round-robin (default)
    server app1:8000;
    server app2:8000;

    # Weighted
    # server app1:8000 weight=3;
    # server app2:8000 weight=1;

    # Least connections
    # least_conn;

    # IP hash (sticky sessions)
    # ip_hash;
}
```

## MySQL Setup

### Docker Compose

```yaml
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
      MYSQL_DATABASE: mydb
      MYSQL_USER: app
      MYSQL_PASSWORD: ${MYSQL_PASSWORD}
    volumes:
      - mysql_data:/var/lib/mysql
      - ./mysql/conf.d:/etc/mysql/conf.d
      - ./mysql/init:/docker-entrypoint-initdb.d
    ports:
      - "3306:3306"
    command: >
      --character-set-server=utf8mb4
      --collation-server=utf8mb4_unicode_ci
      --innodb-buffer-pool-size=1G
      --max-connections=200
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5

volumes:
  mysql_data:
```

### Backup

```bash
# Full backup
mysqldump -u root -p --all-databases --single-transaction > backup.sql

# Restore
mysql -u root -p < backup.sql

# Automated backup with cron
0 2 * * * /usr/bin/mysqldump -u root -p'pass' mydb | gzip > /backup/mydb-$(date +\%Y\%m\%d).sql.gz
```

## Full LNMP Docker Compose

```yaml
services:
  nginx:
    image: nginx:1.25-alpine
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx/conf.d:/etc/nginx/conf.d:ro
      - ./nginx/ssl:/etc/nginx/ssl:ro
      - static_files:/var/www/static:ro
    depends_on:
      app:
        condition: service_healthy
    restart: unless-stopped

  app:
    build:
      context: ./app
      dockerfile: Dockerfile
    environment:
      - DATABASE_URL=mysql+pymysql://app:${MYSQL_PASSWORD}@mysql:3306/mydb
      - REDIS_URL=redis://redis:6379/0
    volumes:
      - static_files:/app/static
    depends_on:
      mysql:
        condition: service_healthy
      redis:
        condition: service_started
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8000/health"]
      interval: 10s
      timeout: 5s
      retries: 3
    restart: unless-stopped

  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
      MYSQL_DATABASE: mydb
      MYSQL_USER: app
      MYSQL_PASSWORD: ${MYSQL_PASSWORD}
    volumes:
      - mysql_data:/var/lib/mysql
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5
    restart: unless-stopped

  redis:
    image: redis:7-alpine
    command: redis-server --appendonly yes --maxmemory 256mb --maxmemory-policy allkeys-lru
    volumes:
      - redis_data:/data
    restart: unless-stopped

  phpmyadmin:
    image: phpmyadmin:5
    ports:
      - "8080:80"
    environment:
      PMA_HOST: mysql
    depends_on:
      - mysql
    profiles: ["admin"]

volumes:
  mysql_data:
  redis_data:
  static_files:
```

## K8s Deployment

```yaml
# MySQL StatefulSet
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: mysql
spec:
  serviceName: mysql
  replicas: 1
  selector:
    matchLabels:
      app: mysql
  template:
    metadata:
      labels:
        app: mysql
    spec:
      containers:
        - name: mysql
          image: mysql:8.0
          ports:
            - containerPort: 3306
          env:
            - name: MYSQL_ROOT_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: mysql-secret
                  key: root-password
          volumeMounts:
            - name: data
              mountPath: /var/lib/mysql
          resources:
            requests:
              cpu: "500m"
              memory: "1Gi"
            limits:
              cpu: "1"
              memory: "2Gi"
  volumeClaimTemplates:
    - metadata:
        name: data
      spec:
        accessModes: ["ReadWriteOnce"]
        resources:
          requests:
            storage: 20Gi
---
apiVersion: v1
kind: Service
metadata:
  name: mysql
spec:
  clusterIP: None
  selector:
    app: mysql
  ports:
    - port: 3306
```

## Performance Tuning

### Nginx

```nginx
worker_processes auto;
worker_rlimit_nofile 65535;

events {
    worker_connections 4096;
    multi_accept on;
    use epoll;
}

http {
    sendfile on;
    tcp_nopush on;
    tcp_nodelay on;
    keepalive_timeout 65;
    gzip on;
    gzip_types text/plain text/css application/json application/javascript;
    gzip_min_length 1000;

    # Buffer tuning
    proxy_buffer_size 128k;
    proxy_buffers 4 256k;
    proxy_busy_buffers_size 256k;
}
```

### MySQL

```ini
# /etc/mysql/conf.d/tuning.cnf
[mysqld]
innodb_buffer_pool_size = 1G
innodb_log_file_size = 256M
innodb_flush_log_at_trx_commit = 2
max_connections = 200
query_cache_type = 0
slow_query_log = 1
slow_query_log_file = /var/log/mysql/slow.log
long_query_time = 1
```
