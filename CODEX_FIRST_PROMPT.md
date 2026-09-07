# DataCraft — Codex 第一次执行提示词

你接手的是一个完全空的 DataCraft Git 仓库。

在做任何代码修改前，请先完整阅读：
- DESIGN.md
- AGENTS.md
- DEV_PROGRESS.md

本次只执行 **V0.1 / Phase 0 — Repository & Engineering**。

不要开始 Phase 1。
不要实现 Datasource、Metadata、Pipeline、Quality、DataX、Camel、SeaTunnel、Flink。

## 目标
把空仓库初始化成一个可持续开发、可编译、可运行的 DataCraft 工程骨架。

### Backend
使用 Java 21、Spring Boot 3、Maven、MyBatis-Plus、PostgreSQL、Redis、Flyway、Spring Security、SpringDoc、JUnit 5。

要求：
1. 建立 Maven 多模块结构。
2. datacraft-bootstrap 作为 Spring Boot 启动模块。
3. 各业务模块只建骨架，不提前写业务逻辑。
4. 提供 application.yml / application-local.yml。
5. 加入基础健康检查。
6. 加入 Flyway 初始化 migration。
7. 本地依赖 Docker Compose 的 PostgreSQL / Redis。

### Frontend
使用 Vue 3、TypeScript、Vite、Element Plus、Pinia、Vue Router、Axios。

要求：
1. 创建 datacraft-web。
2. 建立 src/api、components、layouts、router、stores、styles、types、utils、views 等目录。
3. 创建最基础 DataCraft Layout。
4. 暂不实现真实业务页面。
5. npm run build 必须通过。

### DevOps
创建：
- docker-compose.yml
- .gitignore
- README.md
- 必要开发脚本

README 至少写：
- 项目简介
- 环境要求
- 本地启动步骤
- 后端启动
- 前端启动
- Docker Compose
- 测试方法

## 执行流程
1. 检查仓库内容。
2. 阅读 DESIGN.md、AGENTS.md、DEV_PROGRESS.md。
3. 先输出简短 Phase 0 实施计划。
4. 列出预计创建的主要目录和文件。
5. 再开始编码。

## 完成标准
实际执行并验证：
- mvn test
- mvn package（如适用）
- npm install
- npm run build

如果失败，修复后重跑，不能以“理论上可运行”为完成标准。

最后更新 DEV_PROGRESS.md，并汇报：
1. 创建的主要模块
2. 后端构建/测试结果
3. 前端构建结果
4. Docker Compose 内容
5. 尚未实现的功能
6. 下一步应进入哪个 Phase

特别注意：本次只做 V0.1 / Phase 0。
