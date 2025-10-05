# Proyecto BFFs por Canal

Arquitectura de microservicios para exponer información bancaria migrada desde la fuente legacy. La solución incorpora configuración centralizada, descubrimiento de servicios y resiliencia en la capa BFF.

## Estructura
- `config-server/` Servidor de configuración Spring Cloud (puerto 8888) con perfiles en `src/main/resources/config`.
- `discovery-server/` Eureka Server para registro y descubrimiento de servicios (puerto 8761).
- `legacy-api/` Servicio legacy migrado que expone datos bancarios (HTTP :8090).
- `bff-web/` BFF para canal Web (HTTPS :8443).
- `bff-mobile/` BFF para canal Mobile (HTTPS :8444).
- `bff-atm/` BFF para canal ATM (HTTPS :8445).
- `analytics-service/` Consumidor Kafka que genera resúmenes asíncronos (HTTP :8550).
- `infra/kafka/` `docker-compose` para levantar Kafka + Zookeeper localmente.
- `scripts/` Utilidades para iniciar/detener todos los servicios.

## Novedades clave
- Configuración centralizada: `bff-web` obtiene `bff.web.fallback-message`, tiempos Feign y reglas de Resilience4j desde el Config Server.
- Descubrimiento de servicios: `legacy-api` y `bff-web` se registran en Eureka. El Feign client usa `lb://legacy-api` mediante el identificador del servicio.
- Tolerancia a fallos: `DataService` en `bff-web` aplica `@CircuitBreaker` y `@Retry` (Resilience4j). Ante errores entrega valores por defecto y registra el mensaje definido en configuración externa.
- Arquitectura asíncrona: `bff-web` publica eventos en Kafka (`account.summary.request`) y `analytics-service` los procesa calculando totales/balances usando `legacy-api`.
- Seguridad: Los BFF mantienen HTTPS + Basic Auth por roles; `legacy-api` continúa público para el backend interno.

## Requisitos
- JDK 17
- Maven 3.9+
- Docker + Docker Compose (para Kafka)
- `jq` para ejemplo ATM opcional

## Ejecución rápida
1. Levanta Kafka y Zookeeper:
   ```bash
   docker compose -f infra/kafka/docker-compose.yml up -d
   ```
2. Inicia los microservicios (terminales separados o usando el script incluido; config/discovery deben iniciar primero):

```bash
(cd config-server && ./mvnw spring-boot:run)
(cd discovery-server && ./mvnw spring-boot:run)
(cd legacy-api && ./mvnw spring-boot:run)
(cd analytics-service && ./mvnw spring-boot:run)
(cd bff-web && ./mvnw spring-boot:run)
(cd bff-mobile && ./mvnw spring-boot:run)
(cd bff-atm && ./mvnw spring-boot:run)
```

Alternativamente, ejecutar todo con:
```bash
bash scripts/run-all.sh
```
Detener con:
```bash
bash scripts/stop-all.sh
```

## Verificaciones recomendadas
- Config Server: `curl http://localhost:8888/bff-web.yml`
- Eureka: abrir `http://localhost:8761` y validar registro de `legacy-api` y `bff-web`.
- Actuator: `curl http://localhost:8090/actuator/health` y `curl -k -u web_user:bff-web-123 https://localhost:8443/actuator/health`
- Swagger: `http://localhost:8090/swagger-ui/index.html`

## Pruebas funcionales
- Web summary:
  ```bash
  curl -k -u web_user:bff-web-123 \
    "https://localhost:8443/api/web/accounts/123/summary"
  ```
- Resiliencia: detener `legacy-api` y repetir la solicitud anterior. Se observará lista vacía/balance cero y en logs del BFF el mensaje externo "Servicio temporalmente no disponible...".
- Mobile summary:
  ```bash
  curl -k -u mobile_user:bff-mobile-123 \
    "https://localhost:8444/api/mobile/accounts/123/summary"
  ```
- ATM login + balance (requiere `jq`):
  ```bash
  TOKEN=$(curl -sk -u atm_user:bff-atm-123 -X POST \
    -H 'X-CARD: 1111' -H 'X-PIN: 2222' \
    https://localhost:8445/api/atm/login | jq -r .token)
  curl -sk -u atm_user:bff-atm-123 -H "X-ATM-TOKEN: $TOKEN" \
    "https://localhost:8445/api/atm/balance?accountId=123"
  ```
- Flujo asíncrono (Kafka):
  ```bash
  REQUEST_ID=$(curl -sk -u web_user:bff-web-123 -X POST \
    -H 'Content-Type: application/json' \
    -d '{"from":"2024-01-01","to":"2024-12-31"}' \
    https://localhost:8443/api/web/accounts/123/summary/async | jq -r .requestId)
  curl http://localhost:8550/analytics/summary/$REQUEST_ID
  ```
  También puedes consultar el último resumen calculado para la cuenta:
  ```bash
  curl http://localhost:8550/analytics/summary/account/123/latest
  ```

## Evidencia sugerida para la entrega
1. Captura del dashboard Eureka con los servicios registrados.
2. Salida de `curl http://localhost:8888/bff-web.yml` mostrando las propiedades externas.
3. Respuesta exitosa del resumen Web y otra con el fallback tras apagar `legacy-api`.
4. Health checks de los servicios vía Actuator.
5. Salida del `docker compose` de Kafka y del flujo asíncrono (requestId + respuesta de `analytics-service`).

## Notas
- Los BFF consumen `legacy-api` vía Eureka; si se desea usar URL fija, ajustar `LegacyApiClient`.
- SSL utiliza un keystore de demostración (`keystore.p12`). No usar en producción.
- Los módulos incluyen `mvnw` mínimo que delega a Maven local instalado.

## Arquitectura asíncrona (Kafka)
```
Cliente Web
    │ POST /summary/async (BFF Web)
    ▼
BFF Web ──(Kafka topic account.summary.request)──▶ Analytics Service ──▶ Legacy API
    ▲                                                │
    └───── Consulta resultados REST ─────────────────┘
```
- El BFF publica eventos JSON con `requestId`, `accountId`, `from`, `to` y canal.
- `analytics-service` consume los eventos, obtiene los datos desde `legacy-api`, calcula totales y los expone vía `/analytics/summary/{requestId}`.
- Almacén in-memory guarda el último resultado por cuenta. Puedes consultar `/analytics/summary/account/{accountId}/latest`.
