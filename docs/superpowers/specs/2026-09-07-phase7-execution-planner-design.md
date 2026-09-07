# Phase 7 Execution Planner 设计

## 背景

Phase 6 已建立 `ExecutionEngine` SPI 和 Native 引擎注册。Pipeline 目前仍然只是控制面图模型，执行前需要一个独立的规划步骤，把 DAG 转换为引擎可消费的执行计划，同时为跨 Stage 数据交接保留引用，而不是把数据装入 Java `List`。

## 范围

- 新增不可变 `ExecutionPlan`、`ExecutionStagePlan` 和 `DataReference` 模型；
- 定义 `DataReferenceType`：`DATABASE_TABLE`、`DATABASE_QUERY`、`FILE`、`OBJECT_STORAGE`、`KAFKA_TOPIC`、`MEMORY`、`NONE`；
- 实现 `ExecutionPlanner`：复用 PipelineValidator，按拓扑序遍历节点，解析引擎选择，按引擎连续段拆分 Stage；
- 检查节点是否支持选定引擎，并检查引擎是否已在 EngineRegistry 注册；
- 从数据库源/目标节点的非敏感配置生成表级 DataReference；
- 为跨 Stage 输出生成逻辑引用，并标注后续 Runtime 需要 Materialization；
- 增加 Planner 单元测试。

## 非目标

本阶段不实现：

- JDBC 连接、ResultSet、批量写入和 SQL 执行；
- Materialization 的实际创建、清理和生命周期管理；
- Execution/Stage 数据库记录和运行状态；
- Pipeline 执行接口、调度、质量校验和实时日志；
- DataX、Camel、SeaTunnel、Flink 的运行时适配。

## 规划规则

1. 先复用 `PipelineValidator` 做名称、节点、边和 DAG 校验。
2. 节点引擎选择优先级为：节点 `preferredEngine` > Pipeline 的非 `AUTO` 策略 > 节点默认引擎。
3. 选定引擎必须同时满足节点 `supportedEngines` 和 `EngineRegistry` 注册要求。
4. 使用稳定拓扑序；同一引擎的连续节点合并为一个 Stage，Stage 索引从 0 开始。
5. 源节点和目标节点只生成逻辑表引用，引用元数据只包含 datasourceId、tableName、writeMode 等非密码字段。
6. 非末尾 Stage 的输出使用 `DATABASE_QUERY` 逻辑引用，并写入 `MATERIALIZATION_REQUIRED` 标记；这只是 Planner 合同，不创建临时表。

## 验收标准

- `DATABASE_SOURCE -> FILTER -> DATABASE_SINK` 使用默认 Native 时生成一个 Native Stage；
- 不支持的节点/引擎组合和未注册引擎会在规划阶段失败；
- 不同引擎的连续段被拆分为多个 Stage，并包含跨 Stage DataReference；
- 非法 JSON 和缺少数据源/表配置会得到稳定规划异常；
- Planner 不依赖任何第三方执行引擎实现，也不执行数据访问；
- 全量 Maven 测试、打包和 `DEV_PROGRESS.md` 更新完成。
