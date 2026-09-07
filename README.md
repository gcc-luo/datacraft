# DataCraft

DataCraft 是一个通过可视化 Pipeline 统一编排多种数据处理引擎的数据集成与数据治理平台。

当前版本为 **V0.6 / Phase 9（Quality 已完成）**，已经具备平台认证、系统管理、数据源管理、数据库资产浏览、Pipeline 可视化编排、Native 数据库同步执行和数据质量检查能力。

## 环境要求

- JDK 21
- Maven 3.6.3+
- Node.js 22+
- npm 10+
- Docker Desktop（本地 PostgreSQL / Redis）

## 项目结构

```text
datacraft-server/
  datacraft-common/       通用基础模块
  datacraft-api/          HTTP API 边界模块
  datacraft-auth/         认证、JWT 和系统菜单
  datacraft-datasource/   JDBC 数据源、连接测试和凭据加密
  datacraft-metadata/     PostgreSQL/MySQL 元数据采集和数据资产查询
  datacraft-pipeline/     Pipeline、Node、Edge 模型、DAG 校验和节点元数据
  datacraft-engine-api/   ExecutionEngine SPI、执行规划、引擎注册表
  datacraft-engine-native/ Native JDBC 执行引擎和执行接口
  datacraft-quality/      数据质量规则、结果和异常样例
  datacraft-bootstrap/    Spring Boot 启动模块
datacraft-web/            Vue 3 前端
docker/                   容器初始化资源
docs/adr/                 架构决策记录
deploy/                   部署资源预留
scripts/                  工程脚本预留
```

## 本地启动

复制 `.env.example` 为 `.env`，修改管理员密码、JWT 密钥和数据源加密密钥后执行 `docker compose up -d`，再运行：

```shell
mvn spring-boot:run -pl datacraft-server/datacraft-bootstrap -am
```

后端健康检查：`http://localhost:8080/actuator/health`。

首次启动会由 Flyway 建立 `dc_` 平台表，并使用 `DATACRAFT_ADMIN_USERNAME` / `DATACRAFT_ADMIN_PASSWORD` 创建管理员。密码只以 BCrypt 哈希保存；数据源密码以 AES-256-GCM 密文保存，`DATACRAFT_DATASOURCE_ENCRYPTION_KEY` 解码后必须为 32 字节；缺少安全配置时应用会拒绝启动。

## 已实现功能

### 认证与平台基础

- 管理员初始化、登录、JWT 无状态会话和当前用户查询。
- 角色与菜单接口、登录态恢复、路由鉴权和响应式控制台布局。
- PostgreSQL、Redis、Flyway、统一异常处理和健康检查基础设施。

### 系统管理

管理员可在侧边栏“系统设置”下的二级菜单维护用户、角色和菜单目录：

- 用户新增、编辑、角色分配、启停状态管理和密码重置。
- 角色新增、编辑、启停状态管理和菜单授权。
- 菜单新增、编辑、层级调整、路由信息和排序维护。
- 所有管理接口由 `ADMIN` 角色保护；用户 API 响应不会返回密码或密码哈希。

系统管理 API：

```text
GET/POST/PUT /api/v1/system/admin/users
GET/POST/PUT /api/v1/system/admin/roles
GET/POST/PUT /api/v1/system/admin/menus
```

系统管理复用现有用户、角色、菜单及关联表，并通过 Flyway 增加系统管理二级菜单数据；普通登录用户继续通过 `/api/v1/system/menus` 获取其可见菜单。

前端页面路由：

```text
/system/users
/system/roles
/system/menus
```

### 数据源管理

支持 PostgreSQL 和 MySQL 数据源的新增、编辑、删除、查询和连接测试。数据源密码使用 AES-256-GCM 加密保存，API 响应不会返回密码或密文；编辑时密码留空表示保留原密码。

数据源 API：

```text
GET    /api/v1/datasources
POST   /api/v1/datasources
GET    /api/v1/datasources/{id}
PUT    /api/v1/datasources/{id}
DELETE /api/v1/datasources/{id}
POST   /api/v1/datasources/{id}/test
```

当前支持 PostgreSQL 和 MySQL。编辑数据源时密码留空表示保留原密码；任何 API 响应都不会返回密码或密文。

### 数据资产与元数据

元数据 API：

```text
POST /api/v1/datasources/{id}/metadata/sync
GET  /api/v1/datasets?datasourceId=&schemaName=&keyword=
GET  /api/v1/datasets/{id}
```

元数据同步使用 JDBC `DatabaseMetaData` 采集 Schema、Table、Field、主键和备注，并通过只读数据库方言查询获取估算行数。同步采用整库快照替换；当前仅支持 PostgreSQL 和 MySQL 的表结构元数据，不采集 View、Index、Constraint 等扩展对象。

前端提供数据源选择、Schema/表筛选、面包屑详情、字段搜索和同步操作。

### Pipeline 建模与可视化编排

Pipeline Model API：

```text
GET    /api/v1/pipelines
POST   /api/v1/pipelines
GET    /api/v1/pipelines/{id}
PUT    /api/v1/pipelines/{id}
DELETE /api/v1/pipelines/{id}
GET    /api/v1/node-types
GET    /api/v1/node-types/{type}
```

Pipeline 由控制面维护名称、版本、状态、执行策略、节点和边；创建/更新会校验节点类型、节点引用、悬空边、重复边、自环和 DAG 无环性。

- `/pipelines` 展示 Pipeline 列表，并支持新建、进入编辑器和删除。
- `/pipelines/new` 与 `/pipelines/{id}` 提供三栏 Vue Flow 编辑器：左侧元数据节点库，中间 DAG 画布，右侧管道/节点属性检查器。
- 编辑器支持数据库源、过滤、数据库目标及数据质量节点，建立连线，编辑节点配置 JSON、坐标和首选引擎，并保存图模型。

### 执行规划与 Native 执行

平台已经完成 ExecutionEngine SPI、Engine Registry、引擎能力声明、健康检查、ExecutionPlan、Stage 和 DataReference 模型。

执行 API：

```text
POST /api/v1/pipelines/{id}/validate
POST /api/v1/pipelines/{id}/plan
POST /api/v1/pipelines/{id}/execute
```

Native Engine 当前支持单个 Native Stage 的数据库源到数据库目标同步，包含：

- JDBC 源/目标连接。
- Filter 条件 SQL 下推。
- 前向读取和 500 行批量写入，不将全量数据加载到 JVM。
- `APPEND` 和 `TRUNCATE` 写入模式。
- 事务提交、回滚和失败恢复。

引擎管理 API：

```text
GET  /api/v1/engines
GET  /api/v1/engines/{code}
POST /api/v1/engines/{code}/test
PUT  /api/v1/engines/{code}
```

### 数据质量

质量检查 API：

```text
POST /api/v1/quality/checks
GET  /api/v1/quality/results
GET  /api/v1/quality/results/{id}
```

当前质量规则包括 `NULL_CHECK`、`UNIQUE_CHECK`、`RANGE_CHECK`、`REGEX_CHECK`、`LENGTH_CHECK`、`ENUM_CHECK` 和 `CUSTOM_SQL_CHECK`。结果记录总行数、异常行数、通过行数、通过率，并最多保存 1000 条异常样例。前端当前提供快速空值检查和结果查看。

质量检查支持 PostgreSQL/MySQL 标识符处理、正则方言、自定义 SQL 安全校验和参数绑定。

## 当前产品边界

当前版本可以完成“用户/角色/菜单配置 → 配置数据源 → 浏览数据资产 → 编排数据库 Pipeline → 校验/规划 → Native 执行 → 数据质量检查”的基础闭环。

以下能力尚未实现或尚未形成完整产品闭环：

- Quartz 定时调度、Cron 配置、执行历史、重试、取消和审计日志。
- DataX、Camel、SeaTunnel、Flink 执行引擎。
- HTTP、SFTP、Kafka、MQ 等非 JDBC 连接器。
- 多 Stage 跨引擎执行、Materialization 和临时数据清理。
- 完整任务中心监控；当前工作台中的部分执行数据仍为界面占位内容。
- 数据血缘、数据标准、影响分析和 AI 治理能力。

当前尚不支持通过 Native Runtime 执行包含质量节点的完整 Pipeline；质量规则可通过独立质量检查 API 执行，质量节点元数据已注册到 Pipeline 编辑器。

前端开发：

```shell
cd datacraft-web
npm install
npm run dev
```

## 构建与测试

```shell
mvn test
mvn package

cd datacraft-web
npm install
npm run build
```

## 后续计划

下一阶段为 **Phase 10 — Scheduler**，重点是 Quartz 调度、Cron 配置、执行历史、重试、取消和审计日志。
