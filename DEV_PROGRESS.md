# DataCraft Development Progress

## Current Version
V0.1

## Current Phase
Phase 2 — Datasource (Completed)

## Repository State
Initialized modular monolith with the Phase 2 datasource management shell; backend, frontend, infrastructure, and CI builds are ready.

## Completed
- DESIGN.md prepared
- AGENTS.md prepared
- DEV_PROGRESS.md prepared
- Maven multi-module parent and server aggregator initialized
- datacraft-common foundation module initialized
- datacraft-api boundary module initialized
- datacraft-bootstrap Spring Boot application initialized
- PostgreSQL, Redis, and Flyway infrastructure configured
- Vue 3, TypeScript, Vite, Element Plus, Pinia, Vue Router, Vue Flow, Axios, ECharts, and Monaco dependencies initialized
- Docker Compose development environment added and verified healthy
- GitHub Actions backend/frontend CI added
- Backend tests and package build verified with Java 21
- Frontend dependency installation and production build verified
- PostgreSQL-backed `dc_user`, `dc_role`, `dc_menu` and relationship migrations
- Idempotent environment-configured administrator initialization with BCrypt
- Stateless JWT login, current-user and role-filtered menu APIs
- Console-first login page, guarded routes, Pinia auth store, and responsive AppLayout
- Phase 1 backend and frontend unit/component tests
- PostgreSQL-backed `dc_datasource` migration with type, port, and status constraints
- AES-256-GCM datasource credential encryption with fail-fast key validation
- PostgreSQL/MySQL JDBC URL generation and bounded connection testing
- Datasource CRUD, safe test results, duplicate/not-found validation, and password-free API responses
- Datasource console list/create/edit/test/delete workflow
- Phase 2 backend and frontend unit/component tests, package build, and Docker smoke verification

## In Progress
- None

## Next
- Phase 3 — Metadata
- Implement metadata collector, dataset/field model, and asset browsing UI

## Not Started
- Metadata
- Pipeline
- Quality
- Scheduler
- DataX
- Camel
- SeaTunnel
- Flink

## Blocked
None
