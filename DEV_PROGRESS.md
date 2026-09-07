# DataCraft Development Progress

## Current Version
V0.6

## Current Phase
Phase 9 — Quality (Completed)

## Repository State
Modular monolith with system administration, datasource management, metadata browsing, Pipeline modeling, Vue Flow editor, Native execution engine, and SPI-based engine architecture; Java package namespace unified to com.datacraft, execution module split into engine-api and engine-native, Pipeline and Engine management APIs completed.

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
- Built-in 10-node metadata library through NodeRegistry: database source, filter, seven quality checks, and database sink
- Pipeline component library labels localized to Chinese while preserving stable technical node type identifiers
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
- Standalone `datacraft-quality` module wired into the Maven reactor and bootstrap application
- PostgreSQL-backed `dc_quality_result` and `dc_quality_sample` tables with cascade cleanup and lookup indexes
- Unified NULL, UNIQUE, RANGE, REGEX, LENGTH, ENUM, and CUSTOM SQL quality rule registry
- MySQL/PostgreSQL identifier quoting, regex dialects, custom SQL safety validation, and parameter binding
- JDBC table-level quality checks with total/error/pass/passRate metrics and at most 1000 persisted error samples
- Authenticated quality check and result APIs at `/api/v1/quality/checks` and `/api/v1/quality/results`
- Quality result page with quick NULL check, metrics, status, and error sample display
- Quality node metadata registered for Native Pipeline editor support
- Phase 9 quality backend and frontend tests and production build verification
- Fixed NativePipelineExecutor Spring constructor injection and verified backend startup after rebuild
- Added ADMIN-protected system management for users, roles, menus, role assignment, and role-menu authorization
- Added nested system settings navigation with `/system/users`, `/system/roles`, and `/system/menus` routes
- Added Flyway seed migration for system-management second-level menus and ADMIN menu grants
- Kept password hashes out of system-management responses and reused existing auth tables
- Removed redundant system-management page banner and ADMIN description from user, role, and menu views
- Converted user, role, and menu create/edit forms to modal dialogs with overlay close and responsive panels
- Replaced sidebar menu initial-letter placeholders with semantic SVG icons and a generic fallback icon
- Datasource create/edit moved into modal form with unsaved configuration testing and save gating
- Added temporary datasource connection-test APIs, edit password reuse, and frontend regression coverage
- Added shared frontend readability baseline with system font fallback, clearer contrast, and larger UI text
- Increased datasource and Pipeline list header text to 12px for clearer scanning
- Added shared frontend pagination for datasource, asset/table fields, Pipeline, quality, and system user/role/menu lists
- Added page-size switching, boundary-safe page navigation, and reset-to-first-page behavior after asset filtering or selection changes
- Verified frontend with 35 Vitest tests and a successful production build

## In Progress
- None

## Next
- Phase 10 — Scheduler
- Add Quartz scheduling, Cron configuration, execution history, retry/cancel, and audit log capabilities

## Not Started
- Scheduler
- DataX
- Camel
- SeaTunnel
- Flink

## Blocked
None
