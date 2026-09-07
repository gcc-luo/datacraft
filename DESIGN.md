# DataCraft 产品与技术总体设计文档

> 文档版本：V2.0 Design Blueprint  
> 产品名称：**DataCraft**  
> 产品定位：**面向中小团队的数据集成、数据处理、数据质量与数据治理平台**  
> 架构形态：**模块化单体（Modular Monolith）+ 可插拔执行引擎（Pluggable Execution Engines）**  
> 目标读者：产品经理、架构师、Java/Vue 工程师、Codex / Claude Code / Cursor / OpenCode 等 AI 编程工具  
> 核心原则：**DataCraft 管控制面，第三方引擎管执行面；平台掌握统一模型、统一 UI、统一编排、统一调度、统一日志、统一权限。**

---

# 1. 产品愿景

DataCraft 不是简单的数据治理后台，也不是 DataX、Camel、SeaTunnel 的管理壳。

DataCraft 的目标是成为一个统一的数据处理与治理平台：

```text
数据接入
   ↓
数据资产
   ↓
数据处理
   ↓
数据质量
   ↓
数据治理
   ↓
任务调度
   ↓
执行监控
   ↓
治理结果
```

用户不需要关心：

```text
这个节点是 JDBC 执行
这个同步任务是 DataX 执行
这个 SFTP 是 Camel 执行
这个批处理是 SeaTunnel 执行
```

用户只看到：

```text
DataCraft 组件
+
DataCraft Pipeline
+
DataCraft 任务
```

底层执行引擎由 DataCraft 自动选择，或由高级用户显式指定。

---

# 2. 产品核心价值

DataCraft V1~V4 的长期目标：

```text
连接数据
↓
拖拽组件
↓
配置规则
↓
运行
↓
发现问题
↓
治理数据
↓
输出结果
↓
自动调度
```

第一阶段重点解决：

- 数据库接入
- 元数据采集
- Pipeline 编排
- 数据处理
- 数据质量
- 批量同步
- 任务调度
- 执行日志
- 治理结果

第二阶段逐步扩展：

- Camel 企业连接器
- DataX 批量同步
- SeaTunnel
- 文件 / API / MQ
- 数据血缘
- 数据标准
- 影响分析

第三阶段扩展：

- Flink 实时处理
- Kafka
- 实时规则
- 实时质量

第四阶段扩展：

- AI 生成 Pipeline
- AI 生成治理规则
- AI 根因分析
- AI 数据治理 Agent

---

# 3. 核心架构原则

## 3.1 Pipeline First

Pipeline 是 DataCraft 最重要的核心领域。

所有数据能力最终都应该可以被 Pipeline 调用。

---

## 3.2 Control Plane / Execution Plane 分离

DataCraft：

```text
Pipeline
Node
Edge
Execution
Schedule
Quality
Metadata
Permission
Audit
```

属于：

> Control Plane

DataX / Camel / SeaTunnel / Flink：

> Execution Plane

DataCraft 不重写成熟引擎的核心能力。

---

## 3.3 Node ≠ Engine

例如：

```text
MySQL
SFTP
HTTP
Kafka
NullCheck
Masking
```

属于：

> Node / Connector

而：

```text
Native
DataX
Camel
SeaTunnel
Flink
```

属于：

> Execution Engine

二者必须分离。

---

## 3.4 Metadata Driven

前端组件、配置面板、能力描述尽可能由后端元数据驱动。

例如：

```http
GET /api/v1/node-types
```

返回：

```json
{
  "type": "SFTP_SOURCE",
  "name": "SFTP",
  "category": "SOURCE",
  "supportedEngines": ["CAMEL", "SEATUNNEL"],
  "configSchema": {}
}
```

前端不写死所有组件配置。

---

## 3.5 SQL Pushdown First

对于数据库处理：

> 能在数据库中执行，就不要将大规模数据加载进 JVM。

优先：

```text
SQL
View
Temporary Table
CTE
```

---

## 3.6 Engine Adapter

所有第三方引擎都通过 DataCraft Adapter 接入。

禁止 Pipeline 核心代码直接依赖：

```text
DataX API
Camel API
SeaTunnel API
Flink API
```

---

## 3.7 Modular Monolith First

V1~V2 使用模块化单体。

部署：

```text
一个 Java 服务
一个 Web 前端
PostgreSQL
Redis
```

不因“以后可能扩展”而提前微服务化。

---

# 4. 总体架构

```text
┌──────────────────────────────────────────────┐
│                DataCraft Web                 │
│ Vue3 + TypeScript + Element Plus + Vue Flow │
└──────────────────────┬───────────────────────┘
                       │ REST / SSE / WS
                       ▼
┌──────────────────────────────────────────────┐
│               DataCraft Server               │
│          Spring Boot 3 + Java 21             │
│                                              │
│  ┌────────────────────────────────────────┐  │
│  │              Control Plane             │  │
│  │                                        │  │
│  │ Datasource / Metadata / Pipeline       │  │
│  │ Quality / Scheduler / Security / Audit │  │
│  └──────────────────┬─────────────────────┘  │
│                     │                        │
│              Execution Planner               │
│                     │                        │
│      ┌──────────────┼──────────────┐         │
│      ▼              ▼              ▼         │
│ Native Engine    DataX Engine    Camel Engine│
│      │              │              │         │
│ JDBC/SQL        Local Process   CamelContext │
└──────┼──────────────┼──────────────┼─────────┘
       │              │              │
       ▼              ▼              ▼
 External DB       DataX Job       HTTP/SFTP/MQ
```

未来：

```text
SeaTunnel Engine
Flink Engine
Spark Engine
Python Engine
```

---

# 5. 技术栈

## 5.1 后端

| 类型 | 技术 |
|---|---|
| JDK | Java 21 |
| Web | Spring Boot 3.x |
| ORM | MyBatis-Plus |
| 数据库 | PostgreSQL |
| 缓存 | Redis |
| 调度 | Quartz |
| 安全 | Spring Security + JWT |
| API | REST |
| 实时事件 | SSE，后期可选 WebSocket |
| 数据库迁移 | Flyway |
| OpenAPI | SpringDoc |
| JSON | Jackson |
| 构建 | Maven |
| Test | JUnit 5 + Mockito |
| 连接池 | HikariCP |

---

## 5.2 前端

| 类型 | 技术 |
|---|---|
| 框架 | Vue 3 |
| 语言 | TypeScript |
| 构建 | Vite |
| UI | Element Plus |
| 状态 | Pinia |
| 路由 | Vue Router |
| HTTP | Axios |
| DAG | Vue Flow |
| Chart | ECharts |
| SQL 编辑 | Monaco Editor |
| 样式 | SCSS |

---

# 6. Maven 模块设计

```text
datacraft/
│
├── datacraft-server/
│   │
│   ├── datacraft-bootstrap/
│   ├── datacraft-common/
│   ├── datacraft-api/
│   ├── datacraft-security/
│   ├── datacraft-system/
│   ├── datacraft-datasource/
│   ├── datacraft-metadata/
│   ├── datacraft-quality/
│   ├── datacraft-scheduler/
│   ├── datacraft-pipeline/
│   │
│   ├── datacraft-node-api/
│   ├── datacraft-connector-api/
│   ├── datacraft-engine-api/
│   │
│   ├── datacraft-engine-native/
│   ├── datacraft-engine-datax/
│   ├── datacraft-engine-camel/
│   │
│   └── datacraft-plugin-runtime/
│
├── datacraft-web/
│
├── docs/
├── docker/
├── deploy/
├── scripts/
├── AGENTS.md
├── DESIGN.md
├── README.md
└── pom.xml
```

---

# 7. 核心领域模型

```text
Workspace
 ├── User
 ├── Datasource
 ├── Dataset
 │    └── Field
 ├── Pipeline
 │    ├── Node
 │    └── Edge
 ├── QualityRule
 ├── Schedule
 ├── Execution
 │    ├── ExecutionStage
 │    └── NodeExecution
 ├── Engine
 └── AuditLog
```

---

# 8. Pipeline 模型

## dc_pipeline

```text
id
workspace_id
name
description
status
version
graph_json
execution_strategy
created_by
created_at
updated_at
```

execution_strategy：

```text
AUTO
NATIVE
DATAX
CAMEL
SEATUNNEL
```

AUTO 是默认。

---

## dc_pipeline_node

```text
id
pipeline_id
node_key
node_type
node_name
x
y
config_json
preferred_engine
created_at
updated_at
```

---

## dc_pipeline_edge

```text
id
pipeline_id
source_node_key
target_node_key
source_port
target_port
condition_json
created_at
```

---

# 9. Node API

```java
public interface DataProcessNode {

    String type();

    NodeMetadata metadata();

    ValidationResult validate(NodeDefinition definition);
}
```

注意：

> Node 不直接决定如何执行。

---

# 10. Node Metadata

```java
public class NodeMetadata {

    private String type;

    private String name;

    private NodeCategory category;

    private String icon;

    private List<String> supportedEngines;

    private String defaultEngine;

    private ConfigSchema configSchema;
}
```

NodeCategory：

```text
SOURCE
TRANSFORM
QUALITY
GOVERNANCE
SINK
UTILITY
```

---

# 11. Engine SPI

```java
public interface ExecutionEngine {

    String engineType();

    EngineMetadata metadata();

    EngineHealth healthCheck();

    ValidationResult validate(ExecutionPlan plan);

    EngineJob compile(ExecutionPlan plan);

    EngineExecution submit(EngineJob job);

    EngineExecutionStatus getStatus(String engineExecutionId);

    void cancel(String engineExecutionId);

    List<EngineLog> logs(String engineExecutionId);
}
```

---

# 12. Engine Registry

```java
@Component
public class EngineRegistry {

    private final Map<String, ExecutionEngine> engines;

    public ExecutionEngine get(String engineType) {
        return engines.get(engineType);
    }

    public List<ExecutionEngine> list() {
        return List.copyOf(engines.values());
    }
}
```

启动：

```text
NativeExecutionEngine
        ↓
register("NATIVE")

DataXExecutionEngine
        ↓
register("DATAX")

CamelExecutionEngine
        ↓
register("CAMEL")
```

---

# 13. Connector API

```java
public interface ConnectorProvider {

    String type();

    ConnectorMetadata metadata();

    ConnectionTestResult test(Map<String, Object> config);
}
```

例如：

```text
MYSQL
POSTGRESQL
ORACLE
SQLSERVER
HTTP
FTP
SFTP
KAFKA
MQTT
EMAIL
```

---

# 14. Connector 与 Engine 的关系

例如：

```text
MYSQL

Supported:
NATIVE
DATAX
SEATUNNEL
```

SFTP：

```text
Supported:
CAMEL
SEATUNNEL
```

HTTP：

```text
Supported:
CAMEL
NATIVE
```

---

# 15. Engine Capability

```java
public class EngineCapability {

    private boolean batch;

    private boolean stream;

    private Set<String> sourceTypes;

    private Set<String> sinkTypes;

    private Set<String> transformTypes;
}
```

API：

```http
GET /api/v1/engines
```

示例：

```json
{
  "code": "DATAX",
  "name": "Apache DataX",
  "deploymentMode": "LOCAL_PROCESS",
  "enabled": true,
  "capability": {
    "batch": true,
    "stream": false
  }
}
```

---

# 16. Engine Deployment Mode

统一：

```text
EMBEDDED
LOCAL_PROCESS
REMOTE_SERVICE
```

对应：

| Engine | Mode |
|---|---|
| Native | EMBEDDED |
| Camel | EMBEDDED |
| DataX | LOCAL_PROCESS |
| SeaTunnel | LOCAL_PROCESS / REMOTE_SERVICE |
| Flink | REMOTE_SERVICE |

---

# 17. Engine 数据表

## dc_engine

```text
id
engine_code
engine_name
engine_type
deployment_mode
enabled
version
config_json
status
last_health_check
created_at
updated_at
```

---

# 18. Execution Planner

Pipeline 不直接进入 NodeExecutor。

正确流程：

```text
Pipeline
↓
PipelineValidator
↓
ExecutionPlanner
↓
ExecutionPlan
↓
ExecutionStage[]
↓
EngineCompiler
↓
ExecutionEngine
```

---

# 19. ExecutionPlan

```java
public class ExecutionPlan {

    private Long pipelineId;

    private List<ExecutionStagePlan> stages;

    private Map<String, Object> runtimeVariables;
}
```

---

# 20. Execution Stage

一个 Pipeline 可以拆成多个 Stage。

例如：

```text
MySQL
↓
Data Sync
↓
Null Check
↓
Webhook
```

Planner 可能产生：

```text
Stage 1
DATAX

Stage 2
NATIVE

Stage 3
CAMEL
```

---

# 21. dc_execution_stage

```text
id
execution_id
stage_index
engine_code
status
engine_job_id
started_at
finished_at
input_rows
output_rows
error_rows
error_message
engine_metadata_json
created_at
```

---

# 22. DataReference

跨 Node / Stage 不允许默认使用 Java List 传递大数据。

定义：

```java
public class DataReference {

    private DataReferenceType type;

    private String uri;

    private Map<String, Object> metadata;
}
```

DataReferenceType：

```text
DATABASE_TABLE
DATABASE_QUERY
FILE
OBJECT_STORAGE
KAFKA_TOPIC
MEMORY
NONE
```

---

# 23. Materialization

跨 Engine 时：

```text
Stage A
↓
Materialization
↓
Stage B
```

例如：

```text
DataX
↓
PostgreSQL Temp Table
↓
Native Quality
```

未来可以：

```text
DataX
↓
MinIO
↓
Camel
```

---

# 24. Native Engine

V1 默认内置。

能力：

```text
JDBC Source
JDBC Sink
SQL Transform
Filter
Field Mapping
Null Check
Unique Check
Range Check
Regex Check
Masking
Deduplicate
```

核心策略：

```text
SQL Pushdown First
```

---

# 25. DataX Engine

## 25.1 定位

主要用于：

```text
DB → DB
批量数据同步
大量数据迁移
```

---

## 25.2 集成方式

DataX 第一阶段：

```text
LOCAL_PROCESS
```

DataCraft：

```text
ExecutionPlan
↓
DataXCompiler
↓
DataX Job JSON
↓
ProcessBuilder
↓
datax.py
```

---

## 25.3 DataX 配置

```yaml
datacraft:
  engines:
    datax:
      enabled: true
      home: /opt/datax
      python: /usr/bin/python3
```

---

## 25.4 DataX Adapter

模块：

```text
datacraft-engine-datax
```

内部：

```text
DataXExecutionEngine
DataXCompiler
DataXJobBuilder
DataXProcessManager
DataXLogParser
DataXMetricsParser
```

---

# 26. Camel Engine

## 26.1 定位

主要用于：

```text
HTTP
REST
SOAP
FTP
SFTP
Kafka
JMS
RabbitMQ
MQTT
Mail
File
企业应用集成
```

---

## 26.2 集成方式

第一阶段：

```text
EMBEDDED
```

DataCraft Server 内运行：

```text
CamelContext
```

---

## 26.3 Camel Runtime Manager

```text
CamelRuntimeManager

├── startRoute
├── stopRoute
├── removeRoute
├── routeStatus
└── routeLogs
```

禁止：

> 每个 Node 创建一个 CamelContext。

统一 Managed CamelContext。

---

## 26.4 Camel Compiler

```text
ExecutionStage
↓
CamelCompiler
↓
RouteDefinition
↓
CamelContext
```

---

# 27. SeaTunnel Engine

V2 引入。

定位：

```text
大量数据集成
批处理
流式数据同步
CDC
```

实现方式：

```text
LOCAL_PROCESS
+
REMOTE_SERVICE
```

模块：

```text
datacraft-engine-seatunnel
```

---

# 28. Flink Engine

V3 引入。

定位：

```text
流式 Pipeline
实时治理
实时质量规则
Kafka Stream
事件处理
```

执行方式：

```text
REMOTE_SERVICE
```

DataCraft 只负责：

```text
Job Submit
Status
Cancel
Logs
Metrics
```

---

# 29. Engine 自动选择

默认：

```text
AUTO
```

Planner 判断：

```text
Node Capability
+
Data Volume
+
Pipeline Type
+
Engine Availability
+
User Preference
```

示例：

```text
MySQL → PostgreSQL

small
→ Native

large batch
→ DataX

large distributed
→ SeaTunnel
```

---

# 30. 用户界面原则

普通用户：

> 不需要理解底层 Engine。

高级用户：

> 可以查看或指定 Engine。

---

# 31. Pipeline Editor

布局：

```text
┌───────────────────────────────────────────────┐
│ DataCraft / Customer Governance              │
│                       保存  校验  运行  发布  │
├───────────┬──────────────────────┬────────────┤
│ 组件库     │       Canvas         │ 节点配置   │
│           │                      │            │
│ 数据源     │ [MySQL]              │ 名称       │
│ MySQL     │    ↓                 │ 数据源      │
│ HTTP      │ [Mapping]            │ Schema      │
│ SFTP      │    ↓                 │ Table       │
│           │ [NullCheck]          │            │
│ 处理       │    ↓                 │ 高级        │
│ Filter    │ [Masking]            │ Engine:AUTO │
│ Mapping   │    ↓                 │            │
│ SQL       │ [PostgreSQL]         │            │
└───────────┴──────────────────────┴────────────┘
```

---

# 32. Node Library

V1：

```text
数据源
├── MySQL
└── PostgreSQL

处理
├── Filter
├── SQL
├── Field Mapping
└── Type Convert

质量
├── Null Check
├── Unique Check
├── Range Check
└── Regex Check

治理
├── Masking
├── Trim
├── Replace
└── Deduplicate

输出
├── MySQL
└── PostgreSQL
```

---

# 33. V1.5 Node Library

增加：

```text
HTTP
FTP
SFTP
Kafka
RabbitMQ
MQTT
Mail
File
```

主要由 Camel 提供能力。

---

# 34. Engine 管理页面

位置：

```text
系统管理
  ↓
执行引擎
```

界面：

```text
┌─────────────────────────────────────────────────┐
│ DataCraft Native                     运行中      │
│ EMBEDDED                                        │
│ JDBC / SQL / Quality                            │
│                                      [查看]      │
├─────────────────────────────────────────────────┤
│ Apache DataX                         运行中      │
│ LOCAL_PROCESS                                   │
│ /opt/datax                                      │
│                               [测试] [配置]      │
├─────────────────────────────────────────────────┤
│ Apache Camel                         运行中      │
│ EMBEDDED                                        │
│ HTTP / SFTP / Kafka / JMS                       │
│                               [测试] [配置]      │
├─────────────────────────────────────────────────┤
│ Apache SeaTunnel                     未配置      │
│ REMOTE / LOCAL                                   │
│                               [配置接入]         │
└─────────────────────────────────────────────────┘
```

---

# 35. Engine 配置页面

通用字段：

```text
Enabled
Deployment Mode
Version
Config
Health
Last Check
```

DataX：

```text
DataX Home
Python Path
Temp Job Directory
Max Concurrent Jobs
```

Camel：

```text
Enabled Components
Thread Pool
Route Timeout
```

SeaTunnel：

```text
Endpoint
Home
Cluster Mode
```

---

# 36. 数据源管理

页面：

```text
名称
类型
地址
数据库
状态
最后测试
被引用数量
操作
```

操作：

```text
测试
编辑
同步元数据
复制
删除
```

---

# 37. 数据资产

```text
Datasource
  ↓
Catalog
  ↓
Schema
  ↓
Table
  ↓
Field
```

页面支持：

```text
搜索
字段信息
备注
主键
数据类型
估算行数
最近采集时间
```

---

# 38. 数据质量

V1 质量规则：

```text
Null
Unique
Range
Regex
Length
Enum
Custom SQL
```

结果：

```text
totalRows
errorRows
passRows
passRate
```

---

# 39. 质量结果

## dc_quality_result

```text
id
execution_id
node_execution_id
rule_type
dataset_id
field_name
total_rows
error_rows
pass_rows
pass_rate
status
created_at
```

---

# 40. 异常样例

保存：

```text
Sample
+
Error SQL
+
DataReference
```

V1 默认最多保存：

```text
1000 条
```

避免复制整个异常数据集。

---

# 41. Scheduler

V1：

```text
Quartz
```

支持：

```text
Manual
Cron
Daily
Weekly
Monthly
Retry
API Trigger
```

---

# 42. Execution

## dc_pipeline_execution

```text
id
pipeline_id
trigger_type
status
started_at
finished_at
duration_ms
error_message
created_at
```

---

# 43. Node Execution

## dc_node_execution

```text
id
execution_id
stage_id
node_key
node_type
status
started_at
finished_at
duration_ms
input_rows
output_rows
error_rows
error_message
log_text
```

---

# 44. 实时运行状态

V1：

```text
SSE
```

接口：

```http
GET /api/v1/executions/{id}/events
```

事件：

```text
EXECUTION_STARTED
STAGE_STARTED
NODE_STARTED
NODE_LOG
NODE_SUCCESS
NODE_FAILED
STAGE_SUCCESS
STAGE_FAILED
EXECUTION_SUCCESS
EXECUTION_FAILED
```

---

# 45. UI 状态

```text
WAITING   灰
RUNNING   蓝
SUCCESS   绿
FAILED    红
SKIPPED   灰
```

---

# 46. 安全

```text
Spring Security
JWT
RBAC
```

角色：

```text
ADMIN
DEVELOPER
VIEWER
```

---

# 47. 数据源密码

必须：

```text
AES-GCM
```

密钥：

```text
DATACRAFT_SECRET_KEY
```

禁止：

```text
DB 明文
日志输出
API 返回
```

---

# 48. Audit

## dc_audit_log

```text
id
user_id
action
resource_type
resource_id
request_path
ip
detail_json
created_at
```

---

# 49. API 规范

Base：

```text
/api/v1
```

统一响应：

```json
{
  "code": 0,
  "message": "success",
  "data": {}
}
```

---

# 50. Pipeline API

```http
GET    /api/v1/pipelines
POST   /api/v1/pipelines
GET    /api/v1/pipelines/{id}
PUT    /api/v1/pipelines/{id}
DELETE /api/v1/pipelines/{id}

POST /api/v1/pipelines/{id}/validate
POST /api/v1/pipelines/{id}/plan
POST /api/v1/pipelines/{id}/execute
```

---

# 51. Engine API

```http
GET  /api/v1/engines
GET  /api/v1/engines/{code}
POST /api/v1/engines/{code}/test
PUT  /api/v1/engines/{code}
```

---

# 52. Node API

```http
GET /api/v1/node-types
GET /api/v1/node-types/{type}
```

---

# 53. Execution API

```http
GET  /api/v1/executions
GET  /api/v1/executions/{id}
POST /api/v1/executions/{id}/retry
POST /api/v1/executions/{id}/cancel

GET /api/v1/executions/{id}/events
GET /api/v1/executions/{id}/logs
```

---

# 54. UI 风格

DataCraft：

```text
专业
简洁
工程化
低圆角
浅灰背景
白色工作区
弱阴影
高信息密度
```

避免：

```text
大圆角
大量渐变
营销风
花哨动画
过度卡片化
```

---

# 55. 产品版本路线图

---

# V0.1 — Project Foundation

目标：

> 建立可持续开发的工程基础。

实现：

```text
Java 21
Spring Boot
Vue3
PostgreSQL
Redis
Flyway
JWT
登录
系统 Layout
统一响应
统一异常
基础权限
```

验收：

- admin 登录成功
- 页面框架可用
- 前后端能联调
- Docker Compose 可启动 PostgreSQL/Redis
- Flyway 初始化成功

不实现：

```text
Pipeline
Engine
DataX
Camel
```

---

# V0.2 — Datasource & Metadata

目标：

> DataCraft 能真正连接数据库。

实现：

```text
MySQL
PostgreSQL

数据源 CRUD
连接测试
元数据采集
Schema
Table
Field
```

验收：

```text
添加 MySQL
测试成功
同步 customer 表
查看字段
```

---

# V0.3 — Pipeline Designer

目标：

> 先完成“能画、能保存”的 Pipeline。

实现：

```text
Pipeline
Node
Edge
Vue Flow
Node Library
Node Config
保存
加载
DAG Validation
```

暂不执行。

验收：

```text
MySQL
↓
Filter
↓
PostgreSQL

保存
刷新
完整恢复
```

---

# V0.4 — Native Pipeline Engine

目标：

> Pipeline 真正跑起来。

实现：

```text
NodeRegistry
EngineRegistry
NativeExecutionEngine
ExecutionPlanner
ExecutionPlan
ExecutionStage
DataReference
```

第一批节点：

```text
DATABASE_SOURCE
FILTER
DATABASE_SINK
```

验收：

```text
MySQL A
↓
Filter
↓
PostgreSQL B

可以执行成功
```

---

# V0.5 — Transform

实现：

```text
SQL Transform
Field Mapping
Type Convert
Trim
Replace
```

验收：

```text
字段清洗
字段重命名
类型转换
SQL 转换
```

---

# V0.6 — Data Quality

实现：

```text
Null
Unique
Range
Regex
Length
Enum
Custom SQL
```

以及：

```text
Quality Result
Error Samples
Quality Report
```

验收：

```text
customer.phone
空值检查

显示：
total
error
passRate
sample
```

---

# V0.7 — Scheduler & Monitoring

实现：

```text
Quartz
Cron
Execution History
Retry
Cancel
SSE
Node Status
Logs
```

验收：

```text
每天 02:00
自动运行
前端可查看状态
```

---

# V1.0 — DataCraft MVP

V1.0 是第一个真正可演示、可内部使用版本。

必须具备：

```text
用户登录
MySQL
PostgreSQL
元数据
资产浏览
Pipeline
Native Engine
Transform
Quality
Scheduler
Execution
Logs
Report
```

Demo：

```text
MySQL customer
↓
Filter
↓
Null Check
↓
Masking
↓
PostgreSQL customer_clean
```

---

# V1.1 — Engine SPI

目标：

> 为第三方引擎形成正式扩展体系。

实现：

```text
ExecutionEngine SPI
EngineRegistry
EngineMetadata
EngineCapability
EngineHealth
Engine Config
Engine Management UI
```

Native Engine 完成 SPI 化。

验收：

```text
Native 引擎不再由 Pipeline 核心代码直接调用
Pipeline 只依赖 ExecutionEngine
```

---

# V1.2 — DataX Integration

目标：

> 引入成熟批量同步能力。

实现：

```text
DataXExecutionEngine
DataXCompiler
DataXJobBuilder
DataXProcessManager
DataXLogParser
DataX Metrics
```

支持：

```text
MySQL Reader
PostgreSQL Reader
MySQL Writer
PostgreSQL Writer
```

UI：

```text
用户仍然使用 MySQL / PostgreSQL Node
```

高级：

```text
执行引擎：
AUTO
DATAX
NATIVE
```

验收：

```text
100万行 MySQL → PostgreSQL
DataX 执行
DataCraft 显示状态/耗时/日志/数据量
```

---

# V1.3 — Hybrid Execution

目标：

> 一个 Pipeline 可以使用多个执行引擎。

实现：

```text
ExecutionStage
Stage Planner
Materialization
DataReference
Stage Dependency
```

示例：

```text
DATAX
MySQL → PostgreSQL Temp

NATIVE
↓
Quality Check
```

验收：

```text
Stage 1 SUCCESS
↓
Stage 2 SUCCESS
↓
Pipeline SUCCESS
```

---

# V1.5 — Apache Camel Integration

目标：

> DataCraft 从数据库治理扩展到企业数据连接。

实现：

```text
CamelExecutionEngine
CamelCompiler
CamelRuntimeManager
CamelContext
Route Management
```

第一批：

```text
HTTP
FTP
SFTP
File
Kafka
RabbitMQ
Mail
```

节点：

```text
HTTP_SOURCE
HTTP_SINK
SFTP_SOURCE
SFTP_SINK
FILE_SOURCE
FILE_SINK
KAFKA_SOURCE
KAFKA_SINK
MAIL_SINK
```

验收：

```text
SFTP
↓
CSV
↓
Mapping
↓
PostgreSQL
↓
Webhook
```

---

# V1.6 — Connector Center

目标：

> 将连接能力产品化。

新增一级或二级模块：

```text
连接器
```

界面：

```text
数据库
文件
API
消息
企业应用
```

支持：

```text
启用
禁用
配置
测试
能力查看
```

实现：

```text
ConnectorRegistry
ConnectorMetadata
ConnectorProvider
Connector Capability
```

---

# V2.0 — Data Integration Platform

目标：

> 从数据治理产品升级为数据集成 + 数据治理平台。

新增：

```text
Oracle
SQL Server
CSV
Excel
SFTP
HTTP
Kafka
RabbitMQ
```

引擎：

```text
Native
DataX
Camel
```

能力：

```text
数据接入
数据处理
质量
治理
调度
同步
API Integration
```

---

# V2.1 — SeaTunnel

目标：

> 增强大规模数据集成能力。

实现：

```text
SeaTunnelExecutionEngine
SeaTunnelCompiler
Local Mode
Remote Mode
```

支持：

```text
批处理
CDC
部分流式
```

Planner：

```text
AUTO
```

开始支持：

```text
Native
DataX
SeaTunnel
```

自动选择。

---

# V2.2 — Data Lineage

新增：

```text
表级血缘
字段级血缘
Pipeline 血缘
```

来源：

```text
Pipeline Edge
SQL Parser
Metadata
Execution
```

UI：

```text
血缘图
上下游
影响分析
```

---

# V2.3 — Data Standard

新增：

```text
数据标准
字段标准
命名标准
编码标准
值域
字典
```

Pipeline 中：

```text
Standardize Node
```

质量规则可以引用：

```text
Data Standard
```

---

# V2.4 — Governance Workflow

如果需要审批：

```text
Flowable
```

Flowable 只负责：

```text
问题整改
审批
确认
复核
```

不负责 Data Pipeline。

---

# V2.5 — Plugin Runtime

目标：

> 支持第三方扩展。

模块：

```text
datacraft-plugin-runtime
```

插件 API：

```text
NodeProvider
ConnectorProvider
EngineProvider
```

插件目录：

```text
plugins/
```

例如：

```text
datacraft-connector-doris.jar
datacraft-connector-clickhouse.jar
datacraft-connector-mqtt.jar
```

启动：

```text
scan
↓
load
↓
register
↓
NodeMetadata API
↓
UI 自动出现
```

---

# V3.0 — Real-time Data Governance

引入：

```text
Kafka
Flink
```

能力：

```text
实时 Pipeline
实时质量
实时治理
事件处理
```

新增 Pipeline Mode：

```text
BATCH
STREAM
```

---

# V3.1 — Flink Engine

实现：

```text
FlinkExecutionEngine
FlinkCompiler
REST Submit
Job Status
Cancel
Metrics
Logs
```

---

# V3.2 — Realtime Quality

支持：

```text
实时 Null
实时 Range
实时 Regex
实时异常率
窗口质量统计
```

---

# V3.5 — Observability

增强：

```text
任务 SLA
任务趋势
吞吐
失败率
Engine Metrics
Node Metrics
告警
Webhook
企业微信
钉钉
```

---

# V4.0 — AI Data Governance

目标：

> 在已有稳定 Pipeline 模型上增加 AI。

---

## V4.1 AI Pipeline Builder

用户输入：

```text
把 MySQL customer 表同步到 PostgreSQL，
手机号不能为空，
手机号脱敏，
每天凌晨 2 点执行。
```

AI 自动产生：

```text
Pipeline Draft
```

用户确认后保存。

---

## V4.2 AI Quality Rules

AI 根据：

```text
Field Name
Data Type
Sample
Metadata
```

推荐：

```text
Null
Regex
Range
Enum
Unique
```

---

## V4.3 AI Root Cause

任务失败：

```text
ORA-xxxx
timeout
schema mismatch
disk full
```

AI：

```text
解释问题
分析根因
给出处理建议
```

---

## V4.4 Governance Agent

用户：

```text
检查客户数据最近一周质量下降原因
```

Agent：

```text
读取质量结果
读取任务日志
分析 Pipeline
分析数据变化
输出原因
```

---

# 56. 产品菜单版本演进

## V1

```text
首页
数据源
数据资产
数据处理
数据质量
任务中心
系统管理
```

---

## V1.5

增加：

```text
连接器
```

---

## V2

增加：

```text
数据血缘
数据标准
```

---

## V3

增加：

```text
实时任务
监控中心
```

---

## V4

增加：

```text
AI 助手
```

---

# 57. Phase 开发拆分

版本和开发 Phase 不完全相同。

一个版本可以由多个 Phase 完成。

---

# Phase 0 — Repository & Engineering

创建：

```text
Maven Modules
Vue
Docker
PostgreSQL
Redis
Flyway
README
AGENTS
CI
```

验收：

```text
mvn test
npm build
docker compose up
```

全部通过。

---

# Phase 1 — Auth & System

实现：

```text
User
Role
JWT
Login
Layout
Menu
```

---

# Phase 2 — Datasource

实现：

```text
Datasource CRUD
Test Connection
Password Encryption
```

---

# Phase 3 — Metadata

实现：

```text
Metadata Collector
Dataset
Field
Asset UI
```

---

# Phase 4 — Pipeline Model

实现：

```text
Pipeline
Node
Edge
NodeMetadata
NodeRegistry
```

---

# Phase 5 — Pipeline Editor

实现：

```text
Vue Flow
Node Library
Canvas
Config Panel
Save
Load
Validation
```

---

# Phase 6 — Engine API

实现：

```text
ExecutionEngine
EngineRegistry
Capability
Health
```

先只有：

```text
Native
```

---

# Phase 7 — Execution Planner

实现：

```text
ExecutionPlan
Stage
Planner
DataReference
```

---

# Phase 8 — Native Runtime

实现：

```text
JDBC Source
Filter
SQL
Mapping
Sink
```

---

# Phase 9 — Quality

实现：

```text
Quality Node
Quality Result
Samples
Report
```

---

# Phase 10 — Scheduler

实现：

```text
Quartz
Cron
Retry
Cancel
```

---

# Phase 11 — Realtime Execution UI

实现：

```text
SSE
Execution Panel
Node Color
Log
Metrics
```

---

# Phase 12 — DataX

实现：

```text
Adapter
Compiler
Process
Logs
Metrics
```

---

# Phase 13 — Hybrid Stage

实现：

```text
Planner multi-stage
Materialization
Stage transition
```

---

# Phase 14 — Camel

实现：

```text
CamelContext
Compiler
Route Manager
HTTP
SFTP
Kafka
```

---

# Phase 15 — Connector Center

实现：

```text
ConnectorRegistry
UI
ConfigSchema
Health
```

---

# 58. 开发优先级

最高：

```text
P0
```

包括：

```text
Datasource
Metadata
Pipeline Editor
Native Engine
Execution
Quality
```

第二：

```text
P1

DataX
Scheduler
Realtime UI
```

第三：

```text
P2

Camel
Connector Center
```

第四：

```text
P3

SeaTunnel
Lineage
Standard
```

---

# 59. AI 编程工具执行规则

所有 Codex / Claude Code Agent：

必须先阅读：

```text
DESIGN.md
AGENTS.md
```

每个任务：

```text
分析
↓
Plan
↓
Implement
↓
Build
↓
Test
↓
Fix
↓
Report
```

禁止一次开发所有版本。

---

# 60. Codex 总提示词

```text
你是 DataCraft 项目的高级 Java 架构师、Vue 工程师和产品工程师。

在执行任何代码修改前：

1. 阅读仓库根目录 DESIGN.md。
2. 阅读 AGENTS.md。
3. 检查当前 Git 状态。
4. 确认当前开发 Phase。
5. 输出本次实施计划。

DataCraft 核心架构：

DataCraft 是 Control Plane。

第三方执行引擎属于 Execution Plane。

Pipeline、Node、Edge、Execution、Schedule、QualityResult
必须由 DataCraft 自己维护。

任何第三方引擎都必须通过 ExecutionEngine SPI 接入。

禁止 Pipeline 核心模块直接依赖 DataX、Camel、SeaTunnel、Flink。

Node 与 Engine 必须分离。

Connector 与 Engine 必须分离。

跨 Engine Stage 不允许使用 Java List 传递大规模数据，
必须使用 DataReference + Materialization。

技术栈：

Java 21
Spring Boot 3
Maven
MyBatis-Plus
PostgreSQL
Redis
Flyway
Quartz
Spring Security
JWT

Vue 3
TypeScript
Vite
Element Plus
Pinia
Vue Flow
ECharts
Monaco Editor

架构：

Modular Monolith

禁止擅自微服务化。

当前工作只实现指定 Phase，
不得提前开发后续版本。

完成后必须：

1. mvn test
2. npm build
3. 修复错误
4. 更新 DEV_PROGRESS.md
5. 输出完成内容
6. 输出尚未完成内容
7. 输出风险
```

---

# 61. AGENTS.md 建议

```text
# DataCraft AI Development Rules

所有 Agent 开始任务前必须阅读：

DESIGN.md
DEV_PROGRESS.md

核心规则：

1. Pipeline First。
2. DataCraft Control Plane 不绑定第三方 Engine。
3. Engine 必须实现 ExecutionEngine SPI。
4. Node 与 Engine 分离。
5. Connector 与 Engine 分离。
6. 前端节点采用 Metadata Driven。
7. 大数据不进入 Java List。
8. SQL Pushdown First。
9. 跨 Stage 使用 DataReference。
10. 禁止微服务化。
11. 禁止明文存储密码。
12. 新数据库字段必须 Flyway。
13. Entity 不直接作为 API Response。
14. Controller 不直接调用 Mapper。
15. Engine 代码必须独立模块。
16. Pipeline Engine 必须有单元测试。
17. 不为了通过测试删除测试。
18. 修改后必须构建。
19. 不擅自实现后续版本。
20. 优先完成 MVP。
```

---

# 62. DEV_PROGRESS.md

项目必须维护：

```text
DEV_PROGRESS.md
```

格式：

```text
Current Version:
V0.3

Current Phase:
Phase 5

Completed:
- Pipeline CRUD
- Node CRUD
- Vue Flow basic editor

In Progress:
- Edge validation

Next:
- Node Config Schema

Blocked:
- None
```

这样 Codex 每次都能知道项目当前进度。

---

# 63. ADR

建议增加：

```text
docs/adr/
```

例如：

```text
0001-modular-monolith.md
0002-engine-spi.md
0003-datareference.md
0004-datax-local-process.md
0005-camel-embedded.md
```

重要架构决策必须记录。

---

# 64. 测试策略

## Unit

必须覆盖：

```text
PipelineValidator
TopologicalSorter
ExecutionPlanner
EngineRegistry
NodeRegistry
DataXCompiler
CamelCompiler
```

---

## Integration

使用：

```text
Testcontainers
```

测试：

```text
PostgreSQL
MySQL
Redis
```

---

## E2E

后期：

```text
Playwright
```

核心流程：

```text
login
datasource
metadata
pipeline
execute
result
```

---

# 65. Docker

开发环境：

```text
postgres
redis
mysql-test
```

V1.2：

```text
datax
```

V1.5：

Camel Embedded，不需要独立容器。

---

# 66. Production

第一阶段：

```text
Nginx
↓
DataCraft Web
↓
DataCraft Server
↓
PostgreSQL
Redis

+
DataX Local
```

后期：

```text
SeaTunnel
Flink
Kafka
```

可独立部署。

---

# 67. 数据库命名

统一：

```text
dc_
```

例如：

```text
dc_user
dc_datasource
dc_dataset
dc_dataset_field
dc_pipeline
dc_pipeline_node
dc_pipeline_edge
dc_engine
dc_schedule
dc_pipeline_execution
dc_execution_stage
dc_node_execution
dc_quality_result
dc_audit_log
```

---

# 68. MVP Demo

V1.0 必须完成：

```text
Login
↓
Add MySQL
↓
Test
↓
Sync Metadata
↓
Browse customer
↓
Create Pipeline
↓
MySQL
↓
Filter
↓
Null Check
↓
Masking
↓
PostgreSQL
↓
Execute
↓
Realtime Status
↓
Quality Result
↓
Schedule
```

---

# 69. V1.2 Demo

```text
MySQL
↓
DataX
↓
PostgreSQL

1,000,000 rows

DataCraft 显示：

Engine: Apache DataX
Status: SUCCESS
Rows: 1,000,000
Duration: 45s
Logs: available
```

---

# 70. V1.5 Demo

```text
SFTP
↓
CSV Parse
↓
Mapping
↓
Quality
↓
PostgreSQL
↓
Webhook
```

底层：

```text
Camel
↓
Native
↓
Native
↓
Native
↓
Native
↓
Camel
```

用户无需理解底层 Engine。

---

# 71. V2 Demo

```text
Oracle
↓
SeaTunnel
↓
PostgreSQL
↓
Quality
↓
Lineage
↓
Standard
```

---

# 72. V3 Demo

```text
Kafka
↓
Flink
↓
Realtime Quality
↓
Kafka / DB
```

---

# 73. V4 Demo

用户输入：

```text
把 ERP 的订单每天同步到数据仓库，
过滤取消订单，
订单金额不能为空，
客户手机号脱敏，
失败时通知我。
```

AI：

```text
自动生成 Pipeline
↓
用户确认
↓
发布
↓
运行
```

---

# 74. 最重要的产品边界

DataCraft 不应该变成：

```text
DataX UI
+
Camel UI
+
SeaTunnel UI
```

也不应该只是：

```text
第三方项目启动器
```

DataCraft 必须拥有：

```text
统一领域模型
统一编排
统一交互
统一配置
统一任务
统一日志
统一质量
统一权限
统一审计
统一执行计划
```

第三方项目只是：

```text
Execution Provider
```

---

# 75. 最终目标架构

```text
                       DataCraft Web
                            │
                            ▼
                    DataCraft API
                            │
                            ▼
                     Pipeline Model
                            │
                            ▼
                   Execution Planner
                            │
                  ┌─────────┼─────────┐
                  │         │         │
                  ▼         ▼         ▼
                Stage 1   Stage 2   Stage 3
                  │         │         │
                  ▼         ▼         ▼
                DATAX     NATIVE     CAMEL
                  │         │         │
                  ▼         ▼         ▼
                DB→DB     Quality    Webhook
                            │
                            ▼
                     DataReference
```

未来：

```text
                SEATUNNEL
                FLINK
                SPARK
                PYTHON
```

不改变 Pipeline 核心模型。

---

# 76. 最终产品一句话

> **DataCraft 是一个通过可视化 Pipeline 统一编排多种数据处理引擎的数据集成与数据治理平台。**

对普通用户：

> 连接数据、拖拽处理、配置规则、运行任务。

对技术架构：

> Control Plane + Pluggable Execution Engines。

对未来商业化：

> 平台拥有统一体验，开源引擎只是底层能力提供者。

