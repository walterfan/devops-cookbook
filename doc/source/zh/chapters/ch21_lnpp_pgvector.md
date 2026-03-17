# 21. LNPP + pgvector 向量搜索栈

```{mermaid}
flowchart LR
    Client[Client] --> Nginx[Nginx<br/>API Gateway]
    Nginx --> FastAPI[FastAPI<br/>Python App]
    FastAPI --> PG[(PostgreSQL<br/>+ pgvector)]
    FastAPI --> Embed[Embedding Model<br/>OpenAI / Local]
    subgraph RAG["RAG Pipeline"]
        Doc[Documents] --> Chunk[Chunking]
        Chunk --> Embed
        Embed --> PG
    end
```

## 概述

**pgvector** 是 PostgreSQL 的向量相似度搜索扩展，非常适合 AI/RAG 应用场景。将它与 Nginx + PostgreSQL + Python (FastAPI) 组合在一起，可以构建一套功能强大的语义搜索(Semantic Search)和 AI 驱动的应用技术栈。

## PostgreSQL + pgvector 环境搭建

### Docker Compose

```yaml
services:
  postgres:
    image: pgvector/pgvector:pg16
    environment:
      POSTGRES_DB: vectordb
      POSTGRES_USER: app
      POSTGRES_PASSWORD: ${PG_PASSWORD:-apppass}
    volumes:
      - pg_data:/var/lib/postgresql/data
      - ./init.sql:/docker-entrypoint-initdb.d/init.sql
    ports:
      - "5432:5432"
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U app -d vectordb"]
      interval: 5s
      timeout: 3s
      retries: 5

  app:
    build: ./app
    environment:
      - DATABASE_URL=postgresql+asyncpg://app:${PG_PASSWORD:-apppass}@postgres:5432/vectordb
      - OPENAI_API_KEY=${OPENAI_API_KEY}
    ports:
      - "8000:8000"
    depends_on:
      postgres:
        condition: service_healthy

  nginx:
    image: nginx:1.25-alpine
    ports:
      - "80:80"
    volumes:
      - ./nginx.conf:/etc/nginx/conf.d/default.conf:ro
    depends_on:
      - app

volumes:
  pg_data:
```

### 初始化 pgvector

```sql
-- init.sql
CREATE EXTENSION IF NOT EXISTS vector;

-- Documents table with vector column
CREATE TABLE documents (
    id SERIAL PRIMARY KEY,
    title TEXT NOT NULL,
    content TEXT NOT NULL,
    metadata JSONB DEFAULT '{}',
    embedding vector(1536),    -- OpenAI ada-002 dimension
    created_at TIMESTAMP DEFAULT NOW()
);

-- HNSW index (recommended for most cases)
CREATE INDEX ON documents USING hnsw (embedding vector_cosine_ops)
    WITH (m = 16, ef_construction = 64);

-- IVFFlat index (alternative, faster build)
-- CREATE INDEX ON documents USING ivfflat (embedding vector_cosine_ops)
--     WITH (lists = 100);
```

## 向量搜索查询

```text
-- Cosine similarity search (most common)
SELECT id, title, content,
       1 - (embedding <=> $1::vector) AS similarity
FROM documents
ORDER BY embedding <=> $1::vector
LIMIT 10;

-- L2 distance search
SELECT id, title, content
FROM documents
ORDER BY embedding <-> $1::vector
LIMIT 10;

-- Inner product search
SELECT id, title, content
FROM documents
ORDER BY embedding <#> $1::vector
LIMIT 10;

-- Filtered search
SELECT id, title, content,
       1 - (embedding <=> $1::vector) AS similarity
FROM documents
WHERE metadata->>'category' = 'tech'
  AND created_at > NOW() - INTERVAL '30 days'
ORDER BY embedding <=> $1::vector
LIMIT 10;
```

## 索引对比

| 索引类型 | 构建速度 | 查询速度 | 内存占用 | 适用场景 |
|----------|----------|----------|----------|----------|
| **HNSW** | 较慢 | 快 | 较高 | 生产环境，向量数 < 1000 万 |
| **IVFFlat** | 快 | 中等 | 较低 | 大规模数据集，需要频繁重建索引 |
| 无索引（精确搜索） | 不适用 | 慢 | 低 | 小数据集（< 1 万条） |

## FastAPI + pgvector 应用

```python
# app/main.py
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from sqlalchemy.ext.asyncio import create_async_engine, AsyncSession
from sqlalchemy.orm import sessionmaker
from sqlalchemy import text
import openai
import os

app = FastAPI(title="Vector Search API")

DATABASE_URL = os.getenv("DATABASE_URL")
engine = create_async_engine(DATABASE_URL)
async_session = sessionmaker(engine, class_=AsyncSession, expire_on_commit=False)

client = openai.AsyncOpenAI()

class DocumentCreate(BaseModel):
    title: str
    content: str
    metadata: dict = {}

class SearchQuery(BaseModel):
    query: str
    top_k: int = 5

async def get_embedding(text: str) -> list[float]:
    response = await client.embeddings.create(
        model="text-embedding-ada-002",
        input=text
    )
    return response.data[0].embedding

@app.post("/documents")
async def create_document(doc: DocumentCreate):
    embedding = await get_embedding(doc.content)
    async with async_session() as session:
        await session.execute(
            text("""
                INSERT INTO documents (title, content, metadata, embedding)
                VALUES (:title, :content, :metadata, :embedding)
            """),
            {
                "title": doc.title,
                "content": doc.content,
                "metadata": doc.metadata,
                "embedding": str(embedding),
            }
        )
        await session.commit()
    return {"status": "created"}

@app.post("/search")
async def search(query: SearchQuery):
    embedding = await get_embedding(query.query)
    async with async_session() as session:
        result = await session.execute(
            text("""
                SELECT id, title, content, metadata,
                       1 - (embedding <=> :embedding::vector) AS similarity
                FROM documents
                ORDER BY embedding <=> :embedding::vector
                LIMIT :top_k
            """),
            {"embedding": str(embedding), "top_k": query.top_k}
        )
        rows = result.fetchall()
    return [
        {
            "id": r.id,
            "title": r.title,
            "content": r.content,
            "similarity": round(r.similarity, 4),
        }
        for r in rows
    ]

@app.get("/health")
async def health():
    return {"status": "ok"}
```

### Dockerfile

```dockerfile
FROM python:3.12-slim
WORKDIR /app
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt
COPY . .
RUN useradd -r appuser
USER appuser
EXPOSE 8000
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]
```

### requirements.txt

```text
fastapi>=0.110
uvicorn[standard]>=0.27
sqlalchemy[asyncio]>=2.0
asyncpg>=0.29
openai>=1.12
pgvector>=0.2
```

## K8s 部署

```yaml
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: postgres-vector
spec:
  serviceName: postgres-vector
  replicas: 1
  selector:
    matchLabels:
      app: postgres-vector
  template:
    metadata:
      labels:
        app: postgres-vector
    spec:
      containers:
        - name: postgres
          image: pgvector/pgvector:pg16
          ports:
            - containerPort: 5432
          env:
            - name: POSTGRES_DB
              value: vectordb
            - name: POSTGRES_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: pg-secret
                  key: password
          resources:
            requests:
              cpu: "500m"
              memory: "1Gi"
            limits:
              cpu: "2"
              memory: "4Gi"
          volumeMounts:
            - name: data
              mountPath: /var/lib/postgresql/data
  volumeClaimTemplates:
    - metadata:
        name: data
      spec:
        accessModes: ["ReadWriteOnce"]
        resources:
          requests:
            storage: 50Gi
```

## 性能优化建议

- 生产环境推荐使用 **HNSW** 索引，召回率更高、查询速度更快
- 构建索引时将 `maintenance_work_mem` 调大：`SET maintenance_work_mem = '2GB'`
- 将 `shared_buffers` 设置为可用内存的 25%
- 批量导入数据时使用 `COPY` 命令或批量 `INSERT`，避免逐条插入
- 定期监控索引大小：`SELECT pg_size_pretty(pg_relation_size('documents_embedding_idx'))`
