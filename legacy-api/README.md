# Legacy API

Servicio Spring Boot que expone datos migrados desde archivos CSV (ver `src/main/resources/data`). Se publica en `http://localhost:8090` y se registra en Eureka como `legacy-api`.

## Endpoints destacados
- `GET /legacy/transactions`
- `GET /legacy/interests`
- `GET /legacy/annual`

Swagger UI: `http://localhost:8090/swagger-ui/index.html`

## Ejecución
```
./mvnw spring-boot:run
```

Actuator health: `curl http://localhost:8090/actuator/health`
