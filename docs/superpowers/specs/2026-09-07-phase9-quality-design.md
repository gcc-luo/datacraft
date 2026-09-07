# Phase 9 Quality 设计

## 目标

补齐 DESIGN.md 中 V0.6 / Phase 9 的质量能力：通过统一规则 Provider 执行数据库表级质量检查，生成 `totalRows`、`errorRows`、`passRows`、`passRate` 和受控异常样例，并通过鉴权 API 查询结果。

## 范围

- 新增独立 `datacraft-quality` 模块，不让 Pipeline 核心依赖质量实现。
- 支持 `NULL_CHECK`、`UNIQUE_CHECK`、`RANGE_CHECK`、`REGEX_CHECK`、`LENGTH_CHECK`、`ENUM_CHECK`、`CUSTOM_SQL_CHECK`。
- 使用 `SqlDialect` 生成 MySQL/PostgreSQL 的差异 SQL，所有用户 SQL 仅允许安全的条件表达式。
- 以 `datasourceId`、`tableName` 和规则配置作为一次质量检查输入。
- 质量结果和最多 1000 条样例写入 PostgreSQL；样例只保存有限字段值，不复制全量数据。
- 提供创建检查、查询结果列表、查询结果详情 API。
- 将质量节点元数据注册为 Native 支持节点，使 Pipeline 编辑器可识别质量节点。

## API

```text
POST /api/v1/quality/checks
GET  /api/v1/quality/results
GET  /api/v1/quality/results/{id}
```

创建请求：

```json
{
  "datasourceId": 1001,
  "tableName": "customer",
  "executionId": 2001,
  "nodeExecutionId": 3001,
  "rules": [
    {"type": "NULL_CHECK", "field": "phone"},
    {"type": "RANGE_CHECK", "field": "age", "min": 0, "max": 120}
  ]
}
```

`executionId` 和 `nodeExecutionId` 在尚未接入执行记录阶段时允许为空；结果仍然可以独立追踪。查询接口不得返回数据源密码。

## 执行与安全

- 规则使用单条聚合查询获取总数和异常数；异常样例使用固定上限查询。
- `CUSTOM_SQL_CHECK` 只接收条件表达式，不接收完整 SQL；拒绝分号、注释、DDL/DML 关键字。
- 标识符统一由 SQL 方言引用，字段名不直接拼接为未转义 SQL。
- 连接通过现有 `JdbcConnectionProvider` 获取，密码只在连接创建时短暂解密。
- 质量结果写入与规则执行使用独立事务，不把一个事务包住整个 Pipeline。

## 非目标

- 本阶段不实现 Scheduler、Execution History、SSE、重试、取消。
- 本阶段不实现 DataX、Camel、SeaTunnel、Flink。
- 本阶段不改变已有 Pipeline 持久化模型，也不直接让 NativePipelineExecutor 承担完整质量节点编排。

## 验收标准

1. 七类规则均能生成数据库差异 SQL，并拒绝不安全自定义条件。
2. 质量检查能返回并持久化总行数、异常行数、通过行数和通过率。
3. 异常样例最多 1000 条，结果详情可以读取样例。
4. API 全部经过 Spring Security 鉴权，未知结果和非法规则返回稳定错误。
5. 质量节点出现在 NodeRegistry，并支持 Native 引擎。
6. 提供迁移、单元测试、MVC 测试、Maven 全量测试和前端构建验证。
