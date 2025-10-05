# Proyecto BFFs por Canal (Sumativa 3 · Semana 8)

Arquitectura de microservicios para exponer información bancaria migrada desde la fuente legacy. La solución incorpora configuración centralizada, descubrimiento de servicios, resiliencia en la capa BFF y autenticación OAuth2 con tokens JWT.

## Estructura
- `config-server/` Servidor de configuración Spring Cloud (puerto 8888) con perfiles en `src/main/resources/config`.
- `discovery-server/` Eureka Server para registro y descubrimiento de servicios (puerto 8761).
- `auth-server/` Authorization Server basado en Spring Authorization Server que emite tokens OAuth2 (puerto 9000).
- `legacy-api/` Servicio legacy migrado que expone datos bancarios (HTTP :8090).
- `bff-web/` BFF para canal Web (HTTPS :8443) con resiliencia, eventos Kafka y consumo vía Eureka.
- `bff-mobile/` BFF para canal Mobile (HTTPS :8444) orientado a consultas resumidas.
- `bff-atm/` BFF para canal ATM (HTTPS :8445) con token interno adicional para operaciones.
- `analytics-service/` Consumidor Kafka que genera resúmenes asíncronos (HTTP :8550).
- `kafka-ui/` Consola web para inspeccionar tópicos y mensajes de Kafka (HTTP :8080).
- `docker-compose.yml` Orquestación completa de la plataforma (Kafka, Config, Eureka, Auth y microservicios).
- `infra/kafka/` Compose mínimo para levantar solo Kafka + Zookeeper en escenarios locales.
- `scripts/` Utilidades para iniciar/detener todos los servicios en modo desarrollo.

## Novedades clave
- **Configuración centralizada:** Los BFF y `analytics-service` obtienen propiedades (Kafka, resiliencia, mensajes de fallback, issuer OAuth2) desde el Config Server.
- **Descubrimiento de servicios:** `legacy-api`, `bff-web` y `analytics-service` se registran en Eureka. El BFF web consume `lb://legacy-api` mediante el identificador del servicio.
- **Seguridad OAuth2:** `auth-server` emite tokens JWT con `client_credentials` y scopes por canal. Los BFF actúan como Resource Servers validando scopes (`SCOPE_bff.*`).
- **Tolerancia a fallos:** `bff-web` mantiene `@CircuitBreaker` y `@Retry` (Resilience4j) ofreciendo respuestas de respaldo configurables.
- **Arquitectura asíncrona:** `bff-web` publica eventos Kafka (`account.summary.request`) y `analytics-service` calcula resúmenes consultables por REST.
- **Contenedores listos:** Dockerfiles por servicio y `docker-compose.yml` permiten levantar todo el ecosistema con un solo comando.

## Requisitos
- JDK 17
- Maven 3.9+
- Docker + Docker Compose (para despliegue completo)
- `jq` para los ejemplos con ATM (opcional)

## Ejecución con Docker Compose
1. Construye y levanta todo el entorno (Kafka, Config, Eureka, Auth, Legacy, BFFs, Analytics):
   ```bash
   docker compose up --build -d
   ```
2. Verifica los contenedores:
   ```bash
   docker compose ps
   ```
   Puedes abrir `http://localhost:8080` para revisar los tópicos en Kafka UI.
3. Detén el entorno:
   ```bash
   docker compose down
   ```

> El archivo `.env` no es necesario. Todos los servicios se comunican por la red `bancoxyz-net` usando los nombres de contenedor definidos.

## Ejecución local (sin Docker)
1. Levanta Kafka si requieres el flujo asíncrono:
   ```bash
   docker compose -f infra/kafka/docker-compose.yml up -d
   ```
2. Inicia los microservicios (usa terminales separados o el script incluido; comienza por config, auth y discovery):
   ```bash
   (cd config-server && ./mvnw spring-boot:run)
   (cd auth-server && ./mvnw spring-boot:run)
   (cd discovery-server && ./mvnw spring-boot:run)
   (cd legacy-api && ./mvnw spring-boot:run)
   (cd analytics-service && ./mvnw spring-boot:run)
   (cd bff-web && ./mvnw spring-boot:run)
   (cd bff-mobile && ./mvnw spring-boot:run)
   (cd bff-atm && ./mvnw spring-boot:run)
   ```
   Alternativa rápida:
   ```bash
   bash scripts/run-all.sh
   ```
   Detén todo con:
   ```bash
   bash scripts/stop-all.sh
   ```

## Autenticación OAuth2 (client credentials)
Tokens firmados por `auth-server` con scopes específicos por canal:

| Cliente | Scopes | Uso principal |
|---------|--------|---------------|
| `bff-web-client` / `bff-web-secret` | `bff.web.read`, `bff.web.write`, `bff.web.admin` | API Web + eventos async |
| `bff-mobile-client` / `bff-mobile-secret` | `bff.mobile.read`, `bff.mobile.admin` | API Mobile |
| `bff-atm-client` / `bff-atm-secret` | `bff.atm.read`, `bff.atm.write`, `bff.atm.admin` | API ATM |

Ejemplos para obtener tokens (requiere `jq` para extraer `access_token`):

```bash
WEB_TOKEN=$(curl -s -u bff-web-client:bff-web-secret \
  -d 'grant_type=client_credentials' \
  --data-urlencode 'scope=bff.web.read bff.web.write' \
  http://localhost:9000/oauth2/token | jq -r .access_token)

MOBILE_TOKEN=$(curl -s -u bff-mobile-client:bff-mobile-secret \
  -d 'grant_type=client_credentials' \
  --data-urlencode 'scope=bff.mobile.read' \
  http://localhost:9000/oauth2/token | jq -r .access_token)

ATM_TOKEN=$(curl -s -u bff-atm-client:bff-atm-secret \
  -d 'grant_type=client_credentials' \
  --data-urlencode 'scope=bff.atm.read bff.atm.write' \
  http://localhost:9000/oauth2/token | jq -r .access_token)
```
Usa los tokens con `Authorization: Bearer <token>` en cada solicitud protegida.

## Verificaciones recomendadas
- Config Server: `curl http://localhost:8888/bff-web.yml`
- Eureka Dashboard: `http://localhost:8761` (espera `legacy-api`, `bff-web`, `analytics-service`).
- Authorization Server: `curl http://localhost:9000/.well-known/openid-configuration`
- Actuator (ejemplo Web):
  ```bash
  curl -k -H "Authorization: Bearer $WEB_TOKEN" https://localhost:8443/actuator/health
  ```
- Kafka topics (si usas Docker): `docker compose exec kafka kafka-topics --bootstrap-server kafka:9092 --list`
- Swagger Legacy: `http://localhost:8090/swagger-ui/index.html`

## Pruebas funcionales
- **Resumen Web síncrono:**
  ```bash
  curl -k -H "Authorization: Bearer $WEB_TOKEN" \
    "https://localhost:8443/api/web/accounts/123/summary"
  ```
- **Resiliencia Web:** Apaga temporalmente `legacy-api` y repite la petición anterior. Obtendrás datos de fallback y en los logs del BFF aparecerá el mensaje externo.
- **Resumen Mobile:**
  ```bash
  curl -k -H "Authorization: Bearer $MOBILE_TOKEN" \
    "https://localhost:8444/api/mobile/accounts/123/summary"
  ```
- **ATM login + balance (token interno + OAuth2):**
  ```bash
  ATM_SESSION=$(curl -sk -H "Authorization: Bearer $ATM_TOKEN" -X POST \
    -H 'X-CARD: 1111' -H 'X-PIN: 2222' \
    https://localhost:8445/api/atm/login | jq -r .token)

  curl -sk -H "Authorization: Bearer $ATM_TOKEN" \
    -H "X-ATM-TOKEN: $ATM_SESSION" \
    "https://localhost:8445/api/atm/balance?accountId=123"
  ```
- **Flujo asíncrono (Kafka):**
  ```bash
  REQUEST_ID=$(curl -sk -H "Authorization: Bearer $WEB_TOKEN" -X POST \
    -H 'Content-Type: application/json' \
    -d '{"from":"2024-01-01","to":"2024-12-31"}' \
    https://localhost:8443/api/web/accounts/123/summary/async | jq -r .requestId)

  curl http://localhost:8550/analytics/summary/$REQUEST_ID
  curl http://localhost:8550/analytics/summary/account/123/latest
  ```

## Evidencia sugerida para la entrega
1. `docker compose ps` o captura del stack mostrando los contenedores activos.
2. Salida de `curl -u bff-web-client:bff-web-secret -d 'grant_type=client_credentials' --data-urlencode 'scope=bff.web.read' http://localhost:9000/oauth2/token` (idealmente formateada con `jq`) para evidenciar la emisión del token y los scopes.
3. `curl http://localhost:8888/bff-web.yml` resaltando propiedades externas (Kafka, issuer, fallback).
4. Dashboard de Eureka con los servicios registrados.
5. Ejecución exitosa del resumen Web (Bearer token) y captura del fallback al detener `legacy-api`.
6. Mensaje del `analytics-service` atendiendo un request asíncrono (requestId y respuesta) o visualización del evento en Kafka UI (`http://localhost:8080`).

## Notas
- Los BFF consumen `legacy-api` vía Eureka; para una URL fija ajusta `LegacyApiClient` o define `LEGACY_API_URL`.
- Los certificados TLS (`keystore.p12`) son solo de demostración.
- Los `mvnw` incluidos delegan a Maven local; instala Maven 3.9+ en tu máquina o usa contenedores.

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
- Un almacén in-memory persiste el último resultado por cuenta para consultas rápidas.
