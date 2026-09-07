# Phase 6 Engine API 实施计划

> 本计划在 `codex/phase6-native-execution` 隔离工作区执行。每个任务先写测试，再写最小实现，并保持独立中文 Conventional Commit。

## 任务 1：建立 API 模型和 execution 模块

**文件：** `datacraft-server/pom.xml`、`datacraft-server/datacraft-execution/pom.xml`、`datacraft-api/src/main/java/io/datacraft/api/execution/*`

- 添加 `EngineDeploymentMode`、`EngineHealthStatus`；
- 添加能力、元数据、健康和 API 响应 record；
- 创建独立 `datacraft-execution` Maven 模块，只依赖 `datacraft-api`、`datacraft-common` 和 Spring Web；
- 先添加模型和模块装配测试，确认模块可被 reactor 构建。

## 任务 2：实现 ExecutionEngine SPI 和 EngineRegistry

**文件：** `datacraft-execution/src/main/java/io/datacraft/execution/*`

- 定义 `ExecutionEngine` SPI；
- 实现 `EngineRegistry` 的构造注入、按代码查找、重复代码拒绝和代码排序；
- 为未知引擎定义稳定异常；
- 先完成 Registry 单元测试，再实现生产代码。

## 任务 3：注册 Native 引擎

**文件：** `datacraft-execution/src/main/java/io/datacraft/execution/nativeengine/*`

- 实现 `NativeExecutionEngine` Spring Bean；
- 声明 `NATIVE`、`EMBEDDED`、启用状态和 Phase 6 能力；
- 健康检查返回 `UP`，不连接业务数据源；
- 增加 Native 元数据和健康检查测试。

## 任务 4：提供引擎发现 API

**文件：** `datacraft-execution/src/main/java/io/datacraft/execution/web/*`

- 实现 `GET /api/v1/engines`；
- 将领域模型转换为 API 响应，不直接暴露实现对象；
- 添加 Web MVC 测试，覆盖匿名 401、认证成功返回 Native、稳定字段和健康信息。

## 任务 5：验证、进度和收尾

- 运行 execution 模块测试、Maven 全量测试和 package；
- 检查依赖边界，确认 Pipeline 核心没有引入 execution 运行时实现；
- 更新 `DEV_PROGRESS.md` 与 Phase 6/7 状态；
- 使用中文 Conventional Commit 提交每个原子任务；
- 完成前执行验证和代码审查检查，保留隔离分支和 worktree，等待后续集成。
