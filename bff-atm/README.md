# BFF Cajeros (bff-atm)

Canal: ATM. Expone HTTPS en `:8445` y consume `legacy-api` vía OpenFeign.

Credenciales (Basic Auth)
- USER: `atm_user` / `bff-atm-123`
- ADMIN: `atm_admin` / `bff-atm-admin` (también USER)

Endpoints clave
- `POST /api/atm/login` (headers: `X-CARD`, `X-PIN`) → emite token
- `GET /api/atm/balance?accountId=...` (headers: `X-ATM-TOKEN`) → `AccountAtmDto`
- `POST /api/atm/withdraw?accountId=...&amount=...` (headers: `X-ATM-TOKEN`) → valida token
- `GET /api/atm/accounts/{accountId}/transactions` → `PageDto<TransactionSlimDto>`
- `GET /api/atm/admin/metrics` (solo ADMIN)

Flujo ejemplo
```
TOKEN=$(curl -sk -u atm_user:bff-atm-123 -X POST \
  -H 'X-CARD: 1111' -H 'X-PIN: 2222' https://localhost:8445/api/atm/login | jq -r .token)

curl -sk -u atm_user:bff-atm-123 -H "X-ATM-TOKEN: $TOKEN" \
  "https://localhost:8445/api/atm/balance?accountId=123"
```

Configuración
- HTTPS habilitado con `keystore.p12`. Ver `src/main/resources/application.yml`.
- Seguridad por roles + validación de token propio de ATM.
