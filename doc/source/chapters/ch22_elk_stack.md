# ELK/ELKK 技术栈

```{mermaid}
flowchart LR
    App[Applications] --> FB[Filebeat]
    FB --> Kafka[(Kafka<br/>Buffer)]
    Kafka --> LS[Logstash<br/>Transform]
    LS --> ES[(Elasticsearch<br/>Store & Index)]
    ES --> Kibana[Kibana<br/>Visualize]
    MB[Metricbeat] --> ES
```

## Architecture

| Component | Role | Port |
|-----------|------|------|
| **Elasticsearch** | Distributed search and analytics engine | 9200, 9300 |
| **Logstash** | Data processing pipeline (input → filter → output) | 5044 |
| **Kibana** | Visualization and dashboard | 5601 |
| **Kafka** | Message buffer (ELKK pattern) | 9092 |
| **Filebeat** | Lightweight log shipper | — |
| **Metricbeat** | Lightweight metrics shipper | — |

## Docker Compose (Full ELK Stack)

```yaml
services:
  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.12.0
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=false
      - "ES_JAVA_OPTS=-Xms1g -Xmx1g"
    volumes:
      - es_data:/usr/share/elasticsearch/data
    ports:
      - "9200:9200"
    healthcheck:
      test: ["CMD-SHELL", "curl -f http://localhost:9200/_cluster/health || exit 1"]
      interval: 10s
      timeout: 5s
      retries: 10

  logstash:
    image: docker.elastic.co/logstash/logstash:8.12.0
    volumes:
      - ./logstash/pipeline:/usr/share/logstash/pipeline:ro
      - ./logstash/config/logstash.yml:/usr/share/logstash/config/logstash.yml:ro
    ports:
      - "5044:5044"    # Beats input
      - "5000:5000"    # TCP input
    depends_on:
      elasticsearch:
        condition: service_healthy

  kibana:
    image: docker.elastic.co/kibana/kibana:8.12.0
    environment:
      - ELASTICSEARCH_HOSTS=http://elasticsearch:9200
    ports:
      - "5601:5601"
    depends_on:
      elasticsearch:
        condition: service_healthy

  filebeat:
    image: docker.elastic.co/beats/filebeat:8.12.0
    user: root
    volumes:
      - ./filebeat/filebeat.yml:/usr/share/filebeat/filebeat.yml:ro
      - /var/lib/docker/containers:/var/lib/docker/containers:ro
      - /var/run/docker.sock:/var/run/docker.sock:ro
    depends_on:
      - logstash

volumes:
  es_data:
```

## Logstash Pipeline

```ruby
# logstash/pipeline/main.conf
input {
  beats {
    port => 5044
  }
  tcp {
    port => 5000
    codec => json
  }
}

filter {
  # Parse JSON logs
  if [message] =~ /^\{/ {
    json {
      source => "message"
    }
  }

  # Parse Apache/Nginx access logs
  if [fileset][name] == "access" {
    grok {
      match => { "message" => "%{COMBINEDAPACHELOG}" }
    }
    date {
      match => [ "timestamp", "dd/MMM/yyyy:HH:mm:ss Z" ]
    }
    geoip {
      source => "clientip"
    }
  }

  # Add metadata
  mutate {
    add_field => { "environment" => "production" }
    remove_field => ["agent", "ecs", "host"]
  }

  # Parse timestamp
  date {
    match => [ "timestamp", "ISO8601", "yyyy-MM-dd HH:mm:ss" ]
    target => "@timestamp"
  }
}

output {
  elasticsearch {
    hosts => ["http://elasticsearch:9200"]
    index => "logs-%{+YYYY.MM.dd}"
  }
  # Debug output
  # stdout { codec => rubydebug }
}
```

## Filebeat Configuration

```yaml
# filebeat/filebeat.yml
filebeat.inputs:
  - type: container
    paths:
      - '/var/lib/docker/containers/*/*.log'
    processors:
      - add_docker_metadata:
          host: "unix:///var/run/docker.sock"

  - type: log
    paths:
      - /var/log/nginx/access.log
    fields:
      type: nginx-access
    fields_under_root: true

output.logstash:
  hosts: ["logstash:5044"]

# Or direct to Elasticsearch
# output.elasticsearch:
#   hosts: ["elasticsearch:9200"]
#   index: "filebeat-%{+yyyy.MM.dd}"
```

## ELKK Pattern (with Kafka)

```yaml
# Add Kafka to the stack
services:
  kafka:
    image: confluentinc/cp-kafka:7.6.0
    environment:
      KAFKA_NODE_ID: 1
      KAFKA_PROCESS_ROLES: broker,controller
      KAFKA_LISTENERS: PLAINTEXT://0.0.0.0:9092,CONTROLLER://0.0.0.0:9093
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092
      KAFKA_CONTROLLER_QUORUM_VOTERS: 1@kafka:9093
      KAFKA_CONTROLLER_LISTENER_NAMES: CONTROLLER
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT
      CLUSTER_ID: 'MkU3OEVBNTcwNTJENDM2Qk'
    ports:
      - "9092:9092"
    volumes:
      - kafka_data:/var/lib/kafka/data
```

```ruby
# Logstash pipeline with Kafka
input {
  kafka {
    bootstrap_servers => "kafka:9092"
    topics => ["app-logs"]
    group_id => "logstash-consumer"
    codec => json
    auto_offset_reset => "latest"
  }
}
```

## Elasticsearch Operations

```bash
# Cluster health
curl -s http://localhost:9200/_cluster/health?pretty

# List indices
curl -s http://localhost:9200/_cat/indices?v

# Create index with mapping
curl -X PUT http://localhost:9200/logs -H 'Content-Type: application/json' -d '{
  "settings": {
    "number_of_shards": 3,
    "number_of_replicas": 1
  },
  "mappings": {
    "properties": {
      "timestamp": { "type": "date" },
      "level": { "type": "keyword" },
      "service": { "type": "keyword" },
      "message": { "type": "text" },
      "trace_id": { "type": "keyword" }
    }
  }
}'

# Search
curl -s http://localhost:9200/logs/_search -H 'Content-Type: application/json' -d '{
  "query": {
    "bool": {
      "must": [
        { "match": { "level": "error" } },
        { "range": { "timestamp": { "gte": "now-1h" } } }
      ]
    }
  },
  "sort": [{ "timestamp": "desc" }],
  "size": 20
}'

# Index Lifecycle Management (ILM)
curl -X PUT http://localhost:9200/_ilm/policy/logs-policy -H 'Content-Type: application/json' -d '{
  "policy": {
    "phases": {
      "hot": { "actions": { "rollover": { "max_size": "50GB", "max_age": "1d" } } },
      "warm": { "min_age": "7d", "actions": { "shrink": { "number_of_shards": 1 } } },
      "delete": { "min_age": "30d", "actions": { "delete": {} } }
    }
  }
}'
```

## K8s Deployment (ECK Operator)

```bash
# Install ECK
helm install elastic-operator elastic/eck-operator -n elastic-system --create-namespace
```

```yaml
apiVersion: elasticsearch.k8s.elastic.co/v1
kind: Elasticsearch
metadata:
  name: logs
spec:
  version: 8.12.0
  nodeSets:
    - name: hot
      count: 3
      config:
        node.roles: ["master", "data_hot", "ingest"]
      volumeClaimTemplates:
        - metadata:
            name: elasticsearch-data
          spec:
            accessModes: ["ReadWriteOnce"]
            storageClassName: fast-ssd
            resources:
              requests:
                storage: 100Gi
      podTemplate:
        spec:
          containers:
            - name: elasticsearch
              resources:
                requests:
                  memory: 4Gi
                  cpu: 2
                limits:
                  memory: 4Gi
    - name: warm
      count: 2
      config:
        node.roles: ["data_warm"]
      volumeClaimTemplates:
        - metadata:
            name: elasticsearch-data
          spec:
            accessModes: ["ReadWriteOnce"]
            storageClassName: standard
            resources:
              requests:
                storage: 500Gi
```
