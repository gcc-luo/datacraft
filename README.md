# DataCraft

DataCraft 是一个通过可视化 Pipeline 统一编排多种数据处理引擎的数据集成与数据治理平台。当前已完成 V0.1 / Phase 2：平台认证、角色菜单、控制台框架和 JDBC 数据源管理。

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

Phase 2 已完成认证、角色菜单、登录态、系统 Layout 和数据源管理。元数据、Pipeline、质量、调度以及任何执行引擎将在后续 Phase 按 `DESIGN.md` 推进。
