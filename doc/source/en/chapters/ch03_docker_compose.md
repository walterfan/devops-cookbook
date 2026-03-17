# 3. Docker Compose Orchestration

```{mermaid}
flowchart TB
    subgraph compose["docker-compose.yml"]
        direction TB
        web[Web App<br/>:8080] --> db[(MySQL<br/>:3306)]
        web --> cache[(Redis<br/>:6379)]
        web --> mq[RabbitMQ<br/>:5672]
        nginx[Nginx<br/>:80/:443] --> web
    end
    subgraph volumes["Volumes"]
        db_data[(db_data)]
        redis_data[(redis_data)]
    end
    db --- db_data
    cache --- redis_data
```

## Compose File Structure

```yaml
# docker-compose.yml
services:
  web:
    build: .
    ports:
      - "8080:8080"
    environment:
      - DB_HOST=db
      - REDIS_HOST=cache
    depends_on:
      db:
        condition: service_healthy
      cache:
        condition: service_started
    networks:
      - backend
    restart: unless-stopped

  db:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: ${DB_ROOT_PASSWORD:-rootpass}
      MYSQL_DATABASE: mydb
      MYSQL_USER: app
      MYSQL_PASSWORD: ${DB_PASSWORD:-apppass}
    volumes:
      - db_data:/var/lib/mysql
      - ./init.sql:/docker-entrypoint-initdb.d/init.sql
    ports:
      - "3306:3306"
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5
    networks:
      - backend

  cache:
    image: redis:7-alpine
    command: redis-server --appendonly yes
    volumes:
      - redis_data:/data
    ports:
      - "6379:6379"
    networks:
      - backend

volumes:
  db_data:
  redis_data:

networks:
  backend:
    driver: bridge
```

## Essential Commands

```bash
# Start all services (detached)
docker compose up -d

# Start with build
docker compose up -d --build

# Stop all services
docker compose down

# Stop and remove volumes
docker compose down -v

# View logs
docker compose logs -f web

# Scale a service
docker compose up -d --scale web=3

# Execute command in a service
docker compose exec db mysql -u root -p

# List running services
docker compose ps

# Restart a single service
docker compose restart web

# Pull latest images
docker compose pull
```

## Environment Variables

### .env file

```bash
# .env (auto-loaded by docker compose)
DB_ROOT_PASSWORD=supersecret
DB_PASSWORD=apppass
APP_PORT=8080
COMPOSE_PROJECT_NAME=myproject
```

### Override files

```bash
# docker-compose.override.yml (auto-loaded for local dev)
services:
  web:
    build:
      context: .
      target: development
    volumes:
      - .:/app  # hot reload
    environment:
      - DEBUG=true
```

```bash
# docker-compose.prod.yml
services:
  web:
    image: registry.example.com/myapp:${VERSION}
    deploy:
      replicas: 3
      resources:
        limits:
          cpus: '1.0'
          memory: 512M

# Use: docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```

## Profiles

```yaml
services:
  web:
    image: myapp:latest
    # always started

  debug-tools:
    image: nicolaka/netshoot
    profiles: ["debug"]
    # only started with: docker compose --profile debug up

  test-runner:
    image: myapp-test:latest
    profiles: ["test"]
```

```bash
docker compose --profile debug up -d
docker compose --profile test run test-runner
```

## LNMP Stack Example

```yaml
# docker-compose-lnmp.yml
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
      - app
    networks:
      - frontend
      - backend

  app:
    build: ./app
    expose:
      - "8000"
    environment:
      - DATABASE_URL=mysql+pymysql://app:apppass@db:3306/mydb
    volumes:
      - static_files:/app/static
    depends_on:
      db:
        condition: service_healthy
    networks:
      - backend

  db:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: rootpass
      MYSQL_DATABASE: mydb
      MYSQL_USER: app
      MYSQL_PASSWORD: apppass
    volumes:
      - mysql_data:/var/lib/mysql
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5
    networks:
      - backend

  phpmyadmin:
    image: phpmyadmin:5
    ports:
      - "8080:80"
    environment:
      PMA_HOST: db
    depends_on:
      - db
    profiles: ["admin"]
    networks:
      - backend

volumes:
  mysql_data:
  static_files:

networks:
  frontend:
  backend:
```

## Microservice Example

```yaml
services:
  gateway:
    image: nginx:1.25-alpine
    ports:
      - "80:80"
    volumes:
      - ./gateway/nginx.conf:/etc/nginx/nginx.conf:ro
    depends_on:
      - user-service
      - order-service

  user-service:
    build: ./services/user
    environment:
      - DB_HOST=user-db
    depends_on:
      user-db:
        condition: service_healthy

  order-service:
    build: ./services/order
    environment:
      - DB_HOST=order-db
      - USER_SERVICE_URL=http://user-service:8080
    depends_on:
      order-db:
        condition: service_healthy

  user-db:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: users
      POSTGRES_PASSWORD: secret
    volumes:
      - user_db_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 5s
      timeout: 3s
      retries: 5

  order-db:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: orders
      POSTGRES_PASSWORD: secret
    volumes:
      - order_db_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 5s
      timeout: 3s
      retries: 5

volumes:
  user_db_data:
  order_db_data:
```

## Tips

- Use `depends_on` with `condition: service_healthy` for reliable startup order
- Use named volumes for persistent data, bind mounts for development
- Use `.env` for secrets, never commit them to git
- Use profiles to separate dev/debug/test tools
- Use `docker compose config` to validate and view the merged config
