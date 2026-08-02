# Arquitectura — polizas-api

Documenta la arquitectura tal como está implementada hoy en `polizas-api/`. No describe diseño objetivo ni patrones aspiracionales, solo lo que existe en el código.

## Capas

Monolito en capas, un proceso, una base H2 en memoria.

```mermaid
flowchart TD
    Client["Cliente HTTP"] --> Filter["ApiKeyFilter\n(x-api-key)"]
    Filter --> Controller["Controllers\nPolizaController / RiesgoController"]
    Controller --> Service["Services\nPolizaService / RiesgoService"]
    Service --> Repo["Repositories\nJPA / Spring Data"]
    Repo --> DB[("H2 in-memory")]
    Service --> Notifier["CoreNotifier"]
    Notifier -->|POST /core-mock/evento| CoreMock["CoreMockController\n(mock del CORE legado)"]
    Service -.->|BusinessRuleException| Advice["GlobalExceptionHandler\n(@RestControllerAdvice)"]
    Controller -.->|ResponseStatusException 404| Advice
```

- **`ApiKeyFilter`** — protege `/polizas` y `/riesgos` (detalle en [Seguridad](#seguridad)).
- **Controllers** — delegan a los services, sin lógica de negocio.
- **Services** — reglas de negocio y transacciones (`@Transactional`).
- **Entities** (`Poliza`, `Riesgo`) — validan sus propias invariantes; `agregarRiesgo`, `renovar`, `cancelar` lanzan `BusinessRuleException`.
- **`GlobalExceptionHandler`** — único punto que traduce excepciones a respuestas HTTP (409 reglas de negocio, 400 validación de DTO).

## Modelo de datos

```mermaid
erDiagram
    POLIZA ||--o{ RIESGO : "tiene"
    POLIZA {
        Long id
        TipoPoliza tipo "INDIVIDUAL | COLECTIVA"
        EstadoPoliza estado "ACTIVA | RENOVADA | CANCELADA"
        Integer vigenciaMeses
        BigDecimal canonMensual
        BigDecimal prima
    }
    RIESGO {
        Long id
        String descripcion
        EstadoRiesgo estado "ACTIVO | CANCELADO"
        Long poliza_id FK
    }
```

Reglas embebidas en el modelo:

- Póliza `INDIVIDUAL` admite máximo 1 riesgo; `COLECTIVA` admite varios.
- `prima = canonMensual * vigenciaMeses`, recalculada en cada renovación.
- Cancelar una póliza cancela en cascada todos sus riesgos activos.

## Flujos principales

### Crear póliza

```mermaid
sequenceDiagram
    participant C as Cliente
    participant PC as PolizaController
    participant PS as PolizaService
    participant DB as H2

    C->>PC: POST /polizas
    PC->>PS: crear(request)
    PS->>PS: new Poliza(...) + agregarRiesgo(...) por cada riesgo
    PS->>DB: save(poliza)
    DB-->>PS: poliza persistida
    PS-->>PC: PolizaResponse
    PC-->>C: 201 Created
```

### Renovar póliza (con notificación al CORE)

```mermaid
sequenceDiagram
    participant C as Cliente
    participant PS as PolizaService
    participant DB as H2
    participant CN as CoreNotifier
    participant CORE as CORE mock

    C->>PS: POST /polizas/{id}/renovar
    PS->>DB: findById(id)
    alt no existe
        PS-->>C: 404
    end
    PS->>PS: poliza.renovar(ipcRate)\n(canon += canon*ipc, estado=RENOVADA)
    PS->>DB: save(poliza)
    PS->>CN: notificar("RENOVACION", id)
    Note over CN: se encola con afterCommit()\nsi hay transacción activa
    PS-->>C: 200 OK
    CN->>CORE: POST /core-mock/evento (tras commit)
    CORE-->>CN: 200 (solo registra)
```

`cancelar` sigue el mismo patrón: valida estado y notifica `CANCELACION` (la cascada a riesgos ya está descrita en [Modelo de datos](#modelo-de-datos)).

### Agregar riesgo a póliza colectiva

```mermaid
sequenceDiagram
    participant C as Cliente
    participant RC as RiesgoController
    participant RS as RiesgoService
    participant DB as H2
    participant CN as CoreNotifier

    C->>RC: POST /polizas/{id}/riesgos
    RC->>RS: agregar(polizaId, request)
    RS->>DB: findById(polizaId)
    alt tipo != COLECTIVA
        RS-->>C: 409 (BusinessRuleException)
    end
    RS->>RS: new Riesgo(...) + poliza.agregarRiesgo(...)
    RS->>DB: save(riesgo)
    RS->>CN: notificar("AGREGAR_RIESGO", polizaId)
    RS-->>C: 201 Created
```

## Integración con el CORE (mock)

`CoreNotifier` llama a `POST /core-mock/evento` **después** de que la transacción local confirma (`TransactionSynchronization.afterCommit`), nunca antes. Si la llamada falla, se registra como warning y no revierte la operación.

## Seguridad

`ApiKeyFilter` exige la cabecera `x-api-key` (valor en `polizas.api-key`, `application.properties`) para toda ruta bajo `/polizas` y `/riesgos`. `/core-mock/evento` y `/swagger-ui/**` quedan fuera del filtro.
