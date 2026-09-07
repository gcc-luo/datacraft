# Phase 6 Engine API 设计

## 背景

Phase 5 已完成 Pipeline 图编辑和控制面持久化。下一阶段需要建立执行引擎的稳定扩展边界，让控制面可以发现可用引擎、展示引擎能力和判断引擎健康状态，同时保持 Pipeline 核心不直接依赖任何第三方执行引擎。

## 范围

本阶段只实现 Engine API：

- 新增独立的 `datacraft-execution` 模块；
- 定义 `ExecutionEngine` SPI；
- 定义引擎元数据、能力和健康状态模型；
- 提供 `EngineRegistry`，负责引擎注册、查找、去重和稳定排序；
- 注册内置 `NativeExecutionEngine`，声明当前可用能力；
- 提供认证保护的 `GET /api/v1/engines` API；
- 为 SPI、Registry、Native 引擎和 API 增加测试。

## 非目标

本阶段不实现：

- `ExecutionPlan`、Stage、Planner 和 DataReference；
- JDBC Source、Filter、Sink 的实际执行；
- MySQL 到 PostgreSQL 的数据传输；
- DataX、Camel、SeaTunnel、Flink 适配；
- Execution 记录、调度、质量校验和实时运行 UI；
- 引擎配置数据库和第三方引擎管理。

以上内容分别留给 Phase 7、Phase 8 及后续阶段，避免提前耦合运行时实现。

## 设计

### SPI 边界

`ExecutionEngine` 放在独立的 execution 模块中，只暴露引擎身份、元数据和健康检查：

```java
public interface ExecutionEngine {
    String engineType();
    EngineMetadata metadata();
    EngineHealth healthCheck();
}
```

执行计划相关方法在 Phase 7 定义，避免本阶段反向创建未完成的 Planner 模型。

### 引擎能力

能力使用稳定的代码集合表示，Native 首批声明：`JDBC_SOURCE`、`JDBC_SINK`、`SQL_PUSHDOWN`、`FILTER`。能力声明是只读元数据，不代表本阶段已经具备执行能力；实际执行能力由后续 Runtime 阶段验收。

### 健康检查

健康状态使用 `UP`、`DOWN`、`UNKNOWN`。Native 是嵌入式引擎，Phase 6 的健康检查只验证组件已加载，不访问业务数据源，避免把数据源网络状态混入 Engine Registry。

### API

`GET /api/v1/engines` 返回所有已注册引擎，按 `code` 升序排列，并在同一个响应中给出元数据和本次健康检查结果。API 复用现有 `ApiResponse` 包装和 Spring Security 认证约束。

## 验收标准

- Spring Boot 能自动发现并注册 Native 引擎；
- 重复引擎代码和未知引擎查询有稳定异常；
- 未认证请求 `/api/v1/engines` 返回 401；
- 已认证请求能返回 Native 的能力和 `UP` 健康状态；
- execution 模块不依赖 DataX、Camel、SeaTunnel、Flink；
- Maven 全量测试和打包通过；
- `DEV_PROGRESS.md` 更新为 Phase 6 完成，并将 Phase 7 标记为下一阶段。
