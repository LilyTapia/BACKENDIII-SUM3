# Analytics Service

Microservicio que consume eventos de Kafka (`account.summary.request`) para generar resúmenes de cuentas usando `legacy-api` y exponerlos vía REST.

- Puerto: `8550`
- Endpoint principal: `GET /analytics/summary/{requestId}`
- Último resumen por cuenta: `GET /analytics/summary/account/{accountId}/latest`
- Requiere Kafka en `localhost:9092` (ver `infra/kafka/docker-compose.yml`).

## Ejecutar
```
./mvnw spring-boot:run
```

El servicio se registra en Eureka como `analytics-service` y necesita que `legacy-api` esté disponible para procesar los eventos.
