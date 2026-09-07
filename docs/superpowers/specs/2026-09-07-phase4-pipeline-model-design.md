# Phase 4 Pipeline Model Design

## Goal

在已有数据源和数据资产基础上，建立 DataCraft 控制面的 Pipeline、Node、Edge 模型，并提供可供下一阶段 Vue Flow 编辑器使用的保存、加载和节点元数据 API。

## Scope

本阶段实现：

- `datacraft-pipeline` 独立模块及 Maven 集成
- Pipeline、Node、Edge 领域模型和 PostgreSQL 持久化
- Pipeline 创建、查询、更新、删除和图快照替换
- NodeMetadata 与 NodeRegistry，以及 `GET /api/v1/node-types`
- Pipeline 名称、节点 key、边引用、重复边和 DAG 环检测
- 认证、统一响应、统一异常和 Entity/DTO 分离

本阶段不实现 Vue Flow 编辑器、Pipeline 执行、ExecutionPlanner、ExecutionEngine、质量、调度或第三方引擎适配。

## Architecture

```text
PipelineController
        ↓
PipelineApplicationService
        ├── PipelineValidator
        ├── NodeRegistry
        └── PipelineRepository
                ↓
        dc_pipeline / dc_pipeline_node / dc_pipeline_edge
```

`datacraft-pipeline` 只依赖 `datacraft-api`、`datacraft-common` 和 Spring/MyBatis 基础设施，不依赖 DataX、Camel、SeaTunnel 或 Flink。Controller 只调用应用服务，服务负责校验、事务和 DTO 映射。

## Domain Model

### Pipeline

- `id`
- `name`
- `description`
- `status`: `DRAFT`, `ACTIVE`, `ARCHIVED`
- `version`
- `executionStrategy`: `AUTO`, `NATIVE`, `DATAX`, `CAMEL`, `SEATUNNEL`
- `createdBy`, `createdAt`, `updatedAt`
- `nodes`, `edges`

### PipelineNode

- `id`
- `nodeKey`
- `nodeType`
- `nodeName`
- `x`, `y`
- `configJson`
- `preferredEngine`

### PipelineEdge

- `id`
- `sourceNodeKey`, `targetNodeKey`
- `sourcePort`, `targetPort`
- `conditionJson`

数据集引用只作为节点 `configJson` 中的控制面配置保存；本阶段不读取业务数据，也不把数据行装入 JVM。

## Node Metadata and Registry

`NodeMetadata` 描述节点类型，不绑定具体执行引擎实现：

- `type`, `name`, `category`, `icon`
- `supportedEngines`, `defaultEngine`
- `configSchema`

初始注册三个控制面可建模的节点类型：`DATABASE_SOURCE`、`FILTER`、`DATABASE_SINK`。它们只提供名称、分类和配置字段描述，不提供运行时执行能力。`NodeRegistry` 提供按类型查询和列表能力，应用服务通过它校验 Pipeline 节点类型。

## Database Model

提供 Flyway `V4__create_pipeline_tables.sql`：

- `dc_pipeline`: Pipeline 基本信息、版本、状态、执行策略、图 JSON 和审计时间
- `dc_pipeline_node`: 节点配置，按 Pipeline 级联删除
- `dc_pipeline_edge`: 边配置，按 Pipeline 级联删除

Pipeline 删除时节点和边级联删除；节点 `node_key` 在同一 Pipeline 内唯一；边按 Pipeline、源 key、目标 key、端口组合避免重复。

## API

所有接口要求登录：

```text
GET    /api/v1/pipelines
POST   /api/v1/pipelines
GET    /api/v1/pipelines/{id}
PUT    /api/v1/pipelines/{id}
DELETE /api/v1/pipelines/{id}
GET    /api/v1/node-types
GET    /api/v1/node-types/{type}
```

创建和更新请求同时携带 Pipeline 基本信息、节点数组和边数组；应用服务以事务替换当前图快照。响应使用 API DTO，不暴露数据库 Entity。

## Validation and Errors

- Pipeline 名称去除首尾空白后不能为空，长度不超过 100
- 节点 key 在同一 Pipeline 内必须唯一且非空
- 节点类型必须存在于 `NodeRegistry`
- 边的源节点和目标节点必须存在，不能自环，不能重复
- 图必须是 DAG；使用 Kahn 拓扑排序检查环，不执行任何节点
- 不存在的 Pipeline 返回 `PIPELINE_NOT_FOUND` 和 HTTP 404
- 图校验失败返回 `PIPELINE_VALIDATION` 和 HTTP 400
- 未知节点类型返回 `NODE_TYPE_NOT_FOUND` 和 HTTP 400

异常响应不包含 SQL、连接信息、密码或原始堆栈内容。

## Testing

后端测试覆盖：

- V4 迁移包含三张表、级联外键、唯一约束和审计字段
- Pipeline/Node/Edge Entity 映射
- 空图、合法 DAG、重复 key、悬空边、自环、重复边和环检测
- NodeRegistry 列表、类型查询和未知类型
- 应用服务的创建、更新快照、查询、删除、版本递增和错误映射
- Controller 认证、CRUD、DTO 响应和统一异常

## Acceptance

```text
登录
↓
创建名为 customer-clean 的 Pipeline
↓
保存 DATABASE_SOURCE → FILTER → DATABASE_SINK
↓
刷新后完整恢复节点、坐标、配置和边
↓
提交包含环的图并收到 PIPELINE_VALIDATION
```

## Next Phase

Phase 5 实现 Vue Flow Pipeline Editor，调用本阶段 Pipeline CRUD 和 NodeMetadata API；本阶段不添加编辑器代码。
