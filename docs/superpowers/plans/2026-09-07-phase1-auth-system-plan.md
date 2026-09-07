# Phase 1 Auth & System Implementation Plan

> **For agentic workers:** Execute this plan task-by-task with TDD. Each task is independently verifiable and stays inside Phase 1.

**Goal:** Build the first usable DataCraft control-plane shell: PostgreSQL-backed users/roles/menus, JWT login, a protected API, and the approved console-first Vue layout.

**Architecture:** Add a `datacraft-auth` module to the existing modular monolith. API records and the shared response envelope live at the API/common boundaries; auth owns persistence, application services, JWT/security, and controllers. Flyway supplies the schema and static role/menu seeds, while an idempotent application initializer creates the first administrator from environment variables. The Vue app uses a Pinia auth store, guarded routes, backend-provided menu metadata, and an `AppLayout` with placeholders for later phases.

**Tech Stack:** Java 21, Spring Boot 3.5, Spring Security, JJWT, MyBatis-Plus, PostgreSQL, Flyway, JUnit 5; Vue 3, TypeScript, Pinia, Vue Router, Element Plus, Axios, Vite.

---

### Task 1: Wire the auth module and shared API contracts

**Files:**
- Create: `datacraft-server/datacraft-auth/pom.xml`
- Modify: `datacraft-server/pom.xml`
- Modify: `datacraft-server/datacraft-bootstrap/pom.xml`
- Create: `datacraft-server/datacraft-api/src/main/java/io/datacraft/api/auth/LoginRequest.java`
- Create: `datacraft-server/datacraft-api/src/main/java/io/datacraft/api/auth/LoginResponse.java`
- Create: `datacraft-server/datacraft-api/src/main/java/io/datacraft/api/auth/UserSummary.java`
- Create: `datacraft-server/datacraft-api/src/main/java/io/datacraft/api/system/MenuDto.java`
- Create: `datacraft-server/datacraft-common/src/main/java/io/datacraft/common/web/ApiResponse.java`
- Create: `datacraft-server/datacraft-common/src/test/java/io/datacraft/common/web/ApiResponseTest.java`

- [ ] **Step 1: Write the failing contract test**

  Add a JUnit test that constructs `ApiResponse.success("ok")` and asserts `code == "0"`, `message == "success"`, and the payload is present; add an error assertion for a supplied code/message. Do not implement the factory methods yet.

- [ ] **Step 2: Run the test to verify it fails**

  Run `mvn -pl datacraft-server/datacraft-common -am -Dtest=ApiResponseTest test`. Expect compilation failure because `ApiResponse` does not exist.

- [ ] **Step 3: Implement the shared contract and module wiring**

  Implement an immutable generic response record/class with `success` and `failure` factories. Add the API records with Bean Validation annotations (`@NotBlank` on username/password), add the auth module as a server child module, and add Spring Web/Validation/Security/JDBC/MyBatis-Plus/JJWT dependencies to the auth module. Make bootstrap depend on auth. Configure the Maven compiler for Java 21 through the existing parent.

- [ ] **Step 4: Run the focused test and compile**

  Run `mvn -pl datacraft-server/datacraft-common -am -Dtest=ApiResponseTest test` and then `mvn -pl datacraft-server/datacraft-auth -am -DskipTests compile`. Expect the response test to pass and all modules to compile.

- [ ] **Step 5: Commit the focused module/contract change**

  Run `git add datacraft-server docs/superpowers/plans/2026-09-07-phase1-auth-system-plan.md` only if the staged set contains the intended files, then commit `feat(auth): add auth module and api contracts`.

### Task 2: Create the Flyway schema and persistence adapters

**Files:**
- Create: `datacraft-server/datacraft-auth/src/main/resources/db/migration/V1__create_auth_tables.sql`
- Create: `datacraft-server/datacraft-auth/src/main/java/io/datacraft/auth/domain/UserAccount.java`
- Create: `datacraft-server/datacraft-auth/src/main/java/io/datacraft/auth/domain/Role.java`
- Create: `datacraft-server/datacraft-auth/src/main/java/io/datacraft/auth/domain/MenuItem.java`
- Create: `datacraft-server/datacraft-auth/src/main/java/io/datacraft/auth/domain/UserRepository.java`
- Create: `datacraft-server/datacraft-auth/src/main/java/io/datacraft/auth/domain/MenuRepository.java`
- Create: `datacraft-server/datacraft-auth/src/main/java/io/datacraft/auth/infrastructure/persistence/entity/UserEntity.java`
- Create: `datacraft-server/datacraft-auth/src/main/java/io/datacraft/auth/infrastructure/persistence/entity/RoleEntity.java`
- Create: `datacraft-server/datacraft-auth/src/main/java/io/datacraft/auth/infrastructure/persistence/entity/MenuEntity.java`
- Create: `datacraft-server/datacraft-auth/src/main/java/io/datacraft/auth/infrastructure/persistence/mapper/UserMapper.java`
- Create: `datacraft-server/datacraft-auth/src/main/java/io/datacraft/auth/infrastructure/persistence/mapper/RoleMapper.java`
- Create: `datacraft-server/datacraft-auth/src/main/java/io/datacraft/auth/infrastructure/persistence/mapper/MenuMapper.java`
- Create: `datacraft-server/datacraft-auth/src/main/java/io/datacraft/auth/infrastructure/persistence/JdbcUserRepository.java`
- Create: `datacraft-server/datacraft-auth/src/main/java/io/datacraft/auth/infrastructure/persistence/JdbcMenuRepository.java`
- Create: `datacraft-server/datacraft-auth/src/test/java/io/datacraft/auth/infrastructure/persistence/RepositoryMappingTest.java`

- [ ] **Step 1: Write the failing persistence mapping test**

  Add a test that asserts the domain mapper rejects a disabled user for authentication and that a menu tree parent is retained when child rows are mapped. Use the repository/domain conversion API, not a controller or mapper directly.

- [ ] **Step 2: Run the test to verify it fails**

  Run `mvn -pl datacraft-server/datacraft-auth -am -Dtest=RepositoryMappingTest test`. Expect compilation failure because the domain and repository types are missing.

- [ ] **Step 3: Implement schema, seeds, entities, mappers, and adapters**

  Create PostgreSQL tables `dc_user`, `dc_role`, `dc_user_role`, `dc_menu`, and `dc_role_menu` with unique codes/usernames, foreign keys, enabled flags, and audit timestamps. Seed the `ADMIN` role and the seven Phase 1 menu codes using `ON CONFLICT DO NOTHING`, then grant all seeds to `ADMIN`. Use MyBatis-Plus `BaseMapper` interfaces and one explicit role-menu tree query. Repository adapters convert entities to domain records and expose `findByUsername`, `insert`, `findRoleCodes`, and `findMenusForRoles` without leaking entities.

- [ ] **Step 4: Run the focused test and migration resource check**

  Run `mvn -pl datacraft-server/datacraft-auth -am -Dtest=RepositoryMappingTest test` and `mvn -pl datacraft-server/datacraft-bootstrap -am -DskipTests process-resources`. Expect the test to pass and the migration to appear under the auth module’s classpath.

- [ ] **Step 5: Commit the persistence change**

  Commit the intended auth schema/domain/persistence files as `feat(auth): add user role and menu persistence`.

### Task 3: Implement JWT and authentication application services

**Files:**
- Create: `datacraft-server/datacraft-auth/src/main/java/io/datacraft/auth/application/AuthApplicationService.java`
- Create: `datacraft-server/datacraft-auth/src/main/java/io/datacraft/auth/application/MenuApplicationService.java`
- Create: `datacraft-server/datacraft-auth/src/main/java/io/datacraft/auth/security/JwtProperties.java`
- Create: `datacraft-server/datacraft-auth/src/main/java/io/datacraft/auth/security/JwtTokenService.java`
- Create: `datacraft-server/datacraft-auth/src/main/java/io/datacraft/auth/security/DataCraftPrincipal.java`
- Create: `datacraft-server/datacraft-auth/src/test/java/io/datacraft/auth/security/JwtTokenServiceTest.java`
- Create: `datacraft-server/datacraft-auth/src/test/java/io/datacraft/auth/application/AuthApplicationServiceTest.java`

- [ ] **Step 1: Write the failing JWT tests**

  Test that a token contains the user id/username/roles and round-trips before expiry, rejects a tampered signature, and rejects an expired token. Test login service behavior for a correct BCrypt password, a wrong password, and a disabled account. Use fake repository implementations and a real BCrypt encoder; mock only the mapper boundary if unavoidable.

- [ ] **Step 2: Run the tests to verify they fail**

  Run `mvn -pl datacraft-server/datacraft-auth -am -Dtest=JwtTokenServiceTest,AuthApplicationServiceTest test`. Expect compilation failure for the missing service classes.

- [ ] **Step 3: Implement minimal JWT and application behavior**

  Bind `DATACRAFT_JWT_SECRET` and `DATACRAFT_JWT_EXPIRATION_SECONDS` through configuration properties, enforce a minimum 256-bit secret, and use JJWT to sign HS256 tokens. `AuthApplicationService` loads an enabled user, uses `PasswordEncoder.matches`, gets role codes, signs the access token, and returns `LoginResponse`; all credential failures use one domain exception. `MenuApplicationService` filters enabled menus by role and builds a deterministic parent/child tree.

- [ ] **Step 4: Run the focused tests to verify green**

  Re-run the exact test command and require all JWT/application assertions to pass.

- [ ] **Step 5: Commit the service change**

  Commit as `feat(auth): implement jwt and login services`.

### Task 4: Add Spring Security, controllers, errors, and admin bootstrap

**Files:**
- Create: `datacraft-server/datacraft-auth/src/main/java/io/datacraft/auth/security/SecurityConfig.java`
- Create: `datacraft-server/datacraft-auth/src/main/java/io/datacraft/auth/security/JwtAuthenticationFilter.java`
- Create: `datacraft-server/datacraft-auth/src/main/java/io/datacraft/auth/web/AuthController.java`
- Create: `datacraft-server/datacraft-auth/src/main/java/io/datacraft/auth/web/SystemController.java`
- Create: `datacraft-server/datacraft-auth/src/main/java/io/datacraft/auth/web/ApiExceptionHandler.java`
- Create: `datacraft-server/datacraft-auth/src/main/java/io/datacraft/auth/application/AdminInitializer.java`
- Create: `datacraft-server/datacraft-auth/src/test/java/io/datacraft/auth/web/AuthControllerTest.java`
- Create: `datacraft-server/datacraft-auth/src/test/java/io/datacraft/auth/security/SecurityConfigTest.java`
- Modify: `datacraft-server/datacraft-bootstrap/src/main/resources/application.yml`
- Modify: `.env.example`

- [ ] **Step 1: Write failing web/security tests**

  Add MockMvc tests asserting login accepts valid JSON and returns the shared envelope, `/auth/me` and `/system/menus` reject anonymous requests with 401, valid Bearer tokens reach protected handlers, and invalid request bodies return 400. Add an initializer test asserting first-run insert and repeat-run idempotence.

- [ ] **Step 2: Run the tests to verify they fail**

  Run `mvn -pl datacraft-server/datacraft-auth -am -Dtest=AuthControllerTest,SecurityConfigTest test`. Expect missing controller/configuration failures.

- [ ] **Step 3: Implement the web and startup boundary**

  Configure stateless Spring Security, permit `/api/v1/auth/login` and health endpoints, install the JWT filter, and translate authentication/authorization exceptions to 401/403. Implement controllers that only call application services and return DTOs wrapped in `ApiResponse`. Implement a transactionally safe `ApplicationReadyEvent` initializer that validates `DATACRAFT_ADMIN_USERNAME` and `DATACRAFT_ADMIN_PASSWORD`, hashes the password, looks up `ADMIN`, and inserts only when absent. Add `DATACRAFT_JWT_SECRET`, expiry, and admin variables to `application.yml` and `.env.example` without putting a real secret in source control.

- [ ] **Step 4: Run the web tests and package the backend**

  Run the focused MockMvc tests, then `mvn -pl datacraft-server/datacraft-bootstrap -am test`. Require a zero exit code and no test failures.

- [ ] **Step 5: Commit the web/security change**

  Commit as `feat(auth): expose protected auth and menu api`.

### Task 5: Add frontend auth API, store, and guarded routing

**Files:**
- Create: `datacraft-web/src/api/http.ts`
- Create: `datacraft-web/src/api/auth.ts`
- Create: `datacraft-web/src/types/auth.ts`
- Create: `datacraft-web/src/stores/auth.ts`
- Modify: `datacraft-web/src/router/index.ts`
- Modify: `datacraft-web/src/main.ts`
- Create: `datacraft-web/src/stores/auth.test.ts`
- Modify: `datacraft-web/package.json`

- [ ] **Step 1: Add the frontend test runner and write failing store tests**

  Add Vitest as a dev dependency and a `test` script. Write tests for successful login persistence, logout clearing `localStorage`, and a failed `/me` restoration clearing a stale token. Mock only Axios at the HTTP boundary.

- [ ] **Step 2: Run the tests to verify they fail**

  Run `npm install` followed by `npm run test -- --run src/stores/auth.test.ts`. Expect failures because the API/store modules do not exist.

- [ ] **Step 3: Implement API types, Axios interceptors, Pinia store, and route guard**

  Define DTO types matching the backend envelope, create an Axios instance using `VITE_API_BASE_URL`, inject the Bearer token, and emit a single 401 cleanup path. Implement the store’s `login`, `restoreSession`, and `logout` actions with a namespaced local-storage key. Add public `/login` and protected `/` routes; the guard restores the session once before deciding whether to redirect.

- [ ] **Step 4: Run the store tests and typecheck**

  Run `npm run test -- --run src/stores/auth.test.ts` and `npm run build`; require all tests and `vue-tsc` to pass.

- [ ] **Step 5: Commit the frontend auth foundation**

  Commit as `feat(web): add jwt auth store and route guard`.

### Task 6: Implement the console-first layout and Phase 1 views

**Files:**
- Create: `datacraft-web/src/layouts/AppLayout.vue`
- Create: `datacraft-web/src/views/LoginView.vue`
- Create: `datacraft-web/src/views/HomeView.vue`
- Create: `datacraft-web/src/views/PlaceholderView.vue`
- Modify: `datacraft-web/src/App.vue`
- Modify: `datacraft-web/src/styles/main.scss`
- Modify: `datacraft-web/src/router/index.ts`

- [ ] **Step 1: Write a failing view smoke test**

  Add a Vitest component smoke test for the login view that renders the username/password fields and login button, and a layout test that renders the menu labels returned by the store. Run it and confirm it fails because the views are absent.

- [ ] **Step 2: Implement the approved A layout**

  Build the compact login form with loading and unified error states, then build `AppLayout` with a dark sidebar, backend menu tree, active route styling, user menu, logout action, responsive collapse, and `RouterView`. Build `HomeView` with three summary cards and a recent-execution panel using static Phase 1-safe data. Route future menu paths to `PlaceholderView` with a clear “not available in this phase” message.

- [ ] **Step 3: Run frontend tests and build**

  Run `npm run test -- --run` and `npm run build`; fix type errors, unused imports, and route/store integration failures.

- [ ] **Step 4: Commit the layout change**

  Commit as `feat(web): add console-first phase one layout`.

### Task 7: Update project configuration, progress, and end-to-end verification

**Files:**
- Modify: `DEV_PROGRESS.md`
- Modify: `README.md`
- Modify: `.env.example`
- Create: `docs/adr/0006-phase1-auth-system.md`

- [ ] **Step 1: Document the runnable configuration**

  Add the admin/JWT environment variables and startup sequence to the README, record the PostgreSQL platform-library decision and auth module boundary in the ADR, and update `.env.example` with safe placeholders only.

- [ ] **Step 2: Update progress from Phase 0 to Phase 1**

  Mark Phase 1 Auth & System complete only after the full verification commands pass. List the implemented auth/menu/layout pieces under Completed, set Next to Phase 2 Datasource, and record any environment-only limitation under Risks rather than claiming unsupported integration.

- [ ] **Step 3: Run the complete verification set**

  Start infrastructure with `docker compose up -d`, run `mvn test`, `mvn package`, `cd datacraft-web; npm run test -- --run`, and `npm run build`. Inspect the exit code and failure count for every command. If Docker is unavailable, report that limitation separately and still run all repository-local tests/builds.

- [ ] **Step 4: Perform a manual API/UI smoke check when infrastructure is available**

  Start the backend with `mvn spring-boot:run -pl datacraft-server/datacraft-bootstrap -am`, open the frontend, log in with the configured admin environment variables, refresh, open a placeholder menu, and log out. Confirm no password or token appears in backend logs.

- [ ] **Step 5: Final diff and progress review**

  Run `git status --short`, inspect only intended Phase 1 changes, and verify that no datasource, metadata, pipeline, engine, scheduler, or quality implementation slipped into the diff.
