# CLAUDE.md

## Commits
- Format: `:gitmoji: [scope] Message` — message starts uppercase, written in English.
- Scopes: `back` · `infra` · `test` · `doc`
- No co-author lines — never add `Co-Authored-By` trailers.

## Code Style
- Formatter: Spotless with `google-java-format` (`make format` to apply, `make lint` to verify)
- Java 21, standard Maven layout (`src/main/java`, `src/test/java`)
- DTOs for request/response bodies, never expose JPA entities directly on controllers
- Business rule violations throw a dedicated exception, mapped to HTTP responses by a single `@ControllerAdvice` — no manual try/catch-to-response in controllers

## Testing
- JUnit 5, `assert`-style via AssertJ where useful
- One test class per service/controller under test; name mirrors the class under test + `Test`
- Business-rule tests reference the scenario they cover from `openspec/changes/polizas-api/specs/`

## Language
- Code (variables, functions, classes, comments): English
- Domain terms from the business (`Poliza`, `Riesgo`, `tipo`, `estado`, `INDIVIDUAL`, `COLECTIVA`): kept in Spanish, matching the test's own domain language — do not translate these to English

## Project Structure
- `com.crubio.polizas.poliza` — policy domain (entity, repository, service, controller)
- `com.crubio.polizas.riesgo` — risk domain
- `com.crubio.polizas.core` — mock CORE integration
- `com.crubio.polizas.security` — API key enforcement
- `com.crubio.polizas.common` — shared exceptions/error handling

## Dev Environment
- `make run` starts the app locally, `make docker-up` starts it via Docker Compose — never call `./mvnw` or `docker compose` directly, always through `make <target>`. If a target is missing, add one to the Makefile instead of falling back to raw commands.
