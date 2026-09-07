# Phase 2 Datasource Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task with verification checkpoints.

**Goal:** Build the Phase 2 JDBC datasource CRUD, connection testing, encrypted credentials, and console UI without implementing metadata or pipeline features.

**Architecture:** Add a standalone `datacraft-datasource` backend module. Controllers call an application service, which uses a domain repository, AES-GCM secret service, and JDBC connection tester; MyBatis entities never cross the API boundary. Add a Vue datasource list/form view using the existing Pinia/Axios/router conventions.

**Tech Stack:** Java 21, Spring Boot 3, MyBatis-Plus, PostgreSQL/Flyway, JDBC, AES-256-GCM, JUnit 5/Mockito, Vue 3, TypeScript, Pinia, Axios, Vitest.

---

### Task 1: Module, API contracts, and configuration

**Files:**
- Modify: `datacraft-server/pom.xml`
- Create: `datacraft-server/datacraft-datasource/pom.xml`
- Modify: `datacraft-server/datacraft-bootstrap/pom.xml`
- Create: `datacraft-server/datacraft-datasource/src/main/java/io/datacraft/datasource/config/DatasourceEncryptionProperties.java`
- Create: `datacraft-server/datacraft-datasource/src/main/java/io/datacraft/datasource/config/DatasourceConfiguration.java`
- Create: `datacraft-server/datacraft-api/src/main/java/io/datacraft/api/datasource/DatasourceType.java`
- Create: `datacraft-server/datacraft-api/src/main/java/io/datacraft/api/datasource/DatasourceStatus.java`
- Create: `datacraft-server/datacraft-api/src/main/java/io/datacraft/api/datasource/DatasourceRequest.java`
- Create: `datacraft-server/datacraft-api/src/main/java/io/datacraft/api/datasource/DatasourceResponse.java`
- Create: `datacraft-server/datacraft-api/src/main/java/io/datacraft/api/datasource/DatasourceTestResponse.java`
- Test: `datacraft-server/datacraft-datasource/src/test/java/io/datacraft/datasource/config/DatasourceEncryptionPropertiesTest.java`

- [ ] **Step 1: Write the failing configuration test**

Assert that a decoded Base64 key of exactly 32 bytes is accepted and a missing/short key throws `IllegalStateException` when the configuration bean is created.

- [ ] **Step 2: Run the focused test to verify it fails**

Run `mvn -pl datacraft-server/datacraft-datasource -am -Dtest=DatasourceEncryptionPropertiesTest test` with Java 21. Expected: compilation failure because the module and properties class do not exist.

- [ ] **Step 3: Add the module and minimal contracts**

Create the module with dependencies on `datacraft-api`, `datacraft-common`, Spring JDBC, Spring Web/Validation, MyBatis-Plus, PostgreSQL runtime, MySQL runtime, and test dependencies. Add it before bootstrap in the server aggregator and add the bootstrap dependency. Define enums, records, and `@ConfigurationProperties(prefix = "datacraft.security.datasource")` with strict 32-byte Base64 validation in its constructor/initialization.

- [ ] **Step 4: Run the focused test to verify it passes**

Run the same Maven command and expect PASS.

### Task 2: Encryption and datasource domain

**Files:**
- Create: `datacraft-server/datacraft-datasource/src/main/java/io/datacraft/datasource/security/SecretCryptoService.java`
- Create: `datacraft-server/datacraft-datasource/src/main/java/io/datacraft/datasource/domain/Datasource.java`
- Create: `datacraft-server/datacraft-datasource/src/main/java/io/datacraft/datasource/domain/DatasourceRepository.java`
- Create: `datacraft-server/datacraft-datasource/src/main/java/io/datacraft/datasource/domain/DatasourceConnectionTester.java`
- Create: `datacraft-server/datacraft-datasource/src/main/java/io/datacraft/datasource/domain/ConnectionTestOutcome.java`
- Test: `datacraft-server/datacraft-datasource/src/test/java/io/datacraft/datasource/security/SecretCryptoServiceTest.java`
- Test: `datacraft-server/datacraft-datasource/src/test/java/io/datacraft/datasource/domain/DatasourceTest.java`

- [ ] **Step 1: Write failing crypto and domain tests**

Cover round-trip encryption, randomized ciphertext for the same plaintext, rejection of tampered ciphertext, and a domain object that exposes only ciphertext (never a plaintext password field).

- [ ] **Step 2: Run focused tests and confirm RED**

Run `mvn -pl datacraft-server/datacraft-datasource -am -Dtest=SecretCryptoServiceTest,DatasourceTest test`. Expected: compilation failure for missing classes.

- [ ] **Step 3: Implement minimal AES-GCM and domain contracts**

Use `Cipher.getInstance("AES/GCM/NoPadding")`, a fresh 12-byte nonce from `SecureRandom`, 128-bit authentication tag, and `v1:` plus Base64 of nonce+ciphertext. Throw `IllegalArgumentException` for malformed/tampered values. Define immutable domain state with id, name, type, host, port, databaseName, username, passwordCiphertext, remark, status, last-tested fields, and audit timestamps.

- [ ] **Step 4: Run focused tests and confirm GREEN**

Run the same command and expect all crypto/domain tests PASS.

### Task 3: Persistence schema and repository

**Files:**
- Create: `datacraft-server/datacraft-datasource/src/main/resources/db/migration/V2__create_datasource_table.sql`
- Create: `datacraft-server/datacraft-datasource/src/main/java/io/datacraft/datasource/infrastructure/MybatisDatasourceConfiguration.java`
- Create: `datacraft-server/datacraft-datasource/src/main/java/io/datacraft/datasource/infrastructure/persistence/entity/DatasourceEntity.java`
- Create: `datacraft-server/datacraft-datasource/src/main/java/io/datacraft/datasource/infrastructure/persistence/mapper/DatasourceMapper.java`
- Create: `datacraft-server/datacraft-datasource/src/main/java/io/datacraft/datasource/infrastructure/persistence/JdbcDatasourceRepository.java`
- Test: `datacraft-server/datacraft-datasource/src/test/java/io/datacraft/datasource/infrastructure/persistence/DatasourceRepositoryMappingTest.java`

- [ ] **Step 1: Write the failing mapping test**

Assert the entity is mapped to `dc_datasource` and its `passwordCiphertext` property is mapped to `password_ciphertext`; assert the migration contains the encrypted password column, type/status checks, and no plaintext `password` column.

- [ ] **Step 2: Run the mapping test and confirm RED**

Run `mvn -pl datacraft-server/datacraft-datasource -am -Dtest=DatasourceRepositoryMappingTest test`. Expected: missing entity/migration failure.

- [ ] **Step 3: Implement migration and repository adapter**

Create `dc_datasource` with unique name, structured JDBC fields, `password_ciphertext TEXT NOT NULL`, `status`, `last_tested_at`, `last_test_latency_ms`, `last_test_message`, audit timestamps, and indexes. Implement MyBatis-Plus CRUD/query methods and explicit entity/domain mapping. Configure mapper scanning only for the datasource package.

- [ ] **Step 4: Run mapping and existing backend tests**

Run the focused test, then `mvn -pl datacraft-server/datacraft-datasource -am test`; expect PASS.

### Task 4: JDBC URL builder, connection tester, and application service

**Files:**
- Create: `datacraft-server/datacraft-datasource/src/main/java/io/datacraft/datasource/application/JdbcUrlBuilder.java`
- Create: `datacraft-server/datacraft-datasource/src/main/java/io/datacraft/datasource/application/JdbcConnectionTester.java`
- Create: `datacraft-server/datacraft-datasource/src/main/java/io/datacraft/datasource/application/DatasourceApplicationService.java`
- Create: `datacraft-server/datacraft-datasource/src/main/java/io/datacraft/datasource/application/DatasourceNotFoundException.java`
- Create: `datacraft-server/datacraft-datasource/src/main/java/io/datacraft/datasource/application/DatasourceDuplicateException.java`
- Create: `datacraft-server/datacraft-datasource/src/main/java/io/datacraft/datasource/application/DatasourceValidationException.java`
- Test: `datacraft-server/datacraft-datasource/src/test/java/io/datacraft/datasource/application/JdbcUrlBuilderTest.java`
- Test: `datacraft-server/datacraft-datasource/src/test/java/io/datacraft/datasource/application/DatasourceApplicationServiceTest.java`

- [ ] **Step 1: Write failing URL/service tests**

Cover PostgreSQL/MySQL URL generation with bounded connect timeouts, create encryption before repository save, duplicate-name rejection, update-with-empty-password preserving the existing ciphertext, test-connection decryption and status persistence, and not-found errors.

- [ ] **Step 2: Run focused tests and confirm RED**

Run `mvn -pl datacraft-server/datacraft-datasource -am -Dtest=JdbcUrlBuilderTest,DatasourceApplicationServiceTest test`; expected: missing implementation failure.

- [ ] **Step 3: Implement minimal service behavior**

Generate only known JDBC schemes. Use `try (Connection connection = DriverManager.getConnection(...))` in the tester, measure elapsed milliseconds, catch `SQLException` into a safe failure outcome, and never include the JDBC URL or exception text in the API outcome. The application service maps request DTOs to domain state, encrypts create/update passwords, checks unique names, and maps domain state to password-free responses.

- [ ] **Step 4: Run focused and module tests**

Run the focused command and then the full datasource module test suite; expect PASS.

### Task 5: REST API and bootstrap configuration

**Files:**
- Create: `datacraft-server/datacraft-datasource/src/main/java/io/datacraft/datasource/web/DatasourceController.java`
- Create: `datacraft-server/datacraft-datasource/src/main/java/io/datacraft/datasource/web/DatasourceExceptionHandler.java`
- Modify: `datacraft-server/datacraft-bootstrap/src/main/resources/application.yml`
- Modify: `.env.example`
- Modify: `README.md`
- Create: `docs/adr/0007-phase2-datasource.md`
- Test: `datacraft-server/datacraft-datasource/src/test/java/io/datacraft/datasource/web/DatasourceControllerTest.java`

- [ ] **Step 1: Write failing MVC tests**

Cover authentication on the collection endpoint, validation failure for malformed create requests, password absence in successful responses, and 404/409 error envelopes.

- [ ] **Step 2: Run MVC tests and confirm RED**

Run `mvn -pl datacraft-server/datacraft-datasource -am -Dtest=DatasourceControllerTest test`; expected: missing controller/configuration failure.

- [ ] **Step 3: Implement controller, exception mapping, and environment docs**

Expose `GET/POST/GET by id/PUT/DELETE/POST test` under `/api/v1/datasources`. Use `@Valid`, authenticated requests, `DATACRAFT_DATASOURCE_ENCRYPTION_KEY` in application configuration and `.env.example`, and add the Phase 2 ADR/README instructions. Return only `ApiResponse` DTOs.

- [ ] **Step 4: Run MVC and full backend tests**

Run the focused MVC test, then Java 21 `mvn test` and `mvn -DskipTests package`; expect PASS.

### Task 6: Frontend datasource workflow

**Files:**
- Create: `datacraft-web/src/types/datasource.ts`
- Create: `datacraft-web/src/api/datasources.ts`
- Create: `datacraft-web/src/views/DatasourceListView.vue`
- Modify: `datacraft-web/src/router/index.ts`
- Modify: `datacraft-web/src/styles/main.scss`
- Test: `datacraft-web/src/views/DatasourceListView.test.ts`

- [ ] **Step 1: Write failing component tests**

Cover loading a list, rendering type/status without a password, creating a datasource with password, keeping an edit password blank by default, invoking connection test, and deleting after confirmation.

- [ ] **Step 2: Run Vitest and confirm RED**

Run `npm run test -- --run src/views/DatasourceListView.test.ts`; expected: missing view/API failure.

- [ ] **Step 3: Implement the UI and API client**

Add typed API methods, a single list/form view with create/edit state, inline test result, safe status rendering, delete confirmation, and a `/datasources` route before the existing placeholder catch-all. Reuse the Phase 1 console classes and do not store or render password values returned by the server.

- [ ] **Step 4: Run frontend tests and production build**

Run `npm run test -- --run` and `npm run build`; expect PASS.

### Task 7: Progress update and end-to-end verification

**Files:**
- Modify: `DEV_PROGRESS.md`

- [ ] **Step 1: Update progress only after tests are green**

Mark Phase 2 complete, list datasource migration/encryption/API/UI/tests under Completed, set Next to Phase 3 Metadata, and leave later phases untouched.

- [ ] **Step 2: Run repository hygiene checks**

Run `git diff --check` and inspect `git status --short`. Do not add unrelated baseline files or secrets.

- [ ] **Step 3: Run real Docker smoke test**

Start `docker compose up -d`, launch the Java 21 jar with admin/JWT/encryption environment variables, create a PostgreSQL datasource pointing at the Compose database, call list/test/update/read/delete, verify the response never contains `password` or `password_ciphertext`, then stop the jar and run `docker compose down`.

- [ ] **Step 4: Report verified completion and boundaries**

Report exact test/build results, the Phase 2 endpoints, required environment variable, and that Phase 3 Metadata remains not started.
