# Phase 8 Native Runtime 实施计划

## 任务 1：建立 JDBC 连接端口

- 在 datasource 模块定义 `JdbcConnection` 和 `JdbcConnectionProvider`。
- 用现有数据源仓储、密钥加密服务和 JDBC URL 构建器实现连接提供者。
- 增加未知数据源和凭据不泄露测试。

## 任务 2：实现 Native JDBC 执行器

- 调用 Phase 7 Planner，限制首版执行单个 Native Stage。
- 解析数据库源、过滤器和数据库汇节点配置。
- 生成带标识符引用的源查询和目标 PreparedStatement。
- 使用 forward-only ResultSet、fetch size 和 500 行批处理完成流式搬运。
- 覆盖 APPEND/TRUNCATE、提交、回滚和执行结果统计。

## 任务 3：验证与交付

- 先运行 Native Runtime 定向测试，再运行完整 Maven 测试和 package 构建。
- 执行 `git diff --check`，扫描执行模块的第三方引擎依赖和凭据泄露风险。
- 更新 `DEV_PROGRESS.md`，将下一阶段切换为 Quality。
