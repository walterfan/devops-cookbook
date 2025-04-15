# Docker Compose Setup for Nginx and Keycloak with MySQL and HTTPS

Here's the updated configuration using MySQL as the database for Keycloak:

## docker-compose.yml

```yaml
version: '3.8'

services:
  # Nginx reverse proxy with HTTPS
  nginx:
    image: nginx:latest
    container_name: nginx
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx/default.conf:/etc/nginx/conf.d/default.conf
      - ./certs:/etc/nginx/certs
    depends_on:
      - keycloak
    networks:
      - keycloak-network

  # Keycloak identity provider with MySQL
  keycloak:
    image: quay.io/keycloak/keycloak:latest
    container_name: keycloak
    environment:
      - KEYCLOAK_ADMIN=admin
      - KEYCLOAK_ADMIN_PASSWORD=admin
      - KC_PROXY=edge
      - KC_HOSTNAME=keycloak.yourdomain.com
      - KC_HOSTNAME_STRICT_HTTPS=true
      # MySQL configuration
      - KC_DB=mysql
      - KC_DB_URL=jdbc:mysql://keycloak-db:3306/keycloak
      - KC_DB_USERNAME=keycloak
      - KC_DB_PASSWORD=keycloak
      # Health check options
      - KC_HEALTH_ENABLED=true
    command: ["start"]
    depends_on:
      - keycloak-db
    expose:
      - "8080"
    networks:
      - keycloak-network
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/health/ready"]
      interval: 30s
      timeout: 10s
      retries: 5

  # MySQL database for Keycloak
  keycloak-db:
    image: mysql:8.0
    container_name: keycloak-db
    environment:
      - MYSQL_DATABASE=keycloak
      - MYSQL_USER=keycloak
      - MYSQL_PASSWORD=keycloak
      - MYSQL_ROOT_PASSWORD=rootpassword
    volumes:
      - keycloak-db-data:/var/lib/mysql
    networks:
      - keycloak-network
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      timeout: 20s
      retries: 10

volumes:
  keycloak-db-data:

networks:
  keycloak-network:
    driver: bridge
```

## nginx/default.conf

This remains the same as in the previous configuration. Here it is again for completeness:

```nginx
server {
    listen 80;
    server_name keycloak.yourdomain.com;
    return 301 https://$host$request_uri;
}

server {
    listen 443 ssl;
    server_name keycloak.yourdomain.com;

    ssl_certificate /etc/nginx/certs/cert.pem;
    ssl_certificate_key /etc/nginx/certs/key.pem;
    
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_prefer_server_ciphers on;
    ssl_ciphers 'ECDHE-ECDSA-AES128-GCM-SHA256:ECDHE-RSA-AES128-GCM-SHA256:ECDHE-ECDSA-AES256-GCM-SHA384:ECDHE-RSA-AES256-GCM-SHA384:DHE-RSA-AES128-GCM-SHA256:DHE-RSA-AES256-GCM-SHA384';
    ssl_session_timeout 1d;
    ssl_session_cache shared:SSL:50m;
    ssl_stapling on;
    ssl_stapling_verify on;

    # Increase buffer size for large headers
    proxy_buffer_size 128k;
    proxy_buffers 4 256k;
    proxy_busy_buffers_size 256k;

    location / {
        proxy_pass http://keycloak:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # Required for WebSockets
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }

    # Redirect /auth to /
    rewrite ^/auth(.*)$ $1 permanent;
}
```

## Key Changes Made:

1. **Added MySQL Service**:
   - Created a dedicated MySQL 8.0 container
   - Configured persistent storage using a Docker volume
   - Set up proper database credentials

2. **Updated Keycloak Configuration**:
   - Changed from `start-dev` to `start` for production mode
   - Added MySQL connection parameters:
     - `KC_DB=mysql`
     - `KC_DB_URL` pointing to the MySQL container
     - Database credentials
   - Added health checks for both Keycloak and MySQL

3. **Added Health Checks**:
   - For Keycloak to ensure it's ready before accepting connections
   - For MySQL to ensure the database is available before Keycloak starts

## Important Notes:

1. **Initial Startup**:
   - The first startup will take longer as Keycloak initializes its database schema
   - MySQL may take a few moments to become available

2. **Production Considerations**:
   - Change all passwords (admin, database, root database)
   - Consider using MySQL connection pooling parameters
   - Adjust MySQL configuration for better performance
   - Set proper memory limits for both Keycloak and MySQL

3. **Backup**:
   - The `keycloak-db-data` volume contains all your Keycloak data
   - Implement a backup strategy for this volume

4. **Database Upgrade**:
   - If you need to upgrade MySQL later, you'll need to:
     - Dump the database
     - Remove the container and volume
     - Create a new MySQL container with the new version
     - Import the data

Would you like me to make any additional adjustments to this configuration?