# ADR 0006: Phase 1 Authentication and System Shell

## Status

Accepted

## Context

DataCraft needs its first usable control-plane surface before datasource and pipeline work begins. The platform database is PostgreSQL, while MySQL and PostgreSQL are later connector types. Authentication must not leak credentials or bind future pipeline modules to security implementation details.

## Decision

- Keep authentication in a dedicated `datacraft-auth` module inside the modular monolith.
- Expose login, current-user, and role-filtered menu endpoints through the API boundary.
- Use stateless HS256 JWT access tokens with a secret from `DATACRAFT_JWT_SECRET`; do not add refresh tokens in Phase 1.
- Hash passwords with BCrypt. Create the first administrator idempotently from environment variables after Flyway migrations complete.
- Keep User, Role, Menu entities private to auth persistence; return DTOs and the shared `ApiResponse` envelope from controllers.
- Render the approved console-first Vue shell from backend menu metadata; future module entries route to placeholders.

## Consequences

The initial deployment requires explicit admin and JWT environment variables. The auth module can evolve independently and future datasource/pipeline modules only depend on protected API contracts. A later phase can replace or extend token rotation without changing the domain tables or menu contract.
