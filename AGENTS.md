# DataCraft AI Development Rules

所有 AI 编程代理在修改代码前必须先阅读：
1. DESIGN.md
2. AGENTS.md
3. DEV_PROGRESS.md

## 核心规则
- DataCraft 是 Control Plane；第三方引擎是 Execution Plane。
- Pipeline、Node、Edge、Execution、Schedule、QualityResult 由 DataCraft 自己维护。
- 第三方引擎必须通过 ExecutionEngine SPI 接入。
- Node、Connector、Engine 必须分离。
- Pipeline 核心模块不得直接依赖 DataX、Camel、SeaTunnel、Flink。
- 跨 Engine Stage 使用 DataReference + Materialization，不用 Java List 传大规模数据。
- SQL Pushdown First。
- 保持 Modular Monolith，不擅自微服务化。
- 数据源密码不得明文存储、返回或写日志。

## 技术栈
Backend: Java 21, Spring Boot 3, Maven, MyBatis-Plus, PostgreSQL, Redis, Flyway, Quartz, Spring Security + JWT, SpringDoc, JUnit 5.
Frontend: Vue 3, TypeScript, Vite, Element Plus, Pinia, Vue Router, Vue Flow, Axios, ECharts, Monaco Editor.

## 编码规则
- Controller 不直接调用 Mapper。
- Entity 不直接作为 API Response。
- 数据库结构修改必须提供 Flyway migration。
- Engine 代码放独立模块。
- Pipeline / Planner / Registry 必须有测试。
- 不删除测试来规避失败。
- 不提前实现后续 Phase。
- 每次任务结束必须构建、测试、更新 DEV_PROGRESS.md。

## Git 提交规范
- 使用 Conventional Commits 格式：`<type>(<scope>): <subject>`。
- `type` 保留英文小写，允许：`feat`、`fix`、`docs`、`style`、`refactor`、`perf`、`test`、`chore`、`ci`、`revert`。
- `scope` 使用中文模块名，可选，例如 `认证`、`Pipeline`、`数据源`、`前端`。
- `subject` 使用中文动宾短语，不超过 50 个汉字，不以句号结尾；中文与英文/数字之间保留一个空格。
- 提交正文使用中文，说明变更背景、技术方案和影响范围；每行不超过 72 个字符。
- 一个提交只完成一项原子变更，避免把无关修改混在同一个提交中。
- 涉及数据库结构、公共 API 或配置格式的不兼容变更，必须在 footer 标注 `BREAKING CHANGE`，并写明迁移或升级方法。
- 有关联事项时，在 footer 使用 `Closes #123`、`Refs #123` 等方式关联 Issue。

提交示例：

```text
feat(Pipeline): 添加 MySQL 到 PostgreSQL 的任务编排模型

背景：需要支持跨数据库 Pipeline 的控制面配置。
方案：新增源节点、目标节点及数据源引用配置。
影响范围：Pipeline 模型、前端编辑器。
```
