# BFF Web (bff-web)

Canal: Web. Expone HTTPS en `:8443`, consume `legacy-api` vía OpenFeign y se integra con Spring Cloud Config + Eureka para configuración dinámica y descubrimiento.

## Credenciales (Basic Auth)
- USER: `web_user` / `bff-web-123`
- ADMIN: `web_admin` / `bff-web-admin` (roles ADMIN y USER)

## Endpoints clave
- `GET /api/web/accounts/{accountId}/summary` → `AccountWebDto{ accountId, balance, transactions }`
- `GET /api/web/accounts/{accountId}/transactions` → `PageDto<TransactionDTO>`
- `POST /api/web/accounts/{accountId}/summary/async` → Emite un evento Kafka y responde con `requestId`
- `GET /api/web/admin/metrics` (solo ADMIN)

## Ejemplos rápidos
```
curl -k -u web_user:bff-web-123 https://localhost:8443/api/web/accounts/123/summary
curl -k -u web_admin:bff-web-admin https://localhost:8443/api/web/admin/metrics
# Async summary (Kafka debe estar activo)
curl -k -u web_user:bff-web-123 -X POST \
  -H 'Content-Type: application/json' \
  -d '{"from":"2024-01-01","to":"2024-12-31"}' \
  https://localhost:8443/api/web/accounts/123/summary/async
```

## Configuración y resiliencia
- HTTPS habilitado con `keystore.p12` (demo). Ver `src/main/resources/application.yml`.
- Obtiene propiedades externas (`bff.web.fallback-message`, Resilience4j, timeouts) desde `config-server` (`spring.config.import=...`).
- Descubre `legacy-api` a través de Eureka; el Feign client usa `name = "legacy-api"` y balanceo de carga.
- `DataService` aplica `@CircuitBreaker` y `@Retry` (Resilience4j). Ante fallos entrega valores por defecto y registra el mensaje externo configurado.
- Publica eventos en Kafka (`account.summary.request`) para procesamientos asíncronos con `analytics-service`.
- Seguridad por roles con `@PreAuthorize`.
