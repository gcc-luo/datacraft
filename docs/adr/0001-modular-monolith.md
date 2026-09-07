# ADR 0001: Modular Monolith

**Date:** 2026-09-07  **Status:** Accepted

## Context

DataCraft 需要在开发效率和架构可扩展性之间取得平衡。微服务架构虽然为大型团队提供了独立部署能力，但在项目初期会引入显著的运维复杂度、网络开销和分布式事务难题。

## Decision

采用 **Modular Monolith** 架构。后端以 Maven 多模块形式组织，每个业务领域（datasource、metadata、pipeline、execution、auth）拥有独立模块和清晰的包边界，但最终打包为单一 Spring Boot 应用。

## Rationale

- 单一部署单元降低运维成本，适合中小团队
- 模块间通过接口/DTO 通信，保留未来拆分能力
- Flyway 统一管理数据库迁移，避免分布式 schema 问题
- 单体事务覆盖跨模块操作，无需 Saga 或 TCC

## Consequences

- 模块间依赖必须单向（bootstrap → api → 业务模块 → common）
- 禁止业务模块间循环依赖
- 后续如需拆分，模块边界已清晰定义
