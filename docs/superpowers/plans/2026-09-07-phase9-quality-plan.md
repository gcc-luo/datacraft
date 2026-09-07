# Phase 9 Quality 实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 为 DataCraft 增加可鉴权、可持久化、方言安全的数据库质量检查能力。

**架构：** 新增 `datacraft-quality` 业务模块，通过 Datasource 的 `JdbcConnectionProvider` 访问数据库；质量规则由 Provider/Registry 负责编译，应用服务负责执行和持久化，Pipeline 模块只保留质量节点元数据。

**技术栈：** Java 21、Spring Boot 3、JDBC、MyBatis-Plus、PostgreSQL、Flyway、JUnit 5、Spring MVC Test。

---

### 任务 1：创建质量模块和 API 契约

**文件：**
- 修改：`datacraft-server/pom.xml`
- 创建：`datacraft-server/datacraft-quality/pom.xml`
- 修改：`datacraft-server/datacraft-bootstrap/pom.xml`
- 创建：`datacraft-server/datacraft-api/src/main/java/io/datacraft/api/quality/QualityCheckRequest.java`
- 创建：`datacraft-server/datacraft-api/src/main/java/io/datacraft/api/quality/QualityRuleRequest.java`
- 创建：`datacraft-server/datacraft-api/src/main/java/io/datacraft/api/quality/QualityResultResponse.java`
- 创建：`datacraft-server/datacraft-api/src/main/java/io/datacraft/api/quality/QualitySampleResponse.java`

- [x] 编写 API record，规则类型、数据源、表名和可选执行上下文字段保持不可变。
- [x] 将 quality 模块放在 datasource 之后、bootstrap 之前，依赖 `datacraft-api`、`datacraft-common`、`datacraft-datasource` 和 Web/JDBC/MyBatis 测试依赖。
- [x] 运行 `mvn '-Denforcer.skip=true' '-Dmaven.compiler.release=17' -pl datacraft-server/datacraft-quality -am test`，模块可编译。

### 任务 2：实现规则 Provider、SQL 方言和安全校验

**文件：**
- 创建：`datacraft-server/datacraft-quality/src/main/java/io/datacraft/quality/domain/QualityRuleProvider.java`
- 创建：`datacraft-server/datacraft-quality/src/main/java/io/datacraft/quality/domain/QualityRuleType.java`
- 创建：`datacraft-server/datacraft-quality/src/main/java/io/datacraft/quality/domain/SqlDialect.java`
- 创建：`datacraft-server/datacraft-quality/src/main/java/io/datacraft/quality/application/QualityRuleRegistry.java`
- 创建：`datacraft-server/datacraft-quality/src/main/java/io/datacraft/quality/application/SqlSafetyValidator.java`
- 创建：`datacraft-server/datacraft-quality/src/main/java/io/datacraft/quality/application/MysqlSqlDialect.java`
- 创建：`datacraft-server/datacraft-quality/src/main/java/io/datacraft/quality/application/PostgresqlSqlDialect.java`
- 测试：`datacraft-server/datacraft-quality/src/test/java/io/datacraft/quality/application/SqlSafetyValidatorTest.java`
- 测试：`datacraft-server/datacraft-quality/src/test/java/io/datacraft/quality/application/QualityRuleRegistryTest.java`

- [x] 先写规则注册、七类规则集合和不安全 SQL 拒绝测试，并完成红测试验证。
- [x] 为 MySQL 使用反引号、PostgreSQL 使用双引号，生成 `COUNT(*)`、条件计数和样例查询。
- [x] 对自定义条件拒绝分号、注释以及 `SELECT/INSERT/UPDATE/DELETE/DDL` 关键字。
- [x] 注册未知规则时返回稳定的质量规则异常。

### 任务 3：持久化结果与样例

**文件：**
- 创建：`datacraft-server/datacraft-quality/src/main/resources/db/migration/V5__create_quality_tables.sql`
- 创建：`datacraft-server/datacraft-quality/src/main/java/io/datacraft/quality/domain/QualityResult.java`
- 创建：`datacraft-server/datacraft-quality/src/main/java/io/datacraft/quality/domain/QualitySample.java`
- 创建：`datacraft-server/datacraft-quality/src/main/java/io/datacraft/quality/infrastructure/persistence/entity/QualityResultEntity.java`
- 创建：`datacraft-server/datacraft-quality/src/main/java/io/datacraft/quality/infrastructure/persistence/entity/QualitySampleEntity.java`
- 创建：`datacraft-server/datacraft-quality/src/main/java/io/datacraft/quality/infrastructure/persistence/mapper/QualityResultMapper.java`
- 创建：`datacraft-server/datacraft-quality/src/main/java/io/datacraft/quality/infrastructure/persistence/mapper/QualitySampleMapper.java`
- 创建：`datacraft-server/datacraft-quality/src/main/java/io/datacraft/quality/infrastructure/persistence/QualityRepository.java`
- 测试：`datacraft-server/datacraft-quality/src/test/java/io/datacraft/quality/infrastructure/QualityMigrationTest.java`

- [x] 建立 `dc_quality_result`、`dc_quality_sample` 表和执行/结果索引，样例数量由应用层限制为 1000。
- [x] Repository 只负责实体映射和保存/查询，不让 Controller 直接访问 Mapper。
- [x] 为迁移版本、级联删除和 JSONB 样例字段增加测试。

### 任务 4：实现 JDBC 质量检查和 API

**文件：**
- 创建：`datacraft-server/datacraft-quality/src/main/java/io/datacraft/quality/application/QualityApplicationService.java`
- 创建：`datacraft-server/datacraft-quality/src/main/java/io/datacraft/quality/web/QualityController.java`
- 创建：`datacraft-server/datacraft-quality/src/main/java/io/datacraft/quality/web/QualityExceptionHandler.java`
- 测试：`datacraft-server/datacraft-quality/src/test/java/io/datacraft/quality/application/QualityApplicationServiceTest.java`
- 测试：`datacraft-server/datacraft-quality/src/test/java/io/datacraft/quality/web/QualityControllerTest.java`

- [x] 先写 JDBC mock 测试：空值规则能执行聚合查询、返回通过率并保存样例。
- [x] 对每条规则按 datasource/table/field 生成查询，使用 PreparedStatement 绑定数值和枚举参数。
- [x] 样例使用 `ResultSetMetaData` 形成有限 JSON 对象，最多保存 1000 条。
- [x] 增加结果列表和详情查询，未知 ID 返回质量结果不存在错误。
- [x] 对输入执行校验，API 只调用 application service。

### 任务 5：注册质量节点并更新进度

**文件：**
- 修改：`datacraft-server/datacraft-pipeline/src/main/java/io/datacraft/pipeline/application/NodeRegistry.java`
- 修改：`datacraft-server/datacraft-pipeline/src/test/java/io/datacraft/pipeline/application/NodeRegistryTest.java`
- 修改：`DEV_PROGRESS.md`

- [x] 增加七类质量节点元数据，统一声明 `NATIVE` 支持和规则配置字段。
- [x] 确认既有 Planner 对质量节点不会绕过 Engine Registry。
- [x] 将 Phase 9 标记为完成，Next 切换为 Phase 10 Scheduler。

### 任务 6：最终验证和提交

- [x] 运行 `$env:JAVA_HOME='D:\Java\jdk-17.0.15+6'; mvn '-Denforcer.skip=true' '-Dmaven.compiler.release=17' test`。
- [x] 运行 `$env:JAVA_HOME='D:\Java\jdk-17.0.15+6'; mvn '-Denforcer.skip=true' '-Dmaven.compiler.release=17' package`。
- [x] 运行 `npm test -- --run` 和 `npm run build`（工作目录 `datacraft-web`）。
- [x] 运行 `git diff --check`，确认没有凭据、后续引擎依赖或未跟踪源文件。
- [ ] 使用中文 Conventional Commit 提交原子变更。
