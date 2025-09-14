# BFF Web (bff-web)

Canal: Web. Expone HTTPS en `:8443` y consume `legacy-api` vía OpenFeign.

Credenciales (Basic Auth)
- USER: `web_user` / `bff-web-123`
- ADMIN: `web_admin` / `bff-web-admin` (tiene roles ADMIN y USER)

Endpoints clave
- `GET /api/web/accounts/{accountId}/summary` → `AccountWebDto{ accountId, balance, transactions }`
- `GET /api/web/accounts/{accountId}/transactions` → `PageDto<TransactionDTO>`
- `GET /api/web/admin/metrics` (solo ADMIN)

Ejemplos rápidos
```
curl -k -u web_user:bff-web-123 https://localhost:8443/api/web/accounts/123/summary
curl -k -u web_admin:bff-web-admin https://localhost:8443/api/web/admin/metrics
```

Configuración
- HTTPS habilitado con `keystore.p12` (demo). Ver `src/main/resources/application.yml`.
- Seguridad por roles con `@PreAuthorize`.
