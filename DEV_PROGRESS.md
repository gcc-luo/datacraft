# DataCraft Development Progress

## Current Version
V0.4

## Current Phase
Phase 8 — Architecture Compliance Refactor (Completed)

## Repository State
Modular monolith with datasource management, metadata browsing, Pipeline modeling, Vue Flow editor, Native execution engine, and SPI-based engine architecture; Java package namespace unified to com.datacraft, execution module split into engine-api and engine-native, Pipeline and Engine management APIs completed.

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
- Docker Compose development environment with PostgreSQL, Redis, and MySQL test container
- GitHub Actions backend/frontend CI added
- Backend tests and package build verified with Java 21
- Frontend dependency installation and production build verified
- PostgreSQL-backed dc_user, dc_role, dc_menu and relationship migrations
- Idempotent environment-configured administrator initialization with BCrypt
- Stateless JWT login, current-user and role-filtered menu APIs
- Console-first login page, guarded routes, Pinia auth store, and responsive AppLayout
- Phase 1 backend and frontend unit/component tests
- PostgreSQL-backed dc_datasource migration with type, port, and status constraints
- AES-256-GCM datasource credential encryption with fail-fast key validation
- PostgreSQL/MySQL JDBC URL generation and bounded connection testing
- Datasource CRUD, safe test results, duplicate/not-found validation, and password-free API responses
- Datasource console list/create/edit/test/delete workflow
- Phase 2 backend and frontend unit/component tests, package build, and Docker smoke verification
- Standalone datacraft-metadata module wired into the Maven reactor and bootstrap application
- PostgreSQL-backed dc_dataset and dc_dataset_field schema with foreign-key cascade and lookup indexes
- JDBC metadata collectors for PostgreSQL and MySQL tables, columns, primary keys, remarks, and estimated row counts
- Transactional datasource snapshot replacement with password-free dataset/field API DTOs
- Metadata sync, dataset list/filter, and dataset detail REST APIs with authenticated access
- Asset catalog UI with datasource selector, schema/table split view, breadcrumb detail view, field search, and sync action
- Phase 3 backend and frontend unit/component tests and production builds
- Standalone datacraft-pipeline module wired into the Maven reactor and bootstrap application
- PostgreSQL-backed dc_pipeline, dc_pipeline_node, and dc_pipeline_edge graph tables with cascade relationships and graph snapshots
- Pipeline domain model, immutable API DTOs, status/version/execution-strategy fields, and graph persistence boundary
- Built-in DATABASE_SOURCE, FILTER, and DATABASE_SINK NodeMetadata definitions through a metadata-only NodeRegistry
- Pipeline name, node-key, node-type, dangling-edge, duplicate-edge, self-loop, and DAG cycle validation
- Authenticated Pipeline CRUD and NodeMetadata REST APIs with stable validation/not-found error codes
- Phase 4 backend unit, persistence mapping, migration, application-service, and MVC tests
- Typed Pipeline API client and Vue Flow graph mapping for control-plane DTOs
- Pipeline list route with create, browse, and delete workflow
- Metadata-driven node palette with search, category grouping, and drag payloads
- Custom Pipeline canvas nodes with engine/category identity and source/target handles
- Pipeline inspector for pipeline properties, node properties, engine preference, coordinates, and JSON validation
- Three-column Pipeline editor with drag/drop nodes, DAG connections, save, update, delete, and dirty-state feedback
- Phase 5 frontend component/view tests and production build
- Chinese Git commit convention documented in AGENTS.md
- ExecutionEngine SPI with engine metadata, capability declaration, and health contract
- EngineRegistry with stable ordering, duplicate protection, and unknown-engine validation
- Built-in Native engine registration with embedded deployment metadata and loaded health status
- Authenticated engine discovery API at GET /api/v1/engines
- Phase 6 backend tests and full Maven test verification
- Immutable ExecutionPlan, ExecutionStagePlan, and DataReference models
- Deterministic Pipeline DAG topological planning with engine selection precedence
- Engine capability and registry validation during planning
- Database source/sink table references without credential exposure
- Cross-stage logical query references marked MATERIALIZATION_REQUIRED
- Phase 7 planner tests and full Maven test verification
- Controlled JDBC connection provider port with transient credential decryption
- Native JDBC source/filter/sink runtime for linear database pipelines
- SQL filter pushdown, database-aware identifier quoting, and positional field mapping
- Bounded forward-only reads and 500-row PreparedStatement batches without full-result List
- APPEND/TRUNCATE sink modes with transactional commit, rollback, and auto-commit restoration
- Phase 8 Native Runtime design, tests, and Maven verification
- Java package namespace unified from io.datacraft to com.datacraft across all backend modules
- Execution module split into datacraft-engine-api (SPI + planner + registry) and datacraft-engine-native (Native engine + controllers + tests)
- Pipeline validate/plan/execute REST endpoints at POST /api/v1/pipelines/{id}/validate, /plan, /execute
- Engine management APIs: GET /api/v1/engines/{code}, POST /api/v1/engines/{code}/test, PUT /api/v1/engines/{code}
- MySQL test container added to Docker Compose for metadata and Pipeline source testing
- Core ADR documents created: 0001-modular-monolith, 0002-engine-spi, 0003-datareference, 0004-datax-local-process, 0005-camel-embedded
- Full Maven test verification: 82 tests, 0 failures, 0 errors

## In Progress
- None

## Next
- Phase 9 — Quality
- Add Quality Node, QualityResult, sampling, rule execution, and quality report capabilities

## Not Started
- Quality
- Scheduler
- DataX
- Camel
- SeaTunnel
- Flink

## Blocked
None
