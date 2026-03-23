
# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Pickle is a Spring Boot 3.5.9 backend (Java 21) for shared ledger/expense management with OAuth authentication. Uses
MySQL, JWT, AWS S3, and Prometheus/Grafana monitoring. Core domains: User, Mate (friend/invitation), LedgerEntry
(shared expenses), Verdict (judgment/voting).

## Build & Run Commands

```bash
# Build (runs tests excluding REST docs)
./gradlew clean build

# Run tests only (excludes @Tag("restdocs") tests)
./gradlew test

# Run a single test class
./gradlew test --tests 'com.example.demo.application.user.UserServiceTest'

# Run REST documentation tests only
./gradlew docsTest

# Generate OpenAPI spec (depends on docsTest)
./gradlew openapi3

# Run locally (profile=local, generates OpenAPI first)
./gradlew bootRun

# Run with specific profile
./gradlew bootRun --args='--spring.profiles.active=dev'
```

Spring profiles and DDL strategies: `local` (default, localhost MySQL, `create`), `dev` (`update`), `prod`
(`validate`), `test` (`create-drop`).

## Architecture

Hexagonal (Ports & Adapters) with three layers:

- **`domain/`** — Pure business logic. JPA entities extending `BaseEntity` (audit fields), value objects (`Nickname`,
  `InvitationCode`), enums (`LedgerCategory`, `LedgerType`, `PaymentMethod`, `MateStatus`, `VerdictType`, `Provider`),
  domain interface ports (`FileStorage`, `NicknameGenerator`, `RandomBytesSource`), and repository interfaces. No Spring
  service annotations. Keep free of external dependencies.
- **`application/`** — Use cases. `@Service` classes with `@Transactional`. Application-level DTOs and commands (e.g.,
  `UpsertLedgerCommand`). Services: `UserService`, `AuthService`, `LedgerService`, `MateService`, `VerdictService`.
  Throws `IllegalArgumentException` for domain violations, `UnauthorizedException` for auth failures.
- **`infrastructure/`** — Controllers, filters, interceptors, OAuth implementations, AWS integration, and web DTOs.
  `LoginInterceptor` validates JWT; `@UserId` annotation + `UserIdArgumentResolver` resolves the authenticated user ID.
  `HttpLoggingFilter` logs HTTP requests/responses with MDC request tracking. `GlobalExceptionHandler` provides
  centralized error handling.

DTO separation: web DTOs live in `infrastructure/controller/dto/`, application DTOs in `application/dto/`.

## Code Conventions

- **DI**: Constructor injection via Lombok `@RequiredArgsConstructor`
- **Transactions**: `@Transactional` for writes, `@Transactional(readOnly = true)` for reads
- **Entities**: `@NoArgsConstructor(access = AccessLevel.PROTECTED)`, extend `BaseEntity`
- **Repositories**: Extend Spring Data `Repository` (not `JpaRepository`), only declare needed methods
- **Naming**: Services → `{Domain}Service`, Controllers → `{Domain}Controller`, Web requests →
  `{Action}{Domain}WebRequest`, Web responses → `{Domain}{Action}WebResponse`
- **Style**: Google Java Style Guide
- **Language**: Code comments, error messages, and test display names are in Korean

## Testing

- **Integration tests**: Extend `AbstractIntegrationTest` (truncates all tables between tests). Uses
  `@SpringBootTest(webEnvironment = NONE)`. Test data via `DbUtils.givenSavedUser()`.
- **REST docs tests**: `@WebMvcTest` + `@AutoConfigureRestDocs` + `@Tag("restdocs")`. These generate OpenAPI specs and
  run separately via `./gradlew docsTest`.
- **Pattern**: Given-When-Then with `@Nested` classes and `@DisplayName`
- **Test DB**: localhost:3306/dnd_test (user: dnd, password: 1234)

## API Documentation Pipeline

Tests tagged `@Tag("restdocs")` → `docsTest` → `openapi3` (generates YAML) → `copyOpenApiToStatic` (copies to static
resources) → bundled into bootJar and served at `/swagger-ui/`.

## CI/CD

GitHub Actions workflows:

- **ci.yml**: Push/PR to `main` — Java 21 Corretto, MySQL 8.0 service, Gradle build + test
- **deploy-dev.yml**: Dev environment deployment
- **deploy-prod.yml**: Prod environment deployment
- **deploy-monitoring.yml**: Monitoring infrastructure deployment
- **pr-stats.yml**: PR statistics tracking

PR template requires: issue number, summary, design intent, review notes.
