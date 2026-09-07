# ADR 0002: Engine SPI

**Date:** 2026-09-07  **Status:** Accepted

## Context

DataCraft 作为 Control Plane 需要编排多种执行引擎（Native、DataX、Camel、SeaTunnel、Flink），但 Pipeline 核心代码不得直接依赖任何第三方引擎 API。

## Decision

定义 `ExecutionEngine` SPI 接口作为 Control Plane 与 Execution Plane 之间的唯一契约。所有引擎实现该接口，通过 `EngineRegistry` 动态注册和发现。

- `datacraft-engine-api` 模块包含 SPI 接口、领域模型（`EngineMetadata`、`EngineHealth`、`ExecutionPlan`、`DataReference`）和应用层（`EngineRegistry`、`ExecutionPlanner`）
- `datacraft-engine-native` 模块包含 Native 引擎实现和 REST 控制器
- Pipeline 模块只依赖 `datacraft-engine-api`，禁止依赖任何具体引擎实现

## Rationale

- SPI 隔离使得新引擎接入不影响 Pipeline 核心
- 引擎实现可独立编译和测试
- 运行时通过 Spring 依赖注入自动发现引擎 Bean

## Consequences

- 新增引擎只需实现 `ExecutionEngine` 接口并注册为 Spring Bean
- `EngineRegistry` 使用 TreeMap 保证引擎列表稳定排序
- 引擎编码唯一性由 `EngineRegistry` 构造时校验
