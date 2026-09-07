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
