# DataCraft

DataCraft 是一个通过可视化 Pipeline 统一编排多种数据处理引擎的数据集成与数据治理平台。当前已完成 V0.3 / Phase 4：平台认证、角色菜单、控制台框架、JDBC 数据源管理、数据库资产浏览和 Pipeline 控制面模型。

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

Phase 2 数据源 API：

```text
GET    /api/v1/datasources
POST   /api/v1/datasources
GET    /api/v1/datasources/{id}
PUT    /api/v1/datasources/{id}
DELETE /api/v1/datasources/{id}
POST   /api/v1/datasources/{id}/test
```

当前支持 PostgreSQL 和 MySQL。编辑数据源时密码留空表示保留原密码；任何 API 响应都不会返回密码或密文。

Phase 3 元数据 API：

```text
POST /api/v1/datasources/{id}/metadata/sync
GET  /api/v1/datasets?datasourceId=&schemaName=&keyword=
GET  /api/v1/datasets/{id}
```

元数据同步使用 JDBC `DatabaseMetaData` 采集 Schema、Table、Field、主键和备注，并通过只读数据库方言查询获取估算行数。同步采用整库快照替换；当前仅支持 PostgreSQL 和 MySQL 的表结构元数据，不采集 View、Index、Constraint 等扩展对象。

Phase 4 Pipeline Model API：

```text
GET    /api/v1/pipelines
POST   /api/v1/pipelines
GET    /api/v1/pipelines/{id}
PUT    /api/v1/pipelines/{id}
DELETE /api/v1/pipelines/{id}
GET    /api/v1/node-types
GET    /api/v1/node-types/{type}
```

Pipeline 当前由控制面维护名称、版本、状态、执行策略、节点和边；创建/更新会校验节点类型、节点引用和 DAG 无环性。节点注册表只提供元数据，不创建或调用 DataX、Camel、SeaTunnel、Flink 等执行引擎；可视化编辑器和执行规划属于后续阶段。

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

## 当前边界

Phase 4 已完成认证、角色菜单、登录态、系统 Layout、数据源管理、元数据资产浏览和 Pipeline 控制面模型。下一阶段按 `DESIGN.md` 进入 Phase 5 Pipeline Editor，使用 Vue Flow 构建可视化编辑器；执行规划、执行引擎和质量规则仍保持在后续阶段。
