# 21. LNPP + pgvector Vector Search Stack

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

## Overview

**pgvector** is a PostgreSQL extension for vector similarity search, perfect for AI/RAG applications. Combined with Nginx + PostgreSQL + Python (FastAPI), it forms a powerful stack for semantic search and AI-powered applications.

## PostgreSQL + pgvector Setup

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

### Initialize pgvector

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

## Vector Search Queries

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

## Index Comparison

| Index | Build Speed | Query Speed | Memory | Best For |
|-------|-------------|-------------|--------|----------|
| **HNSW** | Slow | Fast | High | Production, < 10M vectors |
| **IVFFlat** | Fast | Medium | Low | Large datasets, frequent rebuilds |
| None (exact) | N/A | Slow | Low | Small datasets (< 10K) |

## FastAPI + pgvector Application

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

## K8s Deployment

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

## Performance Tips

- Use **HNSW** index for production (better recall, faster queries)
- Set `maintenance_work_mem` high during index builds: `SET maintenance_work_mem = '2GB'`
- Use `shared_buffers = 25%` of available RAM
- Batch inserts for bulk loading (use `COPY` or batch `INSERT`)
- Monitor index size: `SELECT pg_size_pretty(pg_relation_size('documents_embedding_idx'))`
