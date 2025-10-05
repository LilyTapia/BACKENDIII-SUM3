# Kafka local (docker-compose)

Inicia un broker de Kafka y Zookeeper usando imágenes de Confluent.

## Requisitos
- Docker
- Docker Compose 1.29+ o plugin integrado

## Uso
```bash
docker compose -f infra/kafka/docker-compose.yml up -d
```

Detener y eliminar contenedores:
```bash
docker compose -f infra/kafka/docker-compose.yml down
```

El broker expone `localhost:9092`. Los tópicos se crean automáticamente al publicar/consumir.
