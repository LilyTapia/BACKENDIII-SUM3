# BFF Móvil (bff-mobile)

Canal: Mobile. Expone HTTPS en `:8444` y consume `legacy-api` vía OpenFeign.

Credenciales (Basic Auth)
- USER: `mobile_user` / `bff-mobile-123`
- ADMIN: `mobile_admin` / `bff-mobile-admin` (también USER)

Endpoints clave
- `GET /api/mobile/accounts/{accountId}/summary` → `AccountMobileDto{ accountId, balance }`
- `GET /api/mobile/accounts/{accountId}/transactions` → `PageDto<TransactionSlimDto>`
- `GET /api/mobile/admin/metrics` (solo ADMIN)

Ejemplos rápidos
```
curl -k -u mobile_user:bff-mobile-123 https://localhost:8444/api/mobile/accounts/123/summary
curl -k -u mobile_user:bff-mobile-123 "https://localhost:8444/api/mobile/accounts/123/transactions?page=0&size=5"
```

Configuración
- HTTPS habilitado con `keystore.p12`. Ver `src/main/resources/application.yml`.
- Seguridad por roles con `@PreAuthorize`.
