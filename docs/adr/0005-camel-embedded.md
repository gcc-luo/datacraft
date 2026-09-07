# ADR 0005: Camel Embedded

**Date:** 2026-09-07  **Status:** Provisional

## Context

Apache Camel 提供丰富的企业连接器（HTTP、SFTP、Kafka、JMS 等），DataCraft 需要在 V1.5 阶段接入 Camel 作为企业连接器引擎。

## Decision

Camel 以 **Embedded** 方式接入。DataCraft 通过 `datacraft-engine-camel` 独立模块在 JVM 内嵌入 CamelContext，将 Camel Route 作为 `ExecutionEngine` SPI 实现的执行单元。

- CamelContext 由 DataCraft Spring 容器管理生命周期
- Route 通过编程式定义，不依赖 XML DSL
- HTTP、SFTP、Kafka 组件按需启用

## Rationale

- Embedded 模式无需独立 Camel 容器，部署简单
- Camel 与 Native 引擎在同一 JVM 内可直接通过 DataReference 交换数据
- 通过 SPI 接入保持 Pipeline 核心解耦

## Consequences

- `datacraft-engine-camel` 模块依赖 camel-core，但不被 Pipeline 模块依赖
- 线程池和路由超时通过引擎配置管理
- 该 ADR 将在 V1.5 实现时细化
