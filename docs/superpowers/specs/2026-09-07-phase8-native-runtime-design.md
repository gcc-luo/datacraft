# Phase 8 Native Runtime 设计

## 目标

在不突破 Control Plane / Execution Plane 边界的前提下，为 DataCraft 增加第一条可执行的原生 JDBC 运行路径：

```text
DATABASE_SOURCE -> FILTER（可选） -> DATABASE_SINK
```

该路径用于验证 Pipeline 图、Planner、Datasource 连接端口和 Native Engine Runtime 能够协同执行 MySQL 到 PostgreSQL 等数据库间的数据搬运。

## 范围

- 通过 `JdbcConnectionProvider` 获取已配置的数据源连接。
- 数据源节点按表读取，使用 JDBC forward-only `ResultSet` 和固定 fetch size。
- Filter 节点转换为源端 SQL `WHERE` 条件，优先进行 SQL Pushdown。
- Sink 节点使用 PreparedStatement 批量写入，默认批大小为 500。
- 以源列顺序和目标列顺序进行位置映射，首版不引入复杂表达式转换。
- 支持 `APPEND` 和 `TRUNCATE` 两种写入模式。
- 写入过程使用事务，成功提交，异常回滚，并恢复连接原有 auto-commit 状态。

## 安全与边界

- 密码继续以密文保存在数据源领域对象中；只有连接创建时在内存中短暂解密。
- 连接提供者和执行器不得记录、返回或拼接密码到错误信息。
- 表名和标识符按目标数据库类型进行引用；Filter 表达式拒绝明显的多语句和注释片段。
- Native Runtime 只能通过 Datasource 端口获取连接，Execution 模块不直接访问数据源 Mapper 或密钥存储。
- 不引入 DataX、Camel、SeaTunnel、Flink，也不在 Pipeline 核心模块中绑定第三方引擎。
- 首版不增加执行记录、调度、质量报告或 HTTP 执行接口，这些属于后续阶段。

## 验收标准

1. Planner 生成的单 Native Stage 可以执行一条线性数据库源到数据库汇 Pipeline。
2. Filter 条件出现在源查询的 `WHERE` 子句中，源结果以受控批次流向目标，不构造全量 Java `List`。
3. 批量写入使用 PreparedStatement，批大小不超过 500，并在成功时提交。
4. 写入失败时执行回滚，且连接 auto-commit 状态得到恢复。
5. 数据源连接异常、Pipeline 图异常、缺失节点配置等情况产生不含凭据的领域异常。
6. 通过模块测试、全量 Maven 测试和 package 构建。
