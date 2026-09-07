# ADR 0003: DataReference

**Date:** 2026-09-07  **Status:** Accepted

## Context

跨引擎 Stage 之间传递数据时，直接在 JVM 内存中使用 `List` 承载大规模数据会导致 OOM 和 GC 压力，且无法支持不同引擎在不同进程甚至不同节点上执行。

## Decision

引入 `DataReference` 模型作为跨 Stage 数据传递的唯一载体。`DataReference` 通过 URI 和元数据描述数据位置，而非直接持有数据本身。

- `DATABASE_TABLE` 类型指向数据库表
- `DATABASE_QUERY` 类型标记 `MATERIALIZATION_REQUIRED`，表示需要中间物化
- `FILE`、`OBJECT_STORAGE`、`KAFKA_TOPIC` 等类型为后续阶段预留

## Rationale

- 数据量不再受 JVM 堆限制
- 跨引擎 Stage 解耦，各引擎独立消费数据
- 物化策略可按需选择（临时表、文件、对象存储）

## Consequences

- Planner 必须为每个 Stage 正确生成输入/输出 DataReference
- Native Runtime 当前仅支持单 Stage 线性执行，多 Stage 物化在 V1.3 Hybrid 阶段实现
