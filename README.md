# Prueba de Conocimientos – Desarrollador TI

[![CI](https://github.com/cristianrubioa/pruebati/actions/workflows/ci.yml/badge.svg)](https://github.com/cristianrubioa/pruebati/actions/workflows/ci.yml)
![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4-brightgreen)

## Módulo 2: polizas-api

API de gestión de pólizas. Spring Boot 4 + Java 21, H2 en memoria, sin dependencias externas.

Código en [`polizas-api/`](polizas-api); el [`Makefile`](Makefile) raíz orquesta los comandos. Arquitectura y CI documentados en [`docs/`](docs).

### Uso

```bash
make run          # local, puerto 8080
make docker-up    # vía Docker Compose
make test         # JUnit 5 + Mockito + AssertJ
make lint         # revisa formato con Spotless
make format       # aplica formato con Spotless
```

Al arrancar se cargan 2 pólizas de ejemplo ([`DataSeeder`](polizas-api/src/main/java/com/crubio/polizas/DataSeeder.java)). Swagger UI en http://localhost:8080/swagger-ui/index.html — botón **Authorize** para el `x-api-key`.

### Seguridad

`/polizas` y `/riesgos` requieren la cabecera `x-api-key: 123456`.

### Endpoints

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/polizas` | Crear póliza |
| GET | `/polizas?tipo=&estado=` | Listar pólizas, filtros opcionales |
| GET | `/polizas/{id}/riesgos` | Listar riesgos de una póliza |
| POST | `/polizas/{id}/renovar` | Renovar (aplica IPC, estado → RENOVADA) |
| POST | `/polizas/{id}/cancelar` | Cancelar (cascada a sus riesgos) |
| POST | `/polizas/{id}/riesgos` | Agregar riesgo (solo tipo COLECTIVA) |
| POST | `/riesgos/{id}/cancelar` | Cancelar un riesgo |
| POST | `/core-mock/evento` | Mock del CORE legado — solo registra el evento recibido |

### Notas

- El IPC de renovación es configurable en [`application.properties`](polizas-api/src/main/resources/application.properties) (`polizas.ipc-rate=0.05`).
- El riesgo de una póliza individual se crea junto con la póliza; `POST /polizas/{id}/riesgos` es solo para colectivas.
- Cancelar un riesgo ya cancelado es rechazado (HTTP 409).
- La notificación al CORE ocurre después de confirmarse la transacción localmente.

### Ejemplo: crear póliza individual

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
