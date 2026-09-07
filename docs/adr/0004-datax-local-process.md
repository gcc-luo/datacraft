# ADR 0004: DataX Local Process

**Date:** 2026-09-07  **Status:** Provisional

## Context

Apache DataX 是成熟的离线批量同步引擎，适合百万级以上数据量的数据库间迁移。DataCraft 需要在 V1.2 阶段接入 DataX 作为批量同步执行引擎。

## Decision

DataX 以 **Local Process** 方式接入。DataCraft 通过 `datacraft-engine-datax` 独立模块实现 `ExecutionEngine` SPI，内部通过进程管理器调用本地安装的 DataX Python 脚本。

- DataX Home 路径、Python 路径、临时任务目录通过引擎配置管理
- DataCraft 负责生成 DataX JSON 配置、启动进程、解析日志、收集指标
- 取消操作通过进程终止实现

## Rationale

- Local Process 模式无需额外集群，适合中小团队
- DataX 自身成熟的并行读写在批量场景下优于 Native JDBC 流式
- 通过 SPI 接入保持 Pipeline 核心解耦

## Consequences

- 部署环境需预装 DataX 和 Python
- docker-compose 在 V1.2 阶段增加 datax 容器
- 该 ADR 将在 V1.2 实现时细化
