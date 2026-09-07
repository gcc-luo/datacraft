# Phase 2 Datasource Design

## Goal

实现 DataCraft Phase 2 的数据源管理闭环：用户可以创建、查看、编辑、删除 PostgreSQL/MySQL 数据源，并测试连接；数据源密码始终加密保存，且不会出现在 API 响应、前端状态或日志中。

## Scope

本阶段只实现 JDBC 数据源管理，不实现元数据采集、Pipeline 节点、连接器注册中心或第三方执行引擎。

支持的数据源类型：

- `POSTGRESQL`
- `MYSQL`

连接配置采用结构化字段：名称、主机、端口、数据库、用户名、密码和备注。JDBC URL 由类型和结构化字段在服务端生成，避免前端提交任意连接协议。

## Backend Architecture

新增 `datacraft-datasource` 模块，依赖 `datacraft-api` 和 `datacraft-common`，由 bootstrap 聚合。模块内部保持领域、应用、持久化和 Web 分层：

- `Datasource`：不包含明文密码，只表达领域状态和加密凭据。
- `DatasourceRepository`：领域仓储接口。
- `JdbcDatasourceRepository`：MyBatis-Plus 持久化实现。
- `DatasourceApplicationService`：CRUD、连接测试、状态更新和响应映射。
- `DatasourceConnectionTester`：根据数据源类型生成 URL，使用 JDBC 建立连接并在 try-with-resources 中关闭。
- `SecretCryptoService`：AES-256-GCM 加解密，随机 12 字节 nonce，密文格式带版本前缀 `v1:`。
- `DatasourceController`：REST API，只调用应用服务。

数据库新增 Flyway `V2__create_datasource_table.sql`，表名为 `dc_datasource`。密码列命名为 `password_ciphertext`，并通过约束限制类型和状态。删除数据源为物理删除；本阶段没有被引用关系，因此不需要软删除或引用检查。

## Security and Configuration

加密密钥通过环境变量提供：

```yaml
datacraft:
  security:
    datasource:
      encryption-key: ${DATACRAFT_DATASOURCE_ENCRYPTION_KEY}
```

密钥必须是 Base64 编码的 32 字节值。应用启动时校验缺失或长度不正确的密钥并快速失败。密码只在创建/更新请求进入应用服务时短暂存在，在连接测试时解密到局部变量；禁止写入日志、响应、前端 store 或异常消息。

## API

所有接口都要求已登录：

```text
GET    /api/v1/datasources
POST   /api/v1/datasources
GET    /api/v1/datasources/{id}
PUT    /api/v1/datasources/{id}
DELETE /api/v1/datasources/{id}
POST   /api/v1/datasources/{id}/test
```

请求体使用 `DatasourceRequest`。创建时密码必填；更新时密码可空，空值表示保留已有密码。响应使用 `DatasourceResponse`，只返回结构化连接信息、状态、最近测试时间、耗时和安全提示，绝不返回密码或密文。

连接测试成功时返回 `SUCCESS` 和“连接成功”；失败时返回 `FAILED` 和固定的“连接失败，请检查配置与网络”，避免 JDBC 异常泄露 URL、用户名或其他敏感信息。测试结果同时更新数据源的状态字段。

## Frontend Experience

沿用 Phase 1 的 console-first 视觉语言：

- `/datasources`：表格展示名称、类型、地址、数据库、状态、最后测试时间和操作。
- 创建/编辑使用同一表单视图；编辑表单密码默认空白，旁边明确提示“留空则保留原密码”。
- “测试连接”展示 loading 和结果，不展示密码。
- 删除前使用确认对话框；成功后刷新列表。
- 未认证时继续由现有路由守卫重定向到登录页。

## Error Handling

- 参数校验失败返回 `VALIDATION_ERROR` 和字段级提示。
- 数据源不存在返回 `DATASOURCE_NOT_FOUND`、HTTP 404。
- 名称重复返回 `DATASOURCE_DUPLICATE`、HTTP 409。
- 连接测试失败不抛出 JDBC 原始异常到客户端，只返回失败结果。
- 加密配置错误在启动阶段失败，不允许降级到明文或固定密钥。

## Testing Strategy

- 单元测试覆盖加解密、密钥校验、URL 生成、CRUD 服务、更新时保留密码和连接测试结果映射。
- Web 层测试覆盖认证要求、请求校验、密码不出现在响应和 404/409 映射。
- 持久化映射测试覆盖 `dc_datasource` 字段到实体。
- 前端测试覆盖列表加载、创建/编辑表单密码策略、测试连接按钮和删除刷新。
- 最终执行 Maven 测试/打包、前端测试/构建，并用 Docker PostgreSQL 做真实应用启动与 API 冒烟验证。

## Phase Boundary

本阶段完成后，`DEV_PROGRESS.md` 将把 Phase 2 标记为完成，下一阶段仅保留为 Phase 3 Metadata；不实现数据集、字段采集、Pipeline 或 Engine。
