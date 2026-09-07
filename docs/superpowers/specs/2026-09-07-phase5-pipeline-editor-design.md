# Phase 5 Pipeline Editor 设计

## 目标

在 Phase 4 Pipeline 控制面 API 之上，交付第一个可用的 Pipeline 可视化编辑器。用户可以浏览已有 Pipeline、创建草稿、从节点库放置节点、连接 DAG、编辑节点属性并保存图结构。

## 范围

本阶段包含：

- Pipeline 列表、创建草稿、打开详情、更新、删除
- Vue Flow 画布：节点拖拽放置、节点移动、边连接、节点删除
- 后端 `GET /node-types` 驱动的节点库，不在前端硬编码配置表单类型
- 右侧检查器：编辑 Pipeline 基本信息和选中节点的名称、坐标、配置 JSON、首选引擎
- 保存状态、加载状态、错误提示和空状态
- `/pipelines` 与 `/pipelines/:id` 路由，以及保持既有鉴权守卫

本阶段不包含：

- Pipeline 运行、发布、调度、质量规则和执行日志
- DataX、Camel、SeaTunnel、Flink 或 Native Execution Engine 接入
- 多人协作、撤销/重做、版本对比、自动布局和大图性能优化
- 为每个节点类型实现专用配置表单；配置 JSON 先作为稳定的通用边界

## 设计方向

采用“控制室式数据编排”视觉方向：

- 画布使用深靛蓝渐变与低对比网格，形成独立的工作空间氛围
- 节点按来源、处理、输出使用蓝青、暖琥珀、薄荷绿三组强调色
- 顶部工具条呈现 Pipeline 身份、草稿状态、保存动作和未保存提示
- 左侧节点库是可搜索的窄栏；中央画布拥有最高视觉权重；右侧检查器保持纸张般的高亮背景，强化编辑焦点
- 使用窄字宽的标签、清晰的数字坐标和克制的阴影，保持与现有控制台的专业感，同时让编辑器区别于普通 CRUD 页面
- 动效只用于节点进入、选中和保存反馈，并尊重 `prefers-reduced-motion`

## 前端架构

```text
PipelineListView
  ├── pipeline API client
  └── route to PipelineEditorView

PipelineEditorView
  ├── pipeline API client
  ├── node metadata API client
  ├── Vue Flow canvas
  ├── node palette drag/drop
  └── inspector state
```

API 客户端把 Phase 4 的 `ApiResponse<T>` 解包为类型化数据。编辑器内部使用 Vue Flow 的 `Node[]` / `Edge[]`，保存时转换成后端的 `PipelineNodeRequest[]` / `PipelineEdgeRequest[]`；不把持久化 Entity 结构泄漏到模板。

节点元数据决定节点库的显示名称、分类、图标、支持引擎和配置 Schema 摘要。编辑器只展示这些元数据，不据此创建任何执行引擎对象。

## 交互与状态

- 列表页首次加载 Pipeline；点击“新建”创建空草稿并进入编辑器；点击行进入已有编辑器
- 编辑器加载 Pipeline 详情与节点元数据并行请求。节点数据转换为 Vue Flow 节点，边数据转换为 Vue Flow 边
- 节点库条目可拖进画布；新节点使用唯一 `nodeKey`、元数据默认引擎和默认 `{}` 配置
- 连接事件只更新本地边；Vue Flow 负责交互，后端保存时由服务端做最终 DAG 校验
- 选中画布空白处显示 Pipeline 检查器；选中节点显示节点检查器
- 保存按钮在创建时 POST、已有 Pipeline 时 PUT；成功后更新版本和时间，并清除未保存标记
- 删除先调用 DELETE，成功后返回列表；运行按钮本阶段不显示，避免暗示执行能力已完成
- API 错误显示在页面内，不在控制台输出配置内容或凭据

## 文件职责

- `src/api/pipelines.ts`：Pipeline CRUD 与节点元数据请求
- `src/types/pipeline.ts`：API DTO、节点库和编辑器映射类型
- `src/views/PipelineListView.vue`：列表、空状态、创建入口
- `src/views/PipelineEditorView.vue`：三栏编辑器与页面状态
- `src/components/pipeline/PipelineNode.vue`：画布节点视觉呈现与端口
- `src/components/pipeline/PipelinePalette.vue`：元数据驱动节点库与拖拽源
- `src/components/pipeline/PipelineInspector.vue`：Pipeline/Node 属性编辑
- `src/styles/pipeline.scss`：编辑器局部视觉系统和响应式布局
- `src/router/index.ts`：Pipeline 列表与编辑器路由
- 对应 `.test.ts`：API 调用、列表交互、编辑器转换和保存行为

## 错误处理

- 列表、详情、节点元数据请求失败时显示可重试的页面内错误
- 保存失败保留本地编辑状态和未保存提示
- 服务端 `PIPELINE_VALIDATION` 错误直接展示服务端消息，供用户修正节点/边
- 未找到 Pipeline 时返回列表并提示资源已不存在
- 不记录 `configJson` 内容到日志；配置输入仅作为 JSON 文本保存

## 验收标准

- `/pipelines` 不再显示占位页，可列出 Pipeline 并新建空草稿
- `/pipelines/:id` 可加载三栏编辑器，节点库来自 `/api/v1/node-types`
- 至少可以放置 `DATABASE_SOURCE`、`FILTER`、`DATABASE_SINK`，拖动并连接为 DAG
- 右侧检查器编辑名称、节点名称、节点配置和首选引擎后，刷新详情仍能看到保存结果
- 删除后返回列表且列表不再显示该 Pipeline
- 匿名访问仍被路由守卫拦截
- 前端测试和生产构建通过；后端 Phase 4 测试保持通过
