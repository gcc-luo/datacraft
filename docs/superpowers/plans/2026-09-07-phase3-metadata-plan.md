# Phase 3 Metadata Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task with verification checkpoints.

**Goal:** Build PostgreSQL/MySQL metadata collection and the /assets data-asset browsing page for Schema, Table, and Field metadata.

**Architecture:** Add a standalone datacraft-metadata module. A MetadataCollector port is implemented by PostgreSQL and MySQL JDBC collectors; the application service owns datasource lookup, short-lived password decryption, transactional persistence, and DTO mapping. The Vue page uses the existing Axios/router/console conventions and renders the approved breadcrumb-plus-columns layout.

**Tech Stack:** Java 21, Spring Boot 3, MyBatis-Plus, PostgreSQL/Flyway, JDBC DatabaseMetaData, JUnit 5/Mockito, Vue 3, TypeScript, Vitest.

---

### Task 1: Add API contracts, metadata module, and migration contract tests

**Files:**
- Modify: datacraft-server/pom.xml
- Create: datacraft-server/datacraft-metadata/pom.xml
- Modify: datacraft-server/datacraft-bootstrap/pom.xml
- Create: datacraft-server/datacraft-api/src/main/java/io/datacraft/api/metadata/MetadataSyncResponse.java
- Create: datacraft-server/datacraft-api/src/main/java/io/datacraft/api/metadata/DatasetResponse.java
- Create: datacraft-server/datacraft-api/src/main/java/io/datacraft/api/metadata/DatasetFieldResponse.java
- Create: datacraft-server/datacraft-api/src/main/java/io/datacraft/api/metadata/DatasetDetailResponse.java
- Create: datacraft-server/datacraft-metadata/src/main/resources/db/migration/V3__create_metadata_tables.sql
- Test: datacraft-server/datacraft-metadata/src/test/java/io/datacraft/metadata/infrastructure/MetadataMigrationTest.java

- [ ] Step 1: Write a failing migration/DTO test. Assert V3 defines dc_dataset, dc_dataset_field, datasource_id, dataset_id, both uniqueness constraints, cascade foreign keys, audit timestamps, and no password column. Assert the API records can represent sync counts and an ordered detail field list.
- [ ] Step 2: Run mvn -pl datacraft-server/datacraft-metadata -am -Dtest=MetadataMigrationTest test. Expected: RED because the module, migration, and DTO classes do not exist.
- [ ] Step 3: Add the module and API records. The records must have these shapes:

    public record MetadataSyncResponse(Long datasourceId, int schemaCount, int datasetCount, int fieldCount, Instant collectedAt) {}

    public record DatasetResponse(Long id, Long datasourceId, String catalogName, String schemaName, String tableName, String tableRemark, Long estimatedRowCount, Instant collectedAt) {}

    public record DatasetFieldResponse(Long id, String fieldName, int ordinalPosition, String dataType, boolean nullable, boolean primaryKey, String fieldRemark) {}

    public record DatasetDetailResponse(Long id, Long datasourceId, String catalogName, String schemaName, String tableName, String tableRemark, Long estimatedRowCount, Instant collectedAt, List<DatasetFieldResponse> fields) {}

  Add the module before bootstrap in the server reactor. Depend on api, common, datasource, Spring JDBC/Web/Validation, MyBatis-Plus, both runtime drivers, and test/security-test dependencies.
- [ ] Step 4: Add V3 with dc_dataset and dc_dataset_field, FK cascades, unique keys, audit timestamps, and indexes. Run the focused test again. Expected: PASS.

---

### Task 2: Define metadata domain snapshots and JDBC collector tests

**Files:**
- Create: datacraft-server/datacraft-metadata/src/main/java/io/datacraft/metadata/domain/Dataset.java
- Create: datacraft-server/datacraft-metadata/src/main/java/io/datacraft/metadata/domain/DatasetField.java
- Create: datacraft-server/datacraft-metadata/src/main/java/io/datacraft/metadata/domain/MetadataSnapshot.java
- Create: datacraft-server/datacraft-metadata/src/main/java/io/datacraft/metadata/domain/MetadataCollector.java
- Create: datacraft-server/datacraft-metadata/src/main/java/io/datacraft/metadata/application/JdbcMetadataSupport.java
- Create: datacraft-server/datacraft-metadata/src/main/java/io/datacraft/metadata/application/PostgresqlMetadataCollector.java
- Create: datacraft-server/datacraft-metadata/src/main/java/io/datacraft/metadata/application/MysqlMetadataCollector.java
- Test: datacraft-server/datacraft-metadata/src/test/java/io/datacraft/metadata/application/PostgresqlMetadataCollectorTest.java
- Test: datacraft-server/datacraft-metadata/src/test/java/io/datacraft/metadata/application/MysqlMetadataCollectorTest.java

- [ ] Step 1: Write failing collector tests with mocked Connection, DatabaseMetaData, ResultSet, and PreparedStatement. Cover PostgreSQL system-schema filtering, MySQL configured-database filtering, tables/columns/primary keys/remarks/ordinal positions/types/nullability, vendor-specific row-estimate SQL, isolated estimate failure as null, and closure of JDBC resources.
- [ ] Step 2: Run mvn -pl datacraft-server/datacraft-metadata -am -Dtest=PostgresqlMetadataCollectorTest,MysqlMetadataCollectorTest test. Expected: RED because the domain and collector classes do not exist.
- [ ] Step 3: Add immutable Dataset, DatasetField, and MetadataSnapshot types. Do not add a plaintext password field. Define:

    public interface MetadataCollector {
        DatasourceType supports();
        MetadataSnapshot collect(Datasource datasource, String password);
    }

- [ ] Step 4: Implement JDBC collection using the datasource module's JdbcUrlBuilder and DriverManager. Use DatabaseMetaData.getTables, getColumns, and getPrimaryKeys for structure. Use PostgreSQL pg_namespace/pg_class/reltuples and MySQL information_schema.tables/table_rows for estimates. Filter to TABLE and exclude system schemas. Use try-with-resources and convert estimate failures to null without dropping the table. Do not put URL, username, password, or raw exception text in user-facing messages.
- [ ] Step 5: Run the focused collector command again. Expected: all collector tests PASS.

---

### Task 3: Add persistence entities, repository, and application-service tests

**Files:**
- Create: datacraft-server/datacraft-metadata/src/main/java/io/datacraft/metadata/infrastructure/MybatisMetadataConfiguration.java
- Create: datacraft-server/datacraft-metadata/src/main/java/io/datacraft/metadata/infrastructure/persistence/entity/DatasetEntity.java
- Create: datacraft-server/datacraft-metadata/src/main/java/io/datacraft/metadata/infrastructure/persistence/entity/DatasetFieldEntity.java
- Create: datacraft-server/datacraft-metadata/src/main/java/io/datacraft/metadata/infrastructure/persistence/mapper/DatasetMapper.java
- Create: datacraft-server/datacraft-metadata/src/main/java/io/datacraft/metadata/infrastructure/persistence/mapper/DatasetFieldMapper.java
- Create: datacraft-server/datacraft-metadata/src/main/java/io/datacraft/metadata/infrastructure/persistence/DatasetRepository.java
- Create: datacraft-server/datacraft-metadata/src/main/java/io/datacraft/metadata/application/DatasetNotFoundException.java
- Create: datacraft-server/datacraft-metadata/src/main/java/io/datacraft/metadata/application/MetadataApplicationService.java
- Test: datacraft-server/datacraft-metadata/src/test/java/io/datacraft/metadata/infrastructure/MetadataRepositoryMappingTest.java
- Test: datacraft-server/datacraft-metadata/src/test/java/io/datacraft/metadata/application/MetadataApplicationServiceTest.java

- [ ] Step 1: Write failing tests for entity column mapping, sync collector selection, ciphertext decryption, snapshot replacement, accurate counts, missing datasource, list filters, detail ordering, and missing dataset.
- [ ] Step 2: Run mvn -pl datacraft-server/datacraft-metadata -am -Dtest=MetadataRepositoryMappingTest,MetadataApplicationServiceTest test. Expected: RED because the entities, repository, and service do not exist.
- [ ] Step 3: Add MyBatis-Plus entities and mappers. Keep explicit domain/entity mapping. Configure mapper scanning only under the metadata module.
- [ ] Step 4: Implement DatasetRepository with:

    List<Dataset> findAll(Long datasourceId, String schemaName, String keyword);
    Optional<Dataset> findById(Long id);
    void replaceDatasourceSnapshot(Long datasourceId, List<Dataset> datasets);

  Implement replaceDatasourceSnapshot transactionally by deleting current datasource datasets, relying on FK cascade for fields, then inserting the collected snapshot. This is idempotent and removes tables no longer present.
- [ ] Step 5: Implement MetadataApplicationService. Inject DatasourceRepository, SecretCryptoService, DatasetRepository, and collectors. Resolve by DatasourceType, decrypt only inside sync, collect, persist, and map DTOs. Expose the fixed safe error 元数据同步失败，请检查数据源配置与网络 for connection failures.
- [ ] Step 6: Run the focused tests and then mvn -pl datacraft-server/datacraft-metadata -am test. Expected: PASS.

---

### Task 4: Add metadata REST API and web-layer tests

**Files:**
- Create: datacraft-server/datacraft-metadata/src/main/java/io/datacraft/metadata/web/MetadataController.java
- Create: datacraft-server/datacraft-metadata/src/main/java/io/datacraft/metadata/web/MetadataExceptionHandler.java
- Test: datacraft-server/datacraft-metadata/src/test/java/io/datacraft/metadata/web/MetadataControllerTest.java

- [ ] Step 1: Write failing MVC tests for anonymous rejection, authenticated sync counts, query forwarding, ordered detail fields, 404 mapping, and JSON absence of password/password_ciphertext.
- [ ] Step 2: Run mvn -pl datacraft-server/datacraft-metadata -am -Dtest=MetadataControllerTest test. Expected: RED because the controller and handler do not exist.
- [ ] Step 3: Expose:
    POST /api/v1/datasources/{datasourceId}/metadata/sync
    GET /api/v1/datasets?datasourceId=&schemaName=&keyword=
    GET /api/v1/datasets/{id}

  Keep the controller dependent only on MetadataApplicationService. Map DatasetNotFoundException to HTTP 404 with DATASET_NOT_FOUND and collection failures to a safe business error.
- [ ] Step 4: Run the focused MVC command and the full metadata module suite. Expected: PASS.

---

### Task 5: Implement the /assets frontend page and tests

**Files:**
- Create: datacraft-web/src/types/metadata.ts
- Create: datacraft-web/src/api/metadata.ts
- Create: datacraft-web/src/views/AssetListView.vue
- Modify: datacraft-web/src/router/index.ts
- Modify: datacraft-web/src/styles/main.scss
- Test: datacraft-web/src/views/AssetListView.test.ts

- [ ] Step 1: Write failing component tests for datasource loading, empty guidance, asset list loading, Schema grouping, table selection, breadcrumb/table details/fields, keyword filtering, sync loading/success refresh, and safe failure messages.
- [ ] Step 2: Run npm run test -- --run src/views/AssetListView.test.ts. Expected: RED because the API/types/view do not exist.
- [ ] Step 3: Define types for the four API records. Add listDatasources, listDatasets, getDataset, and syncDatasourceMetadata using the existing HTTP response unwrapping convention. Never store or display datasource passwords.
- [ ] Step 4: Add /assets before the catch-all route. Implement datasource selector, schema-grouped left table list, keyword filter, breadcrumb/right detail panel, ordered field table, loading/empty/no-datasource/sync-success/safe-error states, and auto-selection of the first dataset after sync. Reuse console-first classes and add focused asset styles only.
- [ ] Step 5: Run npm run test -- --run and npm run build. Expected: all frontend tests pass and Vite production build succeeds.

---

### Task 6: Update progress, README, and ADR

**Files:**
- Modify: DEV_PROGRESS.md
- Modify: README.md
- Create: docs/adr/0008-phase3-metadata.md

- [ ] Step 1: Before editing, verify the three metadata endpoints, V3 migration, and /assets route exist; verify no Pipeline or Engine implementation files were added.
- [ ] Step 2: Mark Phase 3 completed, list module/migration/collector/dataset-field/API/UI/tests under Completed, and set Next to Phase 4 — Pipeline Model. Update README module structure, metadata API examples, current boundary, and add ADR 0008 for hybrid collection, snapshot replacement, and credential handling.
- [ ] Step 3: Run:

    rg -n "Phase 3|Metadata|dc_dataset|/assets|Phase 4" DEV_PROGRESS.md README.md docs/adr/0008-phase3-metadata.md
    git diff --check

  Expected: required statements are present and diff check is clean.

---

### Task 7: Full verification and handoff

**Files:**
- No new production files; inspect all Phase 3 changes.

- [ ] Step 1: Run official Java 21 verification:

    mvn clean test
    mvn -DskipTests package

  Expected: success with zero failures. If Java 21 is unavailable, report the exact Enforcer failure and do not claim official Java 21 verification.
- [ ] Step 2: When Java 21 is unavailable, run supplemental compatibility verification with installed JDK 17 only:

    mvn clean -Denforcer.skip=true -Dmaven.compiler.release=17 test
    mvn -Denforcer.skip=true -Dmaven.compiler.release=17 -DskipTests package

  Report this separately from official Java 21 verification.
- [ ] Step 3: Run frontend npm run test -- --run and npm run build.
- [ ] Step 4: Run rg --files datacraft-server datacraft-web | rg "metadata|Asset|Pipeline|Engine", git diff --check, and git status --short. Confirm only Phase 3 files and intended docs changed; do not add secrets/generated output.
- [ ] Step 5: Report exact results, endpoints, acceptance flow, out-of-scope features, Java availability, and remaining risks.
