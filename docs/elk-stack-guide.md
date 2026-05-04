# ELK Stack Setup Guide

This document covers the fundamental features of the ELK stack components, how to set them up locally using Docker Compose, and best practices regarding log indexing.

## 1. Architecture and Fundamental Features

The ELK stack forms a robust data pipeline: `Filebeat -> Logstash -> Elasticsearch -> Kibana`.

*   **Filebeat (The Shipper):** A lightweight agent that reads log files or container logs and forwards them. It remembers where it stopped reading, ensuring logs aren't missed upon restart.
*   **Logstash (The Processor):** Ingests data, transforms/parses it using filters (like `grok`), and sends it to a stash like Elasticsearch. It turns unstructured log text into structured, queryable JSON fields.
*   **Elasticsearch (The Storage & Search Engine):** Stores structural log data as JSON documents and indexes fields for near real-time, lightning-fast search capabilities.
*   **Kibana (The Visualization Dashboard):** The web interface for exploring Elasticsearch data, visualizing logs, and creating dashboards.

## 2. Local Setup Guide

Create an `elk` folder in your project to hold the configurations.

### Logstash Configuration (`elk/logstash.conf`)

```logstash
input {
    beats {
        port => 5044
    }
}

filter {
    # Add filters here later to parse application logs
}

output {
    elasticsearch {
        hosts => ["http://elasticsearch:9200"]
        index => "log-stock-management-%{+YYYY.MM.dd}"
    }
    stdout { codec => rubydebug } 
}
```

### Filebeat Configuration (`elk/filebeat.yml`)

```yaml
filebeat.inputs:
- type: container
  paths:
    - '/var/lib/docker/containers/*/*.log'

output.logstash:
  hosts: ["logstash:5044"]
```

### Docker Compose Configuration

Add the following services to your `docker-compose.yml`:

```yaml
  elasticsearch:
    container_name: elasticsearch
    image: docker.elastic.co/elasticsearch/elasticsearch:8.12.0
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=false
      - "ES_JAVA_OPTS=-Xms512m -Xmx512m"
    ports:
      - "9200:9200"
    volumes:
      - elasticsearch-data:/usr/share/elasticsearch/data

  logstash:
    container_name: logstash
    image: docker.elastic.co/logstash/logstash:8.12.0
    volumes:
      - ./elk/logstash.conf:/usr/share/logstash/pipeline/logstash.conf:ro
    ports:
      - "5044:5044"
    depends_on:
      - elasticsearch

  kibana:
    container_name: kibana
    image: docker.elastic.co/kibana/kibana:8.12.0
    ports:
      - "5601:5601"
    environment:
      - ELASTICSEARCH_HOSTS=http://elasticsearch:9200
    depends_on:
      - elasticsearch

  filebeat:
    container_name: filebeat
    image: docker.elastic.co/beats/filebeat:8.12.0
    user: root
    volumes:
      - ./elk/filebeat.yml:/usr/share/filebeat/filebeat.yml:ro
      - /var/lib/docker/containers:/var/lib/docker/containers:ro
      - /var/run/docker.sock:/var/run/docker.sock:ro
    command: filebeat -e -strict.perms=false
    depends_on:
      - logstash

volumes:
  elasticsearch-data:
```

## 3. Best Practices: Time-Based Indexing

In the Logstash output, we append a datetime string to the Elasticsearch index (e.g., `index => "log-stock-management-%{+YYYY.MM.dd}"`). This is known as **time-based indexing**.

### Why use a date suffix?

1.  **Easy Data Retention and Deletion:** Deleting 30-day-old logs is as simple as dropping an entire index, which is instantaneous and frees disk space with minimal CPU cost. Deleting individual documents from a massive single index is slow and fragmented.
2.  **Search Performance:** Querying recent logs means Elasticsearch only searches today's (or yesterday's) index, rather than scanning months of data.
3.  **Data Tiering:** It allows you to move older, read-only indices to cheaper, slower hardware nodes while keeping today's active index on fast SSDs.

### Searching Across Time Ranges

When searching across multiple days (e.g., "past 7 days"), you configure Kibana to use a wildcard pattern like `log-stock-management-*`.

Elasticsearch smartly resolves this: it looks at the query's date range, identifies the specific daily indices that fall within that window, and queries across only those specific indices in parallel. This mechanism is fast and entirely invisible to the user.
