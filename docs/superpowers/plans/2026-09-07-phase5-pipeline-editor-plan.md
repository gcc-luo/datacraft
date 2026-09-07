# Phase 5 Pipeline Editor 实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:executing-plans 逐任务实现本计划，并在每个检查点运行测试。

**目标：** 在现有 Phase 4 REST API 上交付一个可认证访问、可拖拽连线、可编辑并保存 Pipeline DAG 的 Vue Flow 编辑器。

**架构：** 前端通过类型化 API 客户端消费 Pipeline 和 NodeMetadata DTO；编辑器把后端节点/边映射为 Vue Flow 图元素，保存时反向转换为后端请求。列表、画布节点、节点库和属性检查器各自承担单一职责，执行引擎和运行能力不进入本阶段。

**技术栈：** Vue 3 `<script setup>`、TypeScript、Vue Flow、Vue Router、Axios、Vitest、Vue Test Utils、SCSS。

---

### 任务 1：建立 Pipeline 前端类型、API 和映射测试

**文件：**
- 创建：`datacraft-web/src/types/pipeline.ts`
- 创建：`datacraft-web/src/api/pipelines.ts`
- 创建：`datacraft-web/src/utils/pipelineGraph.ts`
- 创建：`datacraft-web/src/api/pipelines.test.ts`
- 创建：`datacraft-web/src/utils/pipelineGraph.test.ts`

- [x] **步骤 1：编写失败测试**

测试 API 客户端调用 `/v1/pipelines`、`/v1/node-types` 并解包 `ApiResponse`；测试 `detailToCanvas` 将后端 `nodeKey` 映射到 Vue Flow `id`，并将边端点映射到节点 ID；测试 `canvasToRequest` 保留节点位置、配置和边端口。

- [x] **步骤 2：运行测试验证失败**

运行：`npm test -- --run src/api/pipelines.test.ts src/utils/pipelineGraph.test.ts`

预期：FAIL，因类型、API 函数和图转换函数尚不存在。

- [x] **步骤 3：实现最小类型和函数**

定义 `PipelineStatus`、`ExecutionStrategy`、`PipelineResponse`、`PipelineDetailResponse`、`PipelineNodeResponse`、`PipelineEdgeResponse`、`PipelineRequest`、`PipelineNodeRequest`、`PipelineEdgeRequest`、`NodeMetadataResponse`，以及带 `data.nodeKey/nodeType/configJson/preferredEngine` 的 `PipelineCanvasNode`。API 函数使用已有 `http` 和 `ApiResponse` 解包模式：

```ts
export async function listPipelines(): Promise<PipelineResponse[]>
export async function getPipeline(id: number): Promise<PipelineDetailResponse>
export async function createPipeline(request: PipelineRequest): Promise<PipelineResponse>
export async function updatePipeline(id: number, request: PipelineRequest): Promise<PipelineResponse>
export async function deletePipeline(id: number): Promise<void>
export async function listNodeTypes(): Promise<NodeMetadataResponse[]>
```

图转换使用 Vue Flow 的 `Node` / `Edge` 类型，保存请求使用 `sourceNodeKey` / `targetNodeKey`，不得把 API Entity 直接绑定到模板。

- [x] **步骤 4：运行测试验证通过**

运行：`npm test -- --run src/api/pipelines.test.ts src/utils/pipelineGraph.test.ts`

预期：测试通过，覆盖 API 路径、DTO 解包和图结构双向转换。

- [x] **步骤 5：Commit**

```shell
git add datacraft-web/src/api/pipelines.ts datacraft-web/src/api/pipelines.test.ts datacraft-web/src/types/pipeline.ts datacraft-web/src/utils/pipelineGraph.ts datacraft-web/src/utils/pipelineGraph.test.ts
git commit -m "feat(web): add pipeline api and graph mapping"
```

### 任务 2：实现 Pipeline 列表页和编辑器路由

**文件：**
- 创建：`datacraft-web/src/views/PipelineListView.vue`
- 创建：`datacraft-web/src/views/PipelineListView.test.ts`
- 修改：`datacraft-web/src/router/index.ts`

- [x] **步骤 1：编写失败测试**

测试 mock `listPipelines` 后渲染名称、状态、版本和执行策略；测试点击新建跳转 `/pipelines/new`；测试已有行点击跳转 `/pipelines/:id`；测试空状态和请求错误。

- [x] **步骤 2：运行测试验证失败**

运行：`npm test -- --run src/views/PipelineListView.test.ts`

预期：FAIL，因页面和路由尚不存在。

- [x] **步骤 3：实现列表页和路由**

列表页复用现有 `page-heading`、`inline-error`、`primary-action` 视觉基础，新增 Pipeline 状态色、节点/边统计摘要、空状态和删除入口。路由顺序为：

```ts
{ path: 'pipelines', name: 'pipelines', component: () => import('../views/PipelineListView.vue') },
{ path: 'pipelines/:id', name: 'pipeline-editor', component: () => import('../views/PipelineEditorView.vue') },
```

两条路由必须位于 catch-all 之前，继续使用现有全局认证守卫。

- [x] **步骤 4：运行测试验证通过**

运行：`npm test -- --run src/views/PipelineListView.test.ts`

预期：列表、空状态、路由跳转测试通过。

- [x] **步骤 5：Commit**

```shell
git add datacraft-web/src/views/PipelineListView.vue datacraft-web/src/views/PipelineListView.test.ts datacraft-web/src/router/index.ts
git commit -m "feat(web): add pipeline list route"
```

### 任务 3：实现三栏编辑器的节点库和画布节点

**文件：**
- 创建：`datacraft-web/src/components/pipeline/PipelinePalette.vue`
- 创建：`datacraft-web/src/components/pipeline/PipelinePalette.test.ts`
- 创建：`datacraft-web/src/components/pipeline/PipelineNode.vue`
- 创建：`datacraft-web/src/components/pipeline/PipelineNode.test.ts`
- 创建：`datacraft-web/src/styles/pipeline.scss`

- [x] **步骤 1：编写失败测试**

测试节点库按 `SOURCE`、`TRANSFORM`、`SINK` 分类显示元数据并发出拖拽数据；测试画布节点显示元数据名称、节点 key、首选引擎，并提供 source/target handles；测试搜索只保留匹配节点类型或名称。

- [x] **步骤 2：运行测试验证失败**

运行：`npm test -- --run src/components/pipeline/PipelinePalette.test.ts src/components/pipeline/PipelineNode.test.ts`

预期：FAIL，因组件尚不存在。

- [x] **步骤 3：实现组件和局部样式**

`PipelinePalette` 只接受 `NodeMetadataResponse[]`，使用 `dataTransfer.setData('application/datacraft-node', metadata.type)`；`PipelineNode` 使用 Vue Flow `Handle` 和 `Position`，节点卡片颜色由 `category` 决定。`pipeline.scss` 定义深靛蓝画布、低对比网格、蓝青/琥珀/薄荷节点强调色、检查器表单和移动端折叠规则，并添加 `prefers-reduced-motion` 降级。

- [x] **步骤 4：运行测试验证通过**

运行：`npm test -- --run src/components/pipeline/PipelinePalette.test.ts src/components/pipeline/PipelineNode.test.ts`

预期：节点库和节点渲染测试通过。

- [x] **步骤 5：Commit**

```shell
git add datacraft-web/src/components/pipeline datacraft-web/src/styles/pipeline.scss
git commit -m "feat(web): add pipeline palette and canvas node"
```

### 任务 4：实现属性检查器

**文件：**
- 创建：`datacraft-web/src/components/pipeline/PipelineInspector.vue`
- 创建：`datacraft-web/src/components/pipeline/PipelineInspector.test.ts`

- [x] **步骤 1：编写失败测试**

测试无节点选中时编辑 Pipeline 名称、描述、状态和执行策略；测试选中节点时编辑节点名称、坐标、配置 JSON、首选引擎；测试配置 JSON 非法时显示提示且不触发保存。

- [x] **步骤 2：运行测试验证失败**

运行：`npm test -- --run src/components/pipeline/PipelineInspector.test.ts`

预期：FAIL，因检查器组件尚不存在。

- [x] **步骤 3：实现检查器**

组件接收 `pipeline` 和 `selectedNode`，通过 `update:pipeline` / `update:selectedNode` 发出浅拷贝更新；配置 JSON 在失焦时解析校验，仅把合法文本交给父组件；首选引擎使用元数据 `supportedEngines` 选项，默认显示 `AUTO`。不实现任何专用引擎配置或运行按钮。

- [x] **步骤 4：运行测试验证通过**

运行：`npm test -- --run src/components/pipeline/PipelineInspector.test.ts`

预期：检查器行为测试通过。

- [x] **步骤 5：Commit**

```shell
git add datacraft-web/src/components/pipeline/PipelineInspector.vue datacraft-web/src/components/pipeline/PipelineInspector.test.ts
git commit -m "feat(web): add pipeline inspector"
```

### 任务 5：组装 PipelineEditorView，接入保存/删除和拖拽连线

**文件：**
- 创建：`datacraft-web/src/views/PipelineEditorView.vue`
- 创建：`datacraft-web/src/views/PipelineEditorView.test.ts`
- 修改：`datacraft-web/src/main.ts`

- [x] **步骤 1：编写失败测试**

测试已有 Pipeline 加载详情和节点元数据；测试拖拽节点类型到画布后生成唯一 node key；测试连线形成边；测试编辑后保存调用 PUT 并显示新版本；测试 `/pipelines/new` 保存调用 POST；测试删除调用 DELETE 后返回列表；测试保存错误保留未保存标记。

- [x] **步骤 2：运行测试验证失败**

运行：`npm test -- --run src/views/PipelineEditorView.test.ts`

预期：FAIL，因编辑器尚不存在。

- [x] **步骤 3：实现编辑器**

使用 `VueFlow`、`Background`、`Controls`、`MiniMap`，开启 `v-model:nodes` / `v-model:edges`。通过 `nodeTypes: { pipeline: PipelineNode }` 注册自定义节点；`@connect` 生成边；`@drop` 根据元数据创建节点；键盘 Delete 删除选中元素。保存前使用 `canvasToRequest` 构建请求：新建使用 POST，已有使用 PUT；保存成功刷新详情/版本并清除 dirty 状态。加载请求使用 `Promise.all`；所有错误显示页面内消息。

- [x] **步骤 4：运行测试验证通过**

运行：`npm test -- --run src/views/PipelineEditorView.test.ts`

预期：加载、拖放、连线、保存、删除和错误状态测试通过。

- [x] **步骤 5：Commit**

```shell
git add datacraft-web/src/views/PipelineEditorView.vue datacraft-web/src/views/PipelineEditorView.test.ts datacraft-web/src/main.ts
git commit -m "feat(web): build pipeline flow editor"
```

### 任务 6：样式接入、全量验证和进度文档

**文件：**
- 修改：`datacraft-web/src/main.ts`
- 修改：`DEV_PROGRESS.md`
- 修改：`README.md`
- 创建：`docs/adr/0010-phase5-pipeline-editor.md`

- [x] **步骤 1：编写回归测试**

补充路由/编辑器的未认证访问测试，并确认既有 AppLayout、登录、数据源、资产目录测试继续覆盖。

- [x] **步骤 2：运行全量前端测试**

运行：`npm test -- --run`

预期：所有前端测试通过，且没有新增 console error。

- [x] **步骤 3：接入样式并构建**

在 `main.ts` 引入 `@vue-flow/core/dist/style.css`、`@vue-flow/core/dist/theme-default.css` 和 `styles/pipeline.scss`，确保 Vue Flow 全局样式未被 scoped；运行 `npm run build`。

- [x] **步骤 4：更新阶段文档**

将 `DEV_PROGRESS.md` 更新为 V0.4 / Phase 5 Completed，下一阶段改为 Phase 6 Native Execution；在 README 增加编辑器入口和边界说明；ADR 记录本阶段的三栏布局、元数据驱动和不接入执行引擎的决策。

- [x] **步骤 5：运行最终验证并 Commit**

运行：

```shell
npm test -- --run
npm run build
cd ../datacraft-server
mvn '-Denforcer.skip=true' '-Dmaven.compiler.release=17' test '-DskipTests=false'
```

预期：前端测试、前端生产构建和后端全量测试均退出码 0；JDK 21 可用时再运行不带兼容参数的官方命令。

```shell
git add DEV_PROGRESS.md README.md docs/adr/0010-phase5-pipeline-editor.md datacraft-web/src/main.ts
git commit -m "docs(web): complete phase 5 pipeline editor"
```
