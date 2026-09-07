# Phase 3 Metadata Design

## Goal

在已有 PostgreSQL/MySQL 数据源管理能力之上，实现元数据采集和数据资产浏览闭环：用户可以从数据源同步 Schema、Table、Field，并在 `/assets` 查看表和字段信息。

本阶段只实现数据库表级和字段级元数据，不实现 View、Index、存储过程、血缘、Pipeline 或任何执行引擎能力。

## Scope

支持的数据源类型沿用 Phase 2：

- `POSTGRESQL`
- `MYSQL`

采集范围：

- Schema 名称
- Table 名称和备注
- 估算行数
- Field 名称、顺序、类型、可空性、主键标识和备注
- 最近采集时间

系统表和系统 Schema 不进入资产列表。重复同步使用数据源、catalog、schema、table 的组合键更新已有数据集，并同步替换字段集合，不产生重复记录。

## Architecture

新增独立 `datacraft-metadata` 模块，保持模块化单体结构：

```text
MetadataController
        ↓
MetadataApplicationService
        ↓
MetadataCollector
        ↓
DatasetRepository
        ↓
dc_dataset / dc_dataset_field
```

`MetadataCollector` 是统一端口；`PostgresqlMetadataCollector` 和 `MysqlMetadataCollector` 封装数据库差异。采集器使用 JDBC `DatabaseMetaData` 获取表、字段、主键和备注，并使用只读的数据库方言查询获取估算行数。

元数据模块通过已有数据源领域仓储读取连接配置，通过已有 `SecretCryptoService` 在同步调用的局部作用域内解密密码。明文密码不得进入实体、DTO、日志或异常消息。

Controller 只调用应用服务；持久化 Entity 不直接作为 API 响应；API 使用 `datacraft-api` 中的 DTO。

## Database Model

提供 Flyway `V3__create_metadata_tables.sql`：

### `dc_dataset`

```text
id
datasource_id
catalog_name
schema_name
table_name
table_remark
estimated_row_count
collected_at
created_at
updated_at
```

外键关联 `dc_datasource(id)`，数据源删除时级联删除元数据；唯一约束为 `(datasource_id, catalog_name, schema_name, table_name)`。

### `dc_dataset_field`

```text
id
dataset_id
field_name
ordinal_position
data_type
nullable
primary_key
field_remark
created_at
updated_at
```

外键关联 `dc_dataset(id)` 并级联删除；唯一约束为 `(dataset_id, field_name)`。为数据源、Schema、数据集和字段查询建立必要索引。

## Collection Behavior

采集流程：

1. 根据数据源 ID 查询数据源；不存在时返回 `DATASOURCE_NOT_FOUND`。
2. 使用数据源类型选择 PostgreSQL 或 MySQL 采集器。
3. 解密密码到局部变量，建立 JDBC 连接，并在 try-with-resources 中关闭。
4. 读取非系统 Schema、表、字段和主键。
5. 读取表备注及估算行数；估算值不可用时允许返回 `null`，不能阻断其他元数据保存。
6. 在一个应用服务事务中 upsert 数据集并替换该数据集的字段集合。
7. 返回本次同步的 Schema 数、Table 数、Field 数和采集时间。

数据库差异：

- PostgreSQL 使用 `pg_class.reltuples` / `pg_namespace` 获取估算行数，并排除 `pg_catalog`、`information_schema`、`pg_toast` 等系统 Schema。
- MySQL 使用 `information_schema.tables.table_rows` 获取估算行数，并以当前数据库作为 Schema 范围。
- 表和字段的基础信息优先使用 JDBC `DatabaseMetaData`，避免把厂商 SQL 扩散到应用层。

任何单表行数估算失败只记录安全的内部诊断信息并将该表估算值置空；连接失败返回固定的 `元数据同步失败，请检查数据源配置与网络`，不向客户端泄露 JDBC URL、用户名或原始异常。

## API

所有接口要求登录：

```text
POST /api/v1/datasources/{datasourceId}/metadata/sync
GET  /api/v1/datasets?datasourceId=&schemaName=&keyword=
GET  /api/v1/datasets/{id}
```

同步响应返回：

```text
datasourceId
schemaCount
datasetCount
fieldCount
collectedAt
```

数据集列表响应返回：

```text
id
datasourceId
catalogName
schemaName
tableName
tableRemark
estimatedRowCount
collectedAt
```

数据集详情响应包含上述数据集字段和有序 `fields` 数组。字段响应不包含任何凭据。

参数错误返回 `VALIDATION_ERROR`；数据集不存在返回 `DATASET_NOT_FOUND`、HTTP 404；连接或采集失败返回安全的业务错误。

## Frontend Experience

新增 `/assets` 页面，采用已确认的 C 方案：面包屑 + 分栏。

- 顶部展示“数据资产”和“同步元数据”按钮。
- 数据源下拉框切换当前资产范围。
- 左侧按 Schema 分组显示数据集列表，支持表名/字段名关键词搜索。
- 右侧展示当前表的面包屑、表备注、估算行数、最近采集时间和字段列表。
- 字段列表展示字段名、数据类型、可空性、主键和备注。
- 同步过程中显示 loading；成功后刷新列表并自动选中首个数据集；失败显示安全错误信息。
- 数据源为空时引导先去数据源页面创建连接。

页面复用现有 console-first 视觉语言，不新增 UI 框架或状态管理层。

## Testing

后端测试覆盖：

- PostgreSQL/MySQL 采集器的表、字段、主键和系统 Schema 过滤
- 估算行数方言查询
- Dataset/Field 持久化映射
- 同步服务的 upsert、字段替换和统计结果
- 数据源不存在、连接失败和安全错误映射
- API 认证、列表筛选、详情响应和密码不泄露

前端测试覆盖：

- 数据资产加载和数据源切换
- Schema 分组、表选择和字段详情
- 关键词筛选
- 同步按钮 loading、成功刷新和失败提示
- 空状态引导

## Acceptance

```text
添加 MySQL
↓
测试连接成功
↓
同步 customer 表元数据
↓
在数据资产页看到 Schema / customer
↓
查看 customer 的字段、类型和主键
```

本阶段完成后更新 `DEV_PROGRESS.md` 为 Phase 3 Completed，并将下一阶段设置为 Phase 4 — Pipeline Model。

## Out of Scope

- Pipeline、Node、Edge、NodeRegistry
- Vue Flow 编辑器
- ExecutionEngine、ExecutionPlanner、Native Runtime
- DataX、Camel、SeaTunnel、Flink
- 数据质量、调度、血缘和数据标准
- View、Index、存储过程和触发器元数据

