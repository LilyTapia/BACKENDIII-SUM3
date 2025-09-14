# Proyecto BFFs por Canal

Tres BFFs independientes (Web, Mobile, ATM) que consumen `legacy-api` y exponen respuestas optimizadas por canal.

## Estructura
- `bff-web/` Web BFF (HTTPS :8443)
- `bff-mobile/` Mobile BFF (HTTPS :8444)
- `bff-atm/` ATM BFF (HTTPS :8445)
- `legacy-api/` Fuente de datos (HTTP :8090)

Cada BFF sigue la misma organización: `api` (controllers), `service`, `client` (OpenFeign), `dto`, `config`.

## Seguridad
- HTTPS habilitado en los 3 BFFs (keystore demo incluido).
- Basic Auth con roles y `@PreAuthorize` por endpoint.
- ATM agrega validación de token (`X-ATM-TOKEN`) para operaciones críticas.

## Cómo ejecutar (terminales separados)
```
(cd legacy-api && ./mvnw spring-boot:run)
(cd bff-web && ./mvnw spring-boot:run)
(cd bff-mobile && ./mvnw spring-boot:run)
(cd bff-atm && ./mvnw spring-boot:run)
```

## Scripts
- Iniciar todos: `bash scripts/run-all.sh`
- Detener todos: `bash scripts/stop-all.sh`

## Pruebas rápidas
- Web summary: `curl -k -u web_user:bff-web-123 https://localhost:8443/api/web/accounts/123/summary`
- Mobile summary: `curl -k -u mobile_user:bff-mobile-123 https://localhost:8444/api/mobile/accounts/123/summary`
- ATM login + balance:
```
TOKEN=$(curl -sk -u atm_user:bff-atm-123 -X POST -H 'X-CARD: 1111' -H 'X-PIN: 2222' https://localhost:8445/api/atm/login | jq -r .token)
curl -sk -u atm_user:bff-atm-123 -H "X-ATM-TOKEN: $TOKEN" "https://localhost:8445/api/atm/balance?accountId=123"
```

## Notas
- Los BFFs llaman a `legacy-api` por HTTP en localhost:8090 (ambiente interno).
- DTOs de cuenta y transacción son específicos por canal; se usa `PageDto<T>` para listados paginados.
