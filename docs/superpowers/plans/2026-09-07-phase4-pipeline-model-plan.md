# Phase 4 Pipeline Model 实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 建立 DataCraft Pipeline、Node、Edge 控制面模型、DAG 基础校验、节点元数据注册和 REST CRUD，为 Phase 5 Vue Flow 编辑器提供稳定后端能力。

**架构：** 新增独立 `datacraft-pipeline` 模块。PipelineApplicationService 负责请求校验、节点类型校验、版本递增、事务性图快照替换和 DTO 映射；PipelineRepository 负责三张表的持久化；NodeRegistry 只提供节点类型元数据，不依赖任何执行引擎。

**技术栈：** Java 21、Spring Boot 3、MyBatis-Plus、PostgreSQL、Flyway、JUnit 5、Mockito、Spring MVC Test。

---

### 任务 1：添加 Pipeline API 合约、模块和 V4 迁移

**文件：**
- 修改：`datacraft-server/pom.xml`
- 修改：`datacraft-server/datacraft-bootstrap/pom.xml`
- 创建：`datacraft-server/datacraft-pipeline/pom.xml`
- 创建：`datacraft-server/datacraft-api/src/main/java/io/datacraft/api/pipeline/PipelineStatus.java`
- 创建：`datacraft-server/datacraft-api/src/main/java/io/datacraft/api/pipeline/ExecutionStrategy.java`
- 创建：`datacraft-server/datacraft-api/src/main/java/io/datacraft/api/pipeline/PipelineRequest.java`
- 创建：`datacraft-server/datacraft-api/src/main/java/io/datacraft/api/pipeline/PipelineNodeRequest.java`
- 创建：`datacraft-server/datacraft-api/src/main/java/io/datacraft/api/pipeline/PipelineEdgeRequest.java`
- 创建：`datacraft-server/datacraft-api/src/main/java/io/datacraft/api/pipeline/PipelineResponse.java`
- 创建：`datacraft-server/datacraft-api/src/main/java/io/datacraft/api/pipeline/PipelineNodeResponse.java`
- 创建：`datacraft-server/datacraft-api/src/main/java/io/datacraft/api/pipeline/PipelineEdgeResponse.java`
- 创建：`datacraft-server/datacraft-api/src/main/java/io/datacraft/api/pipeline/PipelineDetailResponse.java`
- 创建：`datacraft-server/datacraft-api/src/main/java/io/datacraft/api/pipeline/NodeMetadataResponse.java`
- 创建：`datacraft-server/datacraft-pipeline/src/main/resources/db/migration/V4__create_pipeline_tables.sql`
- 测试：`datacraft-server/datacraft-pipeline/src/test/java/io/datacraft/pipeline/infrastructure/PipelineMigrationTest.java`

- [ ] **步骤 1：先写迁移和 API 合约失败测试**

测试读取 V4 文件并断言 `dc_pipeline`、`dc_pipeline_node`、`dc_pipeline_edge`、级联外键、Pipeline 内节点 key 唯一约束、审计字段和 `graph_json` 存在；同时构造带有节点数组和边数组的请求/详情对象，确保 API 合约可表达完整图快照。

- [ ] **步骤 2：运行测试确认失败**

运行：`mvn '-Denforcer.skip=true' '-Dmaven.compiler.release=17' '-Dtest=PipelineMigrationTest' '-Dsurefire.failIfNoSpecifiedTests=false' -pl datacraft-server/datacraft-pipeline -am test`

预期：失败，原因是 Pipeline 模块、迁移和 API 类型尚不存在。

- [ ] **步骤 3：添加模块、API records 和 V4 迁移**

API 使用以下固定形状：

```java
public record PipelineRequest(String name, String description, PipelineStatus status,
                              ExecutionStrategy executionStrategy,
                              List<PipelineNodeRequest> nodes, List<PipelineEdgeRequest> edges) {}
public record PipelineNodeRequest(String nodeKey, String nodeType, String nodeName,
                                  Double x, Double y, String configJson, String preferredEngine) {}
public record PipelineEdgeRequest(String sourceNodeKey, String targetNodeKey,
                                  String sourcePort, String targetPort, String conditionJson) {}
public record PipelineResponse(Long id, String name, String description, PipelineStatus status,
                               int version, ExecutionStrategy executionStrategy, Instant createdAt, Instant updatedAt) {}
public record PipelineDetailResponse(Long id, String name, String description, PipelineStatus status,
                                     int version, ExecutionStrategy executionStrategy,
                                     List<PipelineNodeResponse> nodes, List<PipelineEdgeResponse> edges,
                                     Instant createdAt, Instant updatedAt) {}
```

节点、边响应只增加持久化 ID；`NodeMetadataResponse` 包含 `type`、`name`、`category`、`icon`、`supportedEngines`、`defaultEngine` 和 `configSchema`。请求中 `status` 缺省时由应用服务使用 `DRAFT`，执行策略缺省时使用 `AUTO`。

V4 使用三张 `dc_` 表：Pipeline 记录基本信息和 `graph_json`，节点/边引用 Pipeline 并 `ON DELETE CASCADE`；节点 key 在 Pipeline 内唯一；所有表包含 `created_at`、`updated_at`。

- [ ] **步骤 4：运行迁移测试确认通过**

运行同一步骤 2 的命令，预期 `PipelineMigrationTest` 通过。

- [ ] **步骤 5：提交任务 1**

运行：`git add datacraft-server/pom.xml datacraft-server/datacraft-bootstrap/pom.xml datacraft-server/datacraft-api/src/main/java/io/datacraft/api/pipeline datacraft-server/datacraft-pipeline docs/superpowers/plans/2026-09-07-phase4-pipeline-model-plan.md && git commit -m "feat(pipeline): add pipeline contracts and schema"`

### 任务 2：实现 Pipeline、Node、Edge 领域模型和 DAG 校验

**文件：**
- 创建：`datacraft-server/datacraft-pipeline/src/main/java/io/datacraft/pipeline/domain/Pipeline.java`
- 创建：`datacraft-server/datacraft-pipeline/src/main/java/io/datacraft/pipeline/domain/PipelineNode.java`
- 创建：`datacraft-server/datacraft-pipeline/src/main/java/io/datacraft/pipeline/domain/PipelineEdge.java`
- 创建：`datacraft-server/datacraft-pipeline/src/main/java/io/datacraft/pipeline/domain/PipelineValidator.java`
- 创建：`datacraft-server/datacraft-pipeline/src/main/java/io/datacraft/pipeline/application/PipelineValidationException.java`
- 测试：`datacraft-server/datacraft-pipeline/src/test/java/io/datacraft/pipeline/domain/PipelineValidatorTest.java`

- [ ] **步骤 1：先写校验失败测试**

覆盖以下输入：合法 `DATABASE_SOURCE → FILTER → DATABASE_SINK` 图通过；空名称、重复 node key、空 key、未知节点类型、悬空边、自环、重复边和 `A → B → A` 环均抛出 `PipelineValidationException`；独立节点和空图允许保存。测试使用固定节点列表和边列表，不调用数据库或执行引擎。

- [ ] **步骤 2：运行测试确认失败**

运行：`mvn '-Denforcer.skip=true' '-Dmaven.compiler.release=17' '-Dtest=PipelineValidatorTest' '-Dsurefire.failIfNoSpecifiedTests=false' -pl datacraft-server/datacraft-pipeline -am test`

预期：失败，原因是领域类型和校验器尚不存在。

- [ ] **步骤 3：实现不可变领域 records 和 Kahn 环检测**

`Pipeline` 持有不可变 nodes/edges；`PipelineValidator.validate(name, nodes, edges, nodeRegistry)` 先校验名称和 key，再校验边引用、重复边、自环，最后用入度表和队列执行 Kahn 拓扑检查。校验器只操作节点 key，不加载业务数据，不调用 Engine。

- [ ] **步骤 4：运行校验测试确认通过**

运行同一步骤 2 的命令，预期所有 PipelineValidator 测试通过。

- [ ] **步骤 5：提交任务 2**

运行：`git add datacraft-server/datacraft-pipeline/src/main/java/io/datacraft/pipeline/domain datacraft-server/datacraft-pipeline/src/main/java/io/datacraft/pipeline/application/PipelineValidationException.java datacraft-server/datacraft-pipeline/src/test/java/io/datacraft/pipeline/domain/PipelineValidatorTest.java && git commit -m "feat(pipeline): add graph model validation"`

### 任务 3：实现 NodeMetadata、NodeRegistry 和 Registry 测试

**文件：**
- 创建：`datacraft-server/datacraft-pipeline/src/main/java/io/datacraft/pipeline/domain/NodeCategory.java`
- 创建：`datacraft-server/datacraft-pipeline/src/main/java/io/datacraft/pipeline/domain/NodeMetadata.java`
- 创建：`datacraft-server/datacraft-pipeline/src/main/java/io/datacraft/pipeline/application/NodeTypeNotFoundException.java`
- 创建：`datacraft-server/datacraft-pipeline/src/main/java/io/datacraft/pipeline/application/NodeRegistry.java`
- 测试：`datacraft-server/datacraft-pipeline/src/test/java/io/datacraft/pipeline/application/NodeRegistryTest.java`

- [ ] **步骤 1：先写 Registry 失败测试**

断言默认 Registry 返回按类型排序的 `DATABASE_SOURCE`、`FILTER`、`DATABASE_SINK`，每个节点的分类、配置字段和默认引擎元数据稳定；查询未知类型抛出 `NodeTypeNotFoundException`。测试明确断言 Registry 不创建任何执行引擎对象。

- [ ] **步骤 2：运行测试确认失败**

运行：`mvn '-Denforcer.skip=true' '-Dmaven.compiler.release=17' '-Dtest=NodeRegistryTest' '-Dsurefire.failIfNoSpecifiedTests=false' -pl datacraft-server/datacraft-pipeline -am test`

预期：失败，原因是 Registry 和元数据类型尚不存在。

- [ ] **步骤 3：实现 Registry**

`NodeMetadata` 使用不可变记录；`configSchema` 使用 `Map<String, Object>` 描述字段名称和类型。Registry 构造时注册三个内置元数据，`list()` 返回不可变列表，`get(type)` 对未知类型抛出业务异常。`supportedEngines` 只保存字符串能力声明，不引入 Engine 模块或第三方引擎依赖。

- [ ] **步骤 4：运行 Registry 测试确认通过**

运行同一步骤 2 的命令，预期 Registry 测试全部通过。

- [ ] **步骤 5：提交任务 3**

运行：`git add datacraft-server/datacraft-pipeline/src/main/java/io/datacraft/pipeline/domain datacraft-server/datacraft-pipeline/src/main/java/io/datacraft/pipeline/application/NodeTypeNotFoundException.java datacraft-server/datacraft-pipeline/src/test/java/io/datacraft/pipeline/application/NodeRegistryTest.java && git commit -m "feat(pipeline): add node metadata registry"`

### 任务 4：实现 MyBatis 持久化和事务性图快照仓储

**文件：**
- 创建：`datacraft-server/datacraft-pipeline/src/main/java/io/datacraft/pipeline/infrastructure/MybatisPipelineConfiguration.java`
- 创建：`datacraft-server/datacraft-pipeline/src/main/java/io/datacraft/pipeline/infrastructure/persistence/entity/PipelineEntity.java`
- 创建：`datacraft-server/datacraft-pipeline/src/main/java/io/datacraft/pipeline/infrastructure/persistence/entity/PipelineNodeEntity.java`
- 创建：`datacraft-server/datacraft-pipeline/src/main/java/io/datacraft/pipeline/infrastructure/persistence/entity/PipelineEdgeEntity.java`
- 创建：`datacraft-server/datacraft-pipeline/src/main/java/io/datacraft/pipeline/infrastructure/persistence/mapper/PipelineMapper.java`
- 创建：`datacraft-server/datacraft-pipeline/src/main/java/io/datacraft/pipeline/infrastructure/persistence/mapper/PipelineNodeMapper.java`
- 创建：`datacraft-server/datacraft-pipeline/src/main/java/io/datacraft/pipeline/infrastructure/persistence/mapper/PipelineEdgeMapper.java`
- 创建：`datacraft-server/datacraft-pipeline/src/main/java/io/datacraft/pipeline/infrastructure/persistence/PipelineRepository.java`
- 创建：`datacraft-server/datacraft-pipeline/src/main/java/io/datacraft/pipeline/infrastructure/persistence/MybatisPipelineRepository.java`
- 测试：`datacraft-server/datacraft-pipeline/src/test/java/io/datacraft/pipeline/infrastructure/PipelineRepositoryMappingTest.java`

- [ ] **步骤 1：先写持久化映射失败测试**

断言 Entity 的 `@TableName`、字段列映射、Pipeline 节点/边 ID 类型和状态/策略字符串映射正确；仓储将三类 Entity 还原为有序领域对象，并在替换快照时先删除旧图、再插入 Pipeline 节点和边。

- [ ] **步骤 2：运行测试确认失败**

运行：`mvn '-Denforcer.skip=true' '-Dmaven.compiler.release=17' '-Dtest=PipelineRepositoryMappingTest' '-Dsurefire.failIfNoSpecifiedTests=false' -pl datacraft-server/datacraft-pipeline -am test`

预期：失败，原因是 Entity、Mapper 和 Repository 尚不存在。

- [ ] **步骤 3：实现 Entity、Mapper 和 Repository**

Repository 提供：`findAll()`、`findById(Long)`、`save(Pipeline)`、`replaceGraph(Long, List<PipelineNode>, List<PipelineEdge>)`、`deleteById(Long)`。`replaceGraph` 使用 `@Transactional`，删除旧节点/边依赖 Pipeline 级联或显式删除，插入新节点/边并保持 ordinal 查询顺序；Pipeline `graph_json` 由节点/边控制面结构序列化生成，不能包含凭据。

- [ ] **步骤 4：运行持久化测试确认通过**

运行同一步骤 2 的命令，预期 Entity 映射和快照替换测试通过。

- [ ] **步骤 5：提交任务 4**

运行：`git add datacraft-server/datacraft-pipeline/src/main/java/io/datacraft/pipeline/infrastructure datacraft-server/datacraft-pipeline/src/test/java/io/datacraft/pipeline/infrastructure/PipelineRepositoryMappingTest.java && git commit -m "feat(pipeline): add pipeline persistence"`

### 任务 5：实现 Pipeline 应用服务和服务层测试

**文件：**
- 创建：`datacraft-server/datacraft-pipeline/src/main/java/io/datacraft/pipeline/application/PipelineNotFoundException.java`
- 创建：`datacraft-server/datacraft-pipeline/src/main/java/io/datacraft/pipeline/application/PipelineApplicationService.java`
- 测试：`datacraft-server/datacraft-pipeline/src/test/java/io/datacraft/pipeline/application/PipelineApplicationServiceTest.java`

- [ ] **步骤 1：先写应用服务失败测试**

覆盖创建默认 DRAFT/AUTO、创建合法图、未知节点类型、非法 DAG、更新时版本从 1 递增、更新替换节点/边、查询详情保持节点/边顺序、删除和 Pipeline 不存在等行为。使用 Mockito 验证服务只调用 Repository 和 Registry，不直接调用 Mapper。

- [ ] **步骤 2：运行测试确认失败**

运行：`mvn '-Denforcer.skip=true' '-Dmaven.compiler.release=17' '-Dtest=PipelineApplicationServiceTest' '-Dsurefire.failIfNoSpecifiedTests=false' -pl datacraft-server/datacraft-pipeline -am test`

预期：失败，原因是应用服务尚不存在。

- [ ] **步骤 3：实现应用服务**

服务将请求转换为领域对象，使用 Registry 校验节点类型，使用 PipelineValidator 校验图，创建时设置版本 1，更新时读取当前 Pipeline 并递增版本；先保存基本信息，再在同一事务边界内替换图快照。输出只使用 `PipelineResponse` 和 `PipelineDetailResponse`，不返回 Entity。

- [ ] **步骤 4：运行服务测试确认通过**

运行同一步骤 2 的命令，预期应用服务测试全部通过。

- [ ] **步骤 5：提交任务 5**

运行：`git add datacraft-server/datacraft-pipeline/src/main/java/io/datacraft/pipeline/application datacraft-server/datacraft-pipeline/src/test/java/io/datacraft/pipeline/application/PipelineApplicationServiceTest.java && git commit -m "feat(pipeline): add pipeline application service"`

### 任务 6：实现 REST API、异常处理和 Web 层测试

**文件：**
- 创建：`datacraft-server/datacraft-pipeline/src/main/java/io/datacraft/pipeline/web/PipelineController.java`
- 创建：`datacraft-server/datacraft-pipeline/src/main/java/io/datacraft/pipeline/web/PipelineExceptionHandler.java`
- 创建：`datacraft-server/datacraft-pipeline/src/test/java/io/datacraft/pipeline/PipelineTestApplication.java`
- 测试：`datacraft-server/datacraft-pipeline/src/test/java/io/datacraft/pipeline/web/PipelineControllerTest.java`

- [ ] **步骤 1：先写 MVC 失败测试**

覆盖未登录请求返回 401；登录用户可以访问 Pipeline 列表、创建、详情、更新和删除；`GET /api/v1/node-types` 返回三个元数据类型；Pipeline 不存在返回 `PIPELINE_NOT_FOUND`/404；校验失败返回 `PIPELINE_VALIDATION`/400；未知节点类型返回 `NODE_TYPE_NOT_FOUND`/400；响应中不出现 Entity 内部字段或凭据字段。

- [ ] **步骤 2：运行测试确认失败**

运行：`mvn '-Denforcer.skip=true' '-Dmaven.compiler.release=17' '-Dtest=PipelineControllerTest' '-Dsurefire.failIfNoSpecifiedTests=false' -pl datacraft-server/datacraft-pipeline -am test`

预期：失败，原因是 Controller 和异常处理器尚不存在。

- [ ] **步骤 3：实现 Controller 和异常处理器**

Controller 只注入 `PipelineApplicationService` 和 `NodeRegistry`，映射：

```text
GET    /api/v1/pipelines
POST   /api/v1/pipelines
GET    /api/v1/pipelines/{id}
PUT    /api/v1/pipelines/{id}
DELETE /api/v1/pipelines/{id}
GET    /api/v1/node-types
GET    /api/v1/node-types/{type}
```

异常处理器将三类业务异常映射为固定错误码和安全消息；不返回底层 SQL 或堆栈。

- [ ] **步骤 4：运行 MVC 测试确认通过**

运行同一步骤 2 的命令，预期 Web 层测试全部通过。

- [ ] **步骤 5：提交任务 6**

运行：`git add datacraft-server/datacraft-pipeline/src/main/java/io/datacraft/pipeline/web datacraft-server/datacraft-pipeline/src/test/java/io/datacraft/pipeline/PipelineTestApplication.java datacraft-server/datacraft-pipeline/src/test/java/io/datacraft/pipeline/web/PipelineControllerTest.java && git commit -m "feat(pipeline): expose pipeline and node metadata APIs"`

### 任务 7：更新进度文档、全量验证和交付

**文件：**
- 修改：`DEV_PROGRESS.md`
- 修改：`README.md`
- 创建：`docs/adr/0009-phase4-pipeline-model.md`

- [ ] **步骤 1：更新文档**

将当前 Phase 更新为 Phase 4 Completed，Next 更新为 Phase 5 — Pipeline Editor；README 增加 Pipeline 模块、API 和当前不执行 Pipeline 的边界；ADR 记录三表模型、快照替换、DAG 校验、NodeRegistry 与 Engine 解耦。

- [ ] **步骤 2：运行后端全量测试与打包**

JDK 21 可用时运行 `mvn clean test` 和 `mvn '-DskipTests' package`；否则记录 Enforcer 失败，并用 JDK 17 运行 `mvn clean '-Denforcer.skip=true' '-Dmaven.compiler.release=17' '-Dsurefire.failIfNoSpecifiedTests=false' test` 及 `mvn '-Denforcer.skip=true' '-Dmaven.compiler.release=17' '-DskipTests' package`。

- [ ] **步骤 3：运行前端回归**

运行：`npm run test -- --run` 和 `npm run build`。Phase 4 不新增前端实现，但必须确认 Phase 3 页面未回归。

- [ ] **步骤 4：检查边界和工作区**

运行：`rg --files datacraft-server datacraft-web | rg "pipeline|Pipeline|Engine|DataX|Camel|SeaTunnel|Flink"`、`git diff --check`、`git status --short`。确认没有执行引擎、Planner、Vue Flow 编辑器或明文密码变更。

- [ ] **步骤 5：提交文档并完成收尾**

运行：`git add DEV_PROGRESS.md README.md docs/adr/0009-phase4-pipeline-model.md && git commit -m "docs(pipeline): mark phase 4 complete"`。提交前读取测试输出和 Git 状态，报告提交 SHA、验证结果、Java 版本限制、已知风险及 Phase 5 起点。
