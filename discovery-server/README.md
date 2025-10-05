# Discovery Server

Servidor Eureka (Spring Cloud Netflix) para registro de microservicios.

- Puerto: `8761`
- No se registra a sí mismo (`register-with-eureka: false`).
- Exponer `http://localhost:8761` para visualizar servicios conectados.

## Cómo ejecutar
```
./mvnw spring-boot:run
```

Servicios configurados para registrarse:
- `legacy-api`
- `bff-web`
