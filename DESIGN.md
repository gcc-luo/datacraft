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


---

# 附录 A：Codex/Luna 可直接执行的工程规格（V3.0 增补）

> 本附录把 V2 设计进一步细化为“可执行规格”。目标不是让模型一次性生成全部代码，而是让 Codex/Luna 在每个 Phase 中尽量减少自行猜测。

## A.1 设计文档优先级

AI 编程代理执行任何修改时，必须按以下优先级处理冲突：

```text
用户当前明确指令
    >
DESIGN.md
    >
AGENTS.md
    >
DEV_PROGRESS.md
    >
现有代码
```

如果现有代码与 DESIGN.md 冲突，不允许静默改变架构。先识别偏差，再在本阶段范围内向设计收敛；若需要大范围重构，先输出影响范围。

## A.2 单次 Agent 执行标准流程

每次开发必须严格执行：

```text
Read DESIGN.md
↓
Read AGENTS.md
↓
Read DEV_PROGRESS.md
↓
Check Git Status
↓
Confirm Version / Phase
↓
Output Implementation Plan
↓
Implement
↓
Run Tests / Build
↓
Fix
↓
Update DEV_PROGRESS.md
↓
Report
```

禁止只生成代码但不执行构建验证。

## A.3 Definition of Done

任何一个功能只有同时满足以下条件才算完成：

```text
代码完成
API 完成
数据库 Migration 完成
输入校验完成
异常处理完成
权限检查完成
关键日志完成
单元测试完成
构建通过
必要文档更新
DEV_PROGRESS.md 更新
```

---

# 附录 B：后端工程结构详细约束

## B.1 根工程

```text
datacraft/
├── pom.xml
├── datacraft-server/
├── datacraft-web/
├── DESIGN.md
├── AGENTS.md
├── DEV_PROGRESS.md
├── README.md
├── docker-compose.yml
├── docs/
├── scripts/
├── docker/
└── deploy/
```

## B.2 Maven 模块依赖方向

推荐依赖方向：

```text
datacraft-bootstrap
        ↓
datacraft-api
        ↓
业务模块
        ↓
node-api / connector-api / engine-api
        ↓
datacraft-common
```

Engine 实现：

```text
datacraft-engine-native → datacraft-engine-api

datacraft-engine-datax → datacraft-engine-api

datacraft-engine-camel → datacraft-engine-api
```

严格禁止：

```text
datacraft-pipeline → datacraft-engine-datax

datacraft-pipeline → datacraft-engine-camel
```

允许：

```text
datacraft-pipeline → datacraft-engine-api
```

## B.3 Java 包规范

每个业务模块建议：

```text
com.datacraft.<module>
├── controller
├── service
├── domain
├── dto
├── mapper
├── repository
├── converter
├── exception
└── internal
```

对于 pipeline：

```text
com.datacraft.pipeline
├── controller
├── service
├── domain
├── dto
├── mapper
├── validation
├── planner
├── execution
└── event
```

对于 engine：

```text
com.datacraft.engine.api
├── model
├── spi
└── exception
```

---

# 附录 C：统一 API 规范

## C.1 Base Path

```text
/api/v1
```

## C.2 统一响应

```java
public record ApiResponse<T>(
    int code,
    String message,
    T data
) {}
```

成功：

```json
{
  "code": 0,
  "message": "success",
  "data": {}
}
```

错误：

```json
{
  "code": 5001002,
  "message": "Pipeline contains cycle",
  "data": null
}
```

## C.3 错误码范围

```text
100xxxx System
200xxxx Authentication / Authorization
300xxxx Datasource
400xxxx Metadata
500xxxx Pipeline
600xxxx Execution
700xxxx Quality
800xxxx Scheduler
900xxxx Engine
```

示例：

```text
3001001 Datasource not found
3001002 Datasource connection failed
4001001 Dataset not found
5001001 Pipeline not found
5001002 Pipeline contains cycle
5001003 Invalid node configuration
6001001 Execution not found
6001002 Execution cannot be cancelled
7001001 Quality result not found
9001001 Engine not found
9001002 Engine unavailable
9001003 Engine does not support node
```

## C.4 分页

请求：

```text
page=1
pageSize=20
```

响应 data：

```json
{
  "items": [],
  "page": 1,
  "pageSize": 20,
  "total": 0
}
```

---

# 附录 D：数据库规范与核心 DDL

## D.1 通用约束

数据库固定 PostgreSQL。

主键：

```text
BIGINT
```

由应用层生成：

```text
MyBatis-Plus ASSIGN_ID
```

时间字段：

```text
TIMESTAMPTZ
```

业务表默认：

```text
created_at
updated_at
```

## D.2 用户表

```sql
CREATE TABLE dc_user (
    id              BIGINT PRIMARY KEY,
    username        VARCHAR(64) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    display_name    VARCHAR(128),
    status          VARCHAR(32) NOT NULL DEFAULT 'ENABLED',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```

状态：

```text
ENABLED
DISABLED
LOCKED
```

## D.3 角色表

```sql
CREATE TABLE dc_role (
    id          BIGINT PRIMARY KEY,
    role_code   VARCHAR(64) NOT NULL UNIQUE,
    role_name   VARCHAR(128) NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```

默认角色：

```text
ADMIN
DEVELOPER
VIEWER
```

## D.4 数据源表

```sql
CREATE TABLE dc_datasource (
    id                  BIGINT PRIMARY KEY,
    workspace_id        BIGINT,
    name                VARCHAR(128) NOT NULL,
    type                VARCHAR(32) NOT NULL,
    host                VARCHAR(255),
    port                INTEGER,
    database_name       VARCHAR(128),
    schema_name         VARCHAR(128),
    username            VARCHAR(255),
    password_cipher     TEXT,
    jdbc_url            TEXT,
    config_json         JSONB,
    status              VARCHAR(32) NOT NULL DEFAULT 'UNKNOWN',
    last_test_at        TIMESTAMPTZ,
    created_by          BIGINT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```

V1 数据源：

```text
MYSQL
POSTGRESQL
```

后续：

```text
ORACLE
SQLSERVER
```

## D.5 数据集表

```sql
CREATE TABLE dc_dataset (
    id              BIGINT PRIMARY KEY,
    datasource_id   BIGINT NOT NULL,
    catalog_name    VARCHAR(128),
    schema_name     VARCHAR(128),
    table_name      VARCHAR(256) NOT NULL,
    table_type      VARCHAR(32),
    comment         TEXT,
    row_count       BIGINT,
    last_sync_at    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```

## D.6 字段表

```sql
CREATE TABLE dc_dataset_field (
    id                  BIGINT PRIMARY KEY,
    dataset_id          BIGINT NOT NULL,
    field_name          VARCHAR(256) NOT NULL,
    data_type           VARCHAR(128),
    jdbc_type           INTEGER,
    length              INTEGER,
    precision_value     INTEGER,
    scale_value         INTEGER,
    nullable            BOOLEAN,
    primary_key_flag    BOOLEAN DEFAULT FALSE,
    comment             TEXT,
    ordinal_position    INTEGER,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```

## D.7 Pipeline 表

```sql
CREATE TABLE dc_pipeline (
    id                  BIGINT PRIMARY KEY,
    workspace_id        BIGINT,
    name                VARCHAR(255) NOT NULL,
    description         TEXT,
    status              VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
    version             INTEGER NOT NULL DEFAULT 1,
    graph_json          JSONB,
    execution_strategy  VARCHAR(32) NOT NULL DEFAULT 'AUTO',
    created_by          BIGINT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```

状态：

```text
DRAFT
PUBLISHED
DISABLED
```

## D.8 Pipeline Node

```sql
CREATE TABLE dc_pipeline_node (
    id                  BIGINT PRIMARY KEY,
    pipeline_id         BIGINT NOT NULL,
    node_key            VARCHAR(128) NOT NULL,
    node_type           VARCHAR(128) NOT NULL,
    node_name           VARCHAR(255),
    position_x          NUMERIC(12,2),
    position_y          NUMERIC(12,2),
    config_json         JSONB,
    preferred_engine    VARCHAR(64),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(pipeline_id, node_key)
);
```

## D.9 Pipeline Edge

```sql
CREATE TABLE dc_pipeline_edge (
    id                  BIGINT PRIMARY KEY,
    pipeline_id         BIGINT NOT NULL,
    source_node_key     VARCHAR(128) NOT NULL,
    target_node_key     VARCHAR(128) NOT NULL,
    source_port         VARCHAR(128),
    target_port         VARCHAR(128),
    condition_json      JSONB,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```

## D.10 Engine 表

```sql
CREATE TABLE dc_engine (
    id                  BIGINT PRIMARY KEY,
    engine_code         VARCHAR(64) NOT NULL UNIQUE,
    engine_name         VARCHAR(128) NOT NULL,
    engine_type         VARCHAR(64) NOT NULL,
    deployment_mode     VARCHAR(32) NOT NULL,
    enabled             BOOLEAN NOT NULL DEFAULT TRUE,
    version             VARCHAR(64),
    config_json         JSONB,
    status              VARCHAR(32) DEFAULT 'UNKNOWN',
    last_health_check   TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```

## D.11 Pipeline Execution

```sql
CREATE TABLE dc_pipeline_execution (
    id                  BIGINT PRIMARY KEY,
    pipeline_id         BIGINT NOT NULL,
    trigger_type        VARCHAR(32) NOT NULL,
    status              VARCHAR(32) NOT NULL,
    started_at          TIMESTAMPTZ,
    finished_at         TIMESTAMPTZ,
    duration_ms         BIGINT,
    error_message       TEXT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```

状态：

```text
CREATED
PLANNING
RUNNING
SUCCESS
FAILED
CANCELLING
CANCELLED
```

## D.12 Execution Stage

```sql
CREATE TABLE dc_execution_stage (
    id                      BIGINT PRIMARY KEY,
    execution_id            BIGINT NOT NULL,
    stage_index             INTEGER NOT NULL,
    engine_code             VARCHAR(64) NOT NULL,
    status                  VARCHAR(32) NOT NULL,
    engine_job_id           VARCHAR(255),
    started_at              TIMESTAMPTZ,
    finished_at             TIMESTAMPTZ,
    input_rows              BIGINT,
    output_rows             BIGINT,
    error_rows              BIGINT,
    error_message           TEXT,
    engine_metadata_json    JSONB,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```

## D.13 Node Execution

```sql
CREATE TABLE dc_node_execution (
    id                  BIGINT PRIMARY KEY,
    execution_id        BIGINT NOT NULL,
    stage_id            BIGINT,
    node_key            VARCHAR(128) NOT NULL,
    node_type           VARCHAR(128) NOT NULL,
    status              VARCHAR(32) NOT NULL,
    started_at          TIMESTAMPTZ,
    finished_at         TIMESTAMPTZ,
    duration_ms         BIGINT,
    input_rows          BIGINT,
    output_rows         BIGINT,
    error_rows          BIGINT,
    error_message       TEXT,
    log_text            TEXT
);
```

## D.14 Quality Result

```sql
CREATE TABLE dc_quality_result (
    id                  BIGINT PRIMARY KEY,
    execution_id        BIGINT NOT NULL,
    node_execution_id   BIGINT NOT NULL,
    rule_type           VARCHAR(64) NOT NULL,
    dataset_id          BIGINT,
    field_name          VARCHAR(255),
    total_rows          BIGINT,
    error_rows          BIGINT,
    pass_rows           BIGINT,
    pass_rate           NUMERIC(8,4),
    status              VARCHAR(32),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```

## D.15 Quality Sample

```sql
CREATE TABLE dc_quality_sample (
    id                  BIGINT PRIMARY KEY,
    quality_result_id   BIGINT NOT NULL,
    sample_index        INTEGER NOT NULL,
    data_json           JSONB NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```

默认最多保存 1000 条样例。

## D.16 Schedule

```sql
CREATE TABLE dc_schedule (
    id                  BIGINT PRIMARY KEY,
    pipeline_id         BIGINT NOT NULL,
    name                VARCHAR(255) NOT NULL,
    cron_expression     VARCHAR(128) NOT NULL,
    enabled             BOOLEAN NOT NULL DEFAULT TRUE,
    last_fire_at        TIMESTAMPTZ,
    next_fire_at        TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```

## D.17 Audit

```sql
CREATE TABLE dc_audit_log (
    id              BIGINT PRIMARY KEY,
    user_id         BIGINT,
    action          VARCHAR(128) NOT NULL,
    resource_type   VARCHAR(64),
    resource_id     VARCHAR(128),
    request_path    VARCHAR(512),
    ip              VARCHAR(64),
    detail_json     JSONB,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```

---

# 附录 E：Datasource 详细实现

## E.1 密码

使用：

```text
AES-256-GCM
```

密钥：

```text
DATACRAFT_SECRET_KEY
```

禁止：

```text
DB 明文
API 返回真实密码
日志打印密码
编辑页面读取原密码
```

编辑数据源时，如果密码字段为空：

```text
保留已有密文
```

## E.2 测试连接流程

```text
POST /api/v1/datasources/test
↓
request validation
↓
build JDBC config
↓
create short-lived connection
↓
SELECT 1
↓
read database product/version
↓
close
↓
return latency/status
```

响应：

```json
{
  "success": true,
  "latencyMs": 36,
  "databaseVersion": "PostgreSQL 16"
}
```

## E.3 Metadata Collector SPI

```java
public interface MetadataCollector {

    DatasourceType supports();

    List<TableMetadata> collectTables(
        DatasourceConnection connection
    );

    List<ColumnMetadata> collectColumns(
        DatasourceConnection connection,
        TableIdentifier table
    );
}
```

实现：

```text
MySqlMetadataCollector
PostgresqlMetadataCollector
```

通过：

```text
MetadataCollectorRegistry
```

选择实现。

---

# 附录 F：Node 元数据与动态 UI

## F.1 NodeCategory

```java
public enum NodeCategory {
    SOURCE,
    TRANSFORM,
    QUALITY,
    GOVERNANCE,
    SINK,
    UTILITY
}
```

## F.2 NodeMetadata

```java
public record NodeMetadata(
    String type,
    String name,
    NodeCategory category,
    String icon,
    List<String> supportedEngines,
    String defaultEngine,
    List<NodePort> inputs,
    List<NodePort> outputs,
    ConfigSchema configSchema
) {}
```

## F.3 ConfigSchema

```java
public record ConfigSchema(
    List<ConfigField> fields
) {}
```

```java
public record ConfigField(
    String name,
    String label,
    String type,
    boolean required,
    Object defaultValue,
    List<Option> options,
    Map<String, Object> rules
) {}
```

字段类型：

```text
TEXT
PASSWORD
NUMBER
BOOLEAN
SELECT
MULTI_SELECT
DATASOURCE_SELECT
DATASET_SELECT
FIELD_SELECT
SQL_EDITOR
JSON_EDITOR
CRON
```

## F.4 NodeProvider

```java
public interface NodeProvider {

    String type();

    NodeMetadata metadata();

    ValidationResult validate(
        NodeDefinition definition
    );
}
```

NodeProvider 不执行数据。

## F.5 第一批节点

```text
DATABASE_SOURCE
SQL_SOURCE
FILTER
SQL_TRANSFORM
FIELD_MAPPING
TYPE_CONVERT
TRIM
REPLACE
NULL_CHECK
UNIQUE_CHECK
RANGE_CHECK
REGEX_CHECK
LENGTH_CHECK
ENUM_CHECK
CUSTOM_SQL_CHECK
MASKING
DEDUPLICATE
DEFAULT_VALUE
DATABASE_SINK
```

---

# 附录 G：Pipeline Validator 详细规则

必须校验：

```text
1. 至少一个 SOURCE。
2. 至少存在合法终止节点。
3. DAG 不允许环。
4. Edge source node 存在。
5. Edge target node 存在。
6. sourcePort 必须存在。
7. targetPort 必须存在。
8. 必填 config 完整。
9. Datasource 必须存在。
10. Datasource 不得 DISABLED。
11. Engine capability 能覆盖 Node。
12. SOURCE 不允许普通输入边。
13. SINK 不允许普通输出边。
14. 重复 node_key 不允许。
```

DAG 使用 Kahn Topological Sort。

最低单测：

```text
single chain
branch
merge
cycle
isolated node
missing node
invalid port
```

---

# 附录 H：Execution Engine SPI 详细规格

## H.1 接口

```java
public interface ExecutionEngine {

    String engineType();

    EngineMetadata metadata();

    EngineHealth healthCheck();

    ValidationResult validate(
        ExecutionStagePlan stage
    );

    EngineJob compile(
        ExecutionStagePlan stage
    );

    EngineExecution submit(
        EngineJob job,
        EngineExecutionContext context
    );

    EngineExecutionStatus getStatus(
        String engineExecutionId
    );

    void cancel(
        String engineExecutionId
    );

    EngineLogPage logs(
        String engineExecutionId,
        long offset,
        int limit
    );
}
```

## H.2 Deployment Mode

```java
public enum EngineDeploymentMode {
    EMBEDDED,
    LOCAL_PROCESS,
    REMOTE_SERVICE
}
```

默认：

```text
Native = EMBEDDED
Camel = EMBEDDED
DataX = LOCAL_PROCESS
SeaTunnel = LOCAL_PROCESS / REMOTE_SERVICE
Flink = REMOTE_SERVICE
```

## H.3 Engine Registry

通过 Spring 收集实现：

```java
@Component
public class EngineRegistry {

    private final Map<String, ExecutionEngine> engines;

    public EngineRegistry(List<ExecutionEngine> engineList) {
        this.engines = engineList.stream()
            .collect(Collectors.toUnmodifiableMap(
                ExecutionEngine::engineType,
                Function.identity()
            ));
    }
}
```

禁止核心执行逻辑出现：

```java
if ("DATAX".equals(engine))
```

---

# 附录 I：Execution Planner 可执行规则

## I.1 Planner 输入

```text
PipelineDefinition
NodeMetadata
EngineCapability
RuntimeVariables
PipelineExecutionStrategy
```

## I.2 Planner 输出

```java
public record ExecutionPlan(
    Long pipelineId,
    Integer pipelineVersion,
    List<ExecutionStagePlan> stages,
    Map<String, Object> runtimeVariables
) {}
```

## I.3 Engine 选择优先级（V1 简化）

```text
Node preferredEngine
    >
Pipeline executionStrategy
    >
Node defaultEngine
    >
NATIVE
```

前提：所选引擎必须支持节点。

## I.4 Stage 合并规则

连续节点满足以下条件可以归为同一 Stage：

```text
同一执行引擎
+
引擎支持这些 Node
+
无需跨系统物化
+
数据语义兼容
```

否则切 Stage。

## I.5 示例

Pipeline：

```text
MySQL
↓
Field Mapping
↓
PostgreSQL
```

如果 DataX 支持全部：

```text
Stage 1 = DATAX
```

Pipeline：

```text
MySQL
↓
Sync
↓
Null Check
↓
Webhook
```

可能：

```text
Stage 1 DATAX
Stage 2 NATIVE
Stage 3 CAMEL
```

---

# 附录 J：DataReference 与 Materialization

## J.1 DataReference

```java
public record DataReference(
    DataReferenceType type,
    String uri,
    Map<String, Object> metadata
) {}
```

类型：

```text
DATABASE_TABLE
DATABASE_QUERY
FILE
OBJECT_STORAGE
KAFKA_TOPIC
MEMORY
NONE
```

MEMORY 只用于：

```text
小量控制数据
元数据
小样本
```

禁止用于：

```text
大批量业务数据
```

## J.2 Materialization

跨 Engine：

```text
Stage A
↓
Materialize
↓
DataReference
↓
Stage B
```

V1 首选：

```text
DATABASE_TABLE
```

临时表命名：

```text
dc_tmp_{executionId}_{stageId}
```

失败资源默认保留：

```text
24h
```

后台定时清理。

---

# 附录 K：Execution 状态机与事务边界

## K.1 Execution 状态

```text
CREATED
↓
PLANNING
↓
RUNNING
├── SUCCESS
├── FAILED
└── CANCELLING → CANCELLED
```

## K.2 Node 状态

```text
PENDING
RUNNING
SUCCESS
FAILED
SKIPPED
CANCELLED
```

## K.3 事务边界

禁止：

```text
一个 DB transaction 包住整个 Pipeline
```

正确：

```text
create execution → commit
PLANNING → commit
stage start → commit
node state update → commit
stage finish → commit
execution finish → commit
```

便于服务崩溃后恢复状态。

## K.4 并发

V1：

```text
全局最多 4 个 Pipeline Execution
单 Pipeline 最多 1 个并行 Execution
```

配置：

```yaml
datacraft:
  execution:
    max-concurrent: 4
    max-per-pipeline: 1
```

超出默认：

```text
QUEUE
```

---

# 附录 L：Native Engine 节点规范

## L.1 DATABASE_SOURCE

配置：

```json
{
  "datasourceId": 1001,
  "schema": "public",
  "table": "customer",
  "columns": ["id", "name", "phone"]
}
```

优先输出：

```text
DATABASE_QUERY
```

或：

```text
DATABASE_TABLE
```

## L.2 FILTER

配置：

```json
{
  "expression": "status = 1"
}
```

V1 不自研复杂表达式 DSL。

对于数据库数据，尽量转 SQL Pushdown。

## L.3 SQL_TRANSFORM

配置：

```json
{
  "sql": "SELECT id, trim(name) AS name FROM ${input}"
}
```

V1 仅允许 SELECT。

禁止：

```text
DDL
INSERT
UPDATE
DELETE
多语句
```

必须提供 `SqlSafetyValidator`。

## L.4 FIELD_MAPPING

```json
{
  "mappings": [
    {
      "source": "customer_name",
      "target": "name"
    }
  ]
}
```

## L.5 TYPE_CONVERT

```json
{
  "field": "age",
  "targetType": "INTEGER",
  "onError": "NULL"
}
```

onError：

```text
FAIL
NULL
DEFAULT
```

## L.6 MASKING

内置：

```text
PHONE
EMAIL
ID_CARD
CUSTOM
```

PHONE：

```text
138****1234
```

---

# 附录 M：Data Quality 详细规格

## M.1 统一 SPI

```java
public interface QualityRuleProvider {

    String type();

    QualityRuleMetadata metadata();

    QualityExecutionSpec compile(
        NodeDefinition node,
        DataReference input
    );
}
```

## M.2 NULL_CHECK

```json
{
  "field": "phone"
}
```

SQL：

```sql
field IS NULL
```

## M.3 UNIQUE_CHECK

```sql
GROUP BY field
HAVING COUNT(*) > 1
```

## M.4 RANGE_CHECK

```json
{
  "field": "age",
  "min": 0,
  "max": 120
}
```

## M.5 REGEX_CHECK

必须通过 SQL Dialect 生成数据库差异语法。

接口：

```text
SqlDialect
```

实现：

```text
MySqlDialect
PostgresqlDialect
```

## M.6 CUSTOM_SQL_CHECK

用户输入条件：

```text
age < 0 OR age > 120
```

DataCraft 生成：

```sql
SELECT *
FROM input
WHERE age < 0 OR age > 120
```

禁止多语句及 DDL/DML。

---

# 附录 N：DataX 集成详细规格

## N.1 版本

```text
V1.2
```

## N.2 运行模式

```text
LOCAL_PROCESS
```

## N.3 模块

```text
datacraft-engine-datax
├── DataXExecutionEngine
├── DataXCompiler
├── DataXJobBuilder
├── DataXProcessManager
├── DataXLogParser
└── DataXMetricsParser
```

## N.4 配置

```yaml
datacraft:
  engines:
    datax:
      enabled: false
      home: /opt/datax
      python: /usr/bin/python3
      job-temp-dir: ./data/datax/jobs
      max-concurrent: 2
```

## N.5 执行流程

```text
ExecutionStagePlan
↓
DataXCompiler
↓
DataX Job JSON
↓
ProcessBuilder
↓
datax.py job.json
↓
parse stdout/stderr
↓
EngineExecutionStatus
```

必须记录：

```text
PID
Start Time
Exit Code
Stdout
Stderr
Rows
Duration
```

## N.6 核心原则

禁止把 DataX Job JSON 作为 DataCraft Pipeline 的主存储模型。

DataCraft Pipeline 永远是 Source of Truth。

---

# 附录 O：Apache Camel 集成详细规格

## O.1 版本

```text
V1.5
```

## O.2 模式

```text
EMBEDDED
```

## O.3 Managed CamelContext

整个 DataCraft Server 统一管理 CamelContext。

禁止：

```text
每 Node 一个 CamelContext
```

## O.4 CamelRuntimeManager

职责：

```text
startRoute
stopRoute
removeRoute
routeStatus
cleanup
```

## O.5 第一批 Camel 能力

```text
HTTP
FTP
SFTP
FILE
Kafka
RabbitMQ
Mail
```

按需加载依赖，不把 Camel 全量组件一次引入。

---

# 附录 P：Scheduler 详细规则

V1 使用 Quartz。

Quartz Job 只负责：

```text
触发 ExecutionService.submit()
```

禁止在 Quartz Job 内直接编写 Pipeline 执行逻辑。

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

# 附录 Q：SSE 事件规范

接口：

```http
GET /api/v1/executions/{id}/events
```

事件名：

```text
execution.started
stage.started
node.started
node.log
node.success
node.failed
stage.success
stage.failed
execution.success
execution.failed
execution.cancelled
```

Payload：

```json
{
  "event": "node.success",
  "executionId": 1001,
  "stageId": 2001,
  "nodeKey": "null_check_1",
  "timestamp": "2026-09-07T10:00:00Z",
  "data": {
    "inputRows": 100000,
    "errorRows": 123
  }
}
```

---

# 附录 R：前端路由与页面规格

## R.1 路由

```text
/login
/dashboard
/datasources
/datasources/:id
/assets
/assets/:datasetId
/pipelines
/pipelines/:id/editor
/quality
/quality/results/:id
/executions
/executions/:id
/system/users
/system/roles
/system/engines
```

## R.2 Layout

```text
┌──────────────────────────────────────────────┐
│ DataCraft                              User  │
├─────────────┬────────────────────────────────┤
│ Dashboard   │                                │
│ Datasource  │                                │
│ Assets      │          Router View           │
│ Pipeline    │                                │
│ Quality     │                                │
│ Execution   │                                │
│ System      │                                │
└─────────────┴────────────────────────────────┘
```

## R.3 Pipeline Editor

```text
┌──────────────────────────────────────────────────────────┐
│ ← Pipelines  Customer Governance      Save Validate Run │
├──────────────┬──────────────────────────┬────────────────┤
│ Node Library │ Canvas                   │ Config Panel   │
│              │                          │                │
│ Source       │ [MySQL]                  │ Node Name      │
│ Transform    │    ↓                     │ Datasource     │
│ Quality      │ [Filter]                 │ Schema         │
│ Governance   │    ↓                     │ Table          │
│ Sink         │ [NullCheck]              │                │
│              │    ↓                     │ Engine=AUTO    │
│              │ [PostgreSQL]             │                │
└──────────────┴──────────────────────────┴────────────────┘
```

推荐宽度：

```text
Node Library = 220px
Config Panel = 320px
Canvas = flex
```

## R.4 Node 交互

拖入：

```text
生成 nodeKey
保存 x/y
```

点击：

```text
右侧配置面板
```

复制：

```text
复制 config
生成新 nodeKey
位置偏移 20px
```

删除：

```text
同步删除 Edge
```

---

# 附录 S：安全与审计

## S.1 JWT

```text
Authorization: Bearer <token>
```

Public：

```text
/api/v1/auth/login
/actuator/health
```

其他 API 默认鉴权。

## S.2 RBAC

ADMIN：

```text
全部
```

DEVELOPER：

```text
Datasource read/write
Metadata read
Pipeline read/write/execute
Quality read
Execution read
```

VIEWER：

```text
read only
```

## S.3 审计必须覆盖

```text
login
datasource create/update/delete
pipeline create/update/delete/publish
execution submit/cancel/retry
schedule create/update/delete
engine config update
```

---

# 附录 T：日志规范

日志 MDC：

```text
traceId
executionId
stageId
nodeKey
```

禁止日志输出：

```text
password
password_cipher
JWT
secret key
含密码 JDBC URL
```

---

# 附录 U：配置约定

示例：

```yaml
spring:
  application:
    name: datacraft

  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:datacraft}
    username: ${DB_USER:datacraft}
    password: ${DB_PASSWORD:datacraft}

  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}

  flyway:
    enabled: true

datacraft:
  security:
    jwt-secret: ${DATACRAFT_JWT_SECRET:change-me}
  crypto:
    secret-key: ${DATACRAFT_SECRET_KEY:}
  execution:
    max-concurrent: 4
    max-per-pipeline: 1
```

生产环境禁止使用默认 secret。

---

# 附录 V：测试矩阵

## V.1 Unit Test 必须覆盖

```text
PipelineValidator
DagTopologicalSorter
NodeRegistry
EngineRegistry
ExecutionPlanner
SqlSafetyValidator
```

## V.2 Integration Test

使用 Testcontainers：

```text
PostgreSQL
MySQL
Redis
```

## V.3 DataX

```text
DataXCompilerTest
DataXJobBuilderTest
DataXLogParserTest
```

## V.4 Camel

```text
CamelCompilerTest
CamelRuntimeManagerTest
```

## V.5 Frontend

最低：

```text
TypeScript compile
npm run build
```

后续：

```text
Vitest
Playwright
```

---

# 附录 W：各版本进一步拆成可执行 Phase

## W.1 V0.1 Foundation

### Phase 0A — Repository

创建：

```text
parent pom
server modules
web project
.gitignore
README
docker-compose
```

验收：

```text
mvn -q -DskipTests package
npm run build
```

### Phase 0B — Infrastructure

完成：

```text
PostgreSQL
Redis
Flyway
Actuator
统一 Response
统一 Exception
```

### Phase 0C — Basic Web Layout

完成：

```text
Vue Router
Pinia
Element Plus
Base Layout
Placeholder Routes
```

禁止进入业务功能。

## W.2 V0.2 Datasource / Metadata

### Phase 1A — Datasource CRUD

```text
Entity
Mapper
Service
Controller
DTO
Flyway
UI List
Create/Edit Drawer
```

### Phase 1B — Connection Test

```text
JDBC URL Builder
AES-GCM
Test API
UI Status
```

### Phase 1C — Metadata

```text
Collector SPI
MySQL Collector
PostgreSQL Collector
Dataset tables
Asset UI
```

## W.3 V0.3 Pipeline Designer

### Phase 2A — Pipeline Persistence

```text
Pipeline CRUD
Node
Edge
```

### Phase 2B — Node Metadata

```text
NodeProvider
NodeRegistry
ConfigSchema
GET /node-types
```

### Phase 2C — Vue Flow

```text
Node Library
Canvas
Config Panel
Save/Load
```

### Phase 2D — Validation

```text
DAG
Node config
Port
Datasource reference
```

## W.4 V0.4 Native Engine

### Phase 3A — Engine API

```text
ExecutionEngine
EngineRegistry
NativeEngine skeleton
```

### Phase 3B — Planner

```text
ExecutionPlan
Stage
DataReference
Planner
```

### Phase 3C — First Runnable Pipeline

```text
DATABASE_SOURCE
FILTER
DATABASE_SINK
```

## W.5 V0.5 Transform

```text
SQL Transform
Field Mapping
Type Convert
Trim
Replace
```

## W.6 V0.6 Quality

```text
Null
Unique
Range
Regex
Length
Enum
Custom SQL
Result
Sample
```

## W.7 V0.7 Scheduler / Monitor

```text
Quartz
Cron
Execution History
Retry
Cancel
SSE
Execution Detail UI
```

## W.8 V1.1 Engine SPI Hardening

```text
Engine Management
Capabilities
Health
Native SPI-only path
```

## W.9 V1.2 DataX

```text
Compiler
Process Manager
Logs
Metrics
Cancel
```

## W.10 V1.3 Hybrid

```text
Multi Stage
Materialization
Stage dependency
Temp cleanup
```

## W.11 V1.5 Camel

```text
CamelContext
Route Manager
HTTP
SFTP
Kafka
Webhook
```

---

# 附录 X：Codex/Luna 第一轮执行提示词

将本文件放到仓库根目录并命名为 `DESIGN.md` 后，第一次执行使用：

```text
你接手的是一个完全空的 DataCraft Git 仓库。

先完整阅读：
DESIGN.md
AGENTS.md
DEV_PROGRESS.md

本次只执行：
V0.1 / Phase 0A + Phase 0B + Phase 0C

目标是把空仓库初始化成一个可持续开发、可编译、可运行的工程骨架。

开始编码前：
1. 检查仓库内容和 Git 状态。
2. 输出实施计划。
3. 列出预计创建的主要目录和文件。
4. 说明 Maven 模块依赖方向。
5. 然后开始实现。

本阶段允许：
Java 21
Spring Boot 3
Maven Multi-module
PostgreSQL
Redis
Flyway
Actuator
Vue3
TypeScript
Vite
Element Plus
Pinia
Vue Router
Docker Compose

本阶段禁止：
Datasource 业务
Metadata 业务
Pipeline 业务
DataX
Camel
SeaTunnel
Flink

完成后实际执行：
mvn test
mvn package
npm install
npm run build
docker compose config

失败必须修复后重跑。

最后更新 DEV_PROGRESS.md，并汇报：
- 完成内容
- 创建的模块
- 测试结果
- 构建结果
- 当前风险
- 下一 Phase

不要提前进入下一阶段。
```

---

# 附录 Y：Codex/Luna 后续通用提示词

```text
继续 DataCraft 开发。

先阅读：
DESIGN.md
AGENTS.md
DEV_PROGRESS.md

检查当前代码、Git 状态和已有测试。

本次只执行：
<明确 Version / Phase>

开始编码前先输出计划。
实现完成后执行全部相关测试和构建。
修复本次修改引入的问题。
更新 DEV_PROGRESS.md。

不要进入下一个 Phase。
不要重新设计 DESIGN.md 已确定的核心架构。
```

---

# 附录 Z：模型执行边界

即使使用 Luna 的最高推理级别，也不建议把任务写成：

```text
“根据 DESIGN.md 一次把 DataCraft 全部开发完成。”
```

推荐让高能力模型做的是：

```text
理解整个设计
↓
只执行当前 Phase
↓
跨多个文件完整落地
↓
真实运行构建和测试
↓
自动根据错误修复
↓
更新进度
```

这样既能发挥 Luna 在长上下文和跨文件编码上的优势，又能避免大项目一次性生成导致的架构漂移。

最终原则：

> DESIGN.md 负责“把决定提前做清楚”；Luna 负责“按照决定高质量执行”。

