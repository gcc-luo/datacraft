# Phase 7 Execution Planner 实施计划

> 在 `codex/phase7-execution-planner` 隔离工作区执行。每个任务先写测试，再写最小实现，并使用中文 Conventional Commit。

## 任务 1：定义执行计划和 DataReference 模型

**文件：** `datacraft-server/datacraft-execution/src/main/java/io/datacraft/execution/domain/*`

- 添加 `DataReferenceType`、`DataReference`、`ExecutionStagePlan`、`ExecutionPlan`；
- 为集合和元数据提供不可变保护；
- 增加模型不变性测试。

## 任务 2：实现拓扑规划和引擎选择

**文件：** `datacraft-server/datacraft-execution/src/main/java/io/datacraft/execution/application/*`

- 实现 `ExecutionPlanner`；
- 复用 PipelineValidator 和 NodeRegistry；
- 实现引擎选择优先级、能力检查、Registry 检查和稳定拓扑序；
- 先完成规划器红灯测试，再实现生产代码。

## 任务 3：实现数据库节点引用和跨 Stage 引用

**文件：** `datacraft-server/datacraft-execution/src/main/java/io/datacraft/execution/application/ExecutionPlanner.java`

- 解析源/目标节点 JSON 配置；
- 仅提取 datasourceId、tableName、writeMode 等非敏感字段；
- 生成数据库表引用和后续 Materialization 所需的逻辑查询引用；
- 覆盖非法 JSON、缺少字段和跨引擎 Stage 测试。

## 任务 4：验证并更新进度

- 运行 execution 模块测试和 Maven 全量测试；
- 运行全量 package；
- 检查 Planner 未引入第三方 Engine 或 Runtime；
- 更新 `DEV_PROGRESS.md` 为 Phase 7 完成、Phase 8 Native Runtime 下一阶段；
- 完成前执行验证和代码审查检查，保留分支和 worktree。
