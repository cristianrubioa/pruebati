# polizas-api

API de gestión de pólizas (Módulo 2 de la prueba técnica). Spring Boot 4 + Java 21, persistencia en memoria (H2), sin dependencias externas.

El código vive en [`polizas-api/`](polizas-api); el `Makefile` de la raíz orquesta los comandos ahí dentro.

## Cómo correr

```bash
make run          # local, con ./mvnw spring-boot:run (puerto 8080)
# o
make docker-up    # vía Docker Compose
```

Al arrancar se cargan 2 pólizas de ejemplo (una individual, una colectiva) — ver `DataSeeder`.

## Documentación interactiva (Swagger)

Con la app corriendo: http://localhost:8080/swagger-ui/index.html

Usa el botón **Authorize** para pegar el `x-api-key` una sola vez y probar los endpoints desde el navegador. El JSON de OpenAPI está en `/v3/api-docs`.

## Cómo probar

```bash
make test     # ejecuta la suite (JUnit 5 + Mockito + AssertJ)
make lint     # verifica formato (Spotless)
make format   # aplica formato
```

## Seguridad

Todos los endpoints bajo `/polizas` y `/riesgos` requieren el header:

```
x-api-key: 123456
```

## Endpoints

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/polizas` | Crear póliza (ver nota de gap #1 abajo) |
| GET | `/polizas?tipo=&estado=` | Listar pólizas, filtros opcionales |
| GET | `/polizas/{id}/riesgos` | Listar riesgos de una póliza |
| POST | `/polizas/{id}/renovar` | Renovar (aplica IPC, estado → RENOVADA) |
| POST | `/polizas/{id}/cancelar` | Cancelar (cascada a sus riesgos) |
| POST | `/polizas/{id}/riesgos` | Agregar riesgo (solo tipo COLECTIVA) |
| POST | `/riesgos/{id}/cancelar` | Cancelar un riesgo |
| POST | `/core-mock/evento` | Mock del CORE legado — solo loguea el evento recibido |

## Decisiones frente a ambigüedades del enunciado

El enunciado de la prueba (Módulo 2) tiene tres vacíos que se resolvieron así, de forma deliberada:

1. **No hay endpoint de creación de póliza en la lista del Módulo 2**, aunque el Módulo 1 sí menciona "crear" como requisito de negocio, y el resto de endpoints no tiene sentido sin datos. Se agregó `POST /polizas` para cubrir ese vacío, no como alcance inventado.

2. **El valor de IPC no está definido.** Se dejó configurable en `application.properties` (`polizas.ipc-rate=0.05`, 5% como valor de ejemplo) en vez de un número mágico sin explicar. Cambiarlo ahí ajusta el porcentaje aplicado en cada renovación.

3. **El riesgo único de una póliza individual** se crea junto con la póliza en el mismo `POST /polizas`, ya que `POST /polizas/{id}/riesgos` está reservado explícitamente para pólizas colectivas ("Solo si tipo = Colectiva" en el enunciado).

## Correcciones post-revisión

Detectadas al revisar el código contra las specs propias del proyecto (`openspec/specs/`), no contra el enunciado (que no las exige):

1. **Cancelar un riesgo ya cancelado ahora rechaza la petición** (`BusinessRuleException`, HTTP 409), igual que ya pasaba con `POST /polizas/{id}/cancelar`. Antes `Riesgo.cancelar()` era silenciosamente idempotente y volvía a notificar a CORE sin necesidad.

2. **La notificación a CORE ahora ocurre después del commit real de la transacción**, no antes. `CoreNotifier.notificar()` registra un `TransactionSynchronization.afterCommit()` cuando hay una transacción activa, en vez de llamar al mock CORE en medio del método `@Transactional`. Esto cumple lo que la spec de `core-integration` ya exigía ("SHALL call the mock CORE endpoint after the local state change is committed").

## Ejemplo: crear póliza individual

```bash
curl -X POST http://localhost:8080/polizas \
  -H "x-api-key: 123456" \
  -H "Content-Type: application/json" \
  -d '{
    "tipo": "INDIVIDUAL",
    "vigenciaMeses": 12,
    "canonMensual": 1500000,
    "riesgos": [{"descripcion": "Incendio - apartamento 402"}]
  }'
```
