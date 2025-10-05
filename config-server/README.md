# Config Server

Servidor Spring Cloud Config en modo `native`.

- Puerto: `8888`
- Ubicación de propiedades: `src/main/resources/config`
- Perfil activo por defecto: `native`

## Propiedades expuestas
Actualmente contiene `bff-web.yml` con:
- Mensaje externo para fallbacks (`bff.web.fallback-message`).
- Configuración de Resilience4j (circuit breaker y retry).
- Timeouts de Feign (`feign.client.config.default`).

## Cómo ejecutar
```
./mvnw spring-boot:run
```

Una vez iniciado, verificar con:
```
curl http://localhost:8888/bff-web.yml
```
