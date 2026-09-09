<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref, shallowRef } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { VueFlow } from '@vue-flow/core'
import type { Connection } from '@vue-flow/core'
import { createPipeline, deletePipeline, getPipeline, listNodeTypes, updatePipeline } from '../api/pipelines'
import type { ExecutionStrategy, NodeMetadataResponse, PipelineCanvasEdge, PipelineCanvasNode, PipelineNodeData, PipelineNodePatch, PipelineStatus } from '../types/pipeline'
import { canvasToRequest, detailToCanvas, nodeFromMetadata } from '../utils/pipelineGraph'
import PipelineInspector from '../components/pipeline/PipelineInspector.vue'
import PipelineNode from '../components/pipeline/PipelineNode.vue'
import PipelinePalette from '../components/pipeline/PipelinePalette.vue'

const route = useRoute()
const router = useRouter()
const pipelineId = computed(() => Number(route.params.id))
const isNew = computed(() => route.params.id === 'new')

interface PipelineEditorState {
  id: number | null
  name: string
  description: string | null
  status: PipelineStatus
  executionStrategy: ExecutionStrategy
  version: number
}

const pipeline = reactive<PipelineEditorState>({
  id: null, name: '新建 Pipeline', description: null, status: 'DRAFT', executionStrategy: 'AUTO', version: 0,
})
const nodeMetadata = ref<NodeMetadataResponse[]>([])
const nodes = shallowRef<PipelineCanvasNode[]>([])
const edges = shallowRef<PipelineCanvasEdge[]>([])
const selectedNodeId = ref<string | null>(null)
const loading = ref(true)
const saving = ref(false)
const dirty = ref(false)
const errorMessage = ref('')
const notice = ref('')
const inspectorOpen = ref(true)

const selectedNode = computed<(PipelineNodeData & { position: { x: number; y: number } }) | null>(() => {
  const node = nodes.value.find((item) => item.id === selectedNodeId.value)
  return node ? { ...(node.data as PipelineNodeData), position: node.position } : null
})
const nodeTypes = { pipeline: PipelineNode }

function metadataFor(type: string) {
  return nodeMetadata.value.find((item) => item.type === type)
}

function setPipelineFromResponse(response: { id: number; name: string; description: string | null; status: PipelineStatus; executionStrategy: ExecutionStrategy; version: number }) {
  Object.assign(pipeline, response)
}

async function loadEditor() {
  loading.value = true
  errorMessage.value = ''
  try {
    const metadata = await listNodeTypes()
    nodeMetadata.value = metadata
    if (isNew.value) return

    const detail = await getPipeline(pipelineId.value)
    setPipelineFromResponse(detail)
    const graph = detailToCanvas(detail)
    nodes.value = graph.nodes.map((node) => {
      const data = node.data as PipelineNodeData
      return { ...node, data: { ...data, metadata: metadataFor(data.nodeType) } }
    })
    edges.value = graph.edges
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '加载 Pipeline 失败'
  } finally {
    loading.value = false
  }
}

function markDirty() {
  dirty.value = true
  notice.value = ''
}

function updatePipelineProperties(patch: Partial<PipelineEditorState>) {
  Object.assign(pipeline, patch)
  markDirty()
}

function updateSelectedNode(patch: PipelineNodePatch) {
  if (!selectedNodeId.value) return
  const { position, ...dataPatch } = patch
  nodes.value = nodes.value.map((node) => node.id === selectedNodeId.value
    ? { ...node, position: position || node.position, data: { ...(node.data as PipelineNodeData), ...dataPatch } } as PipelineCanvasNode
    : node)
  markDirty()
}

function onNodeClick(payload: { node?: PipelineCanvasNode } | PipelineCanvasNode) {
  const node = 'id' in payload ? payload : payload.node
  selectedNodeId.value = node?.id || null
  inspectorOpen.value = true
}

function nodeKeyFor(type: string) {
  const prefix = type.toLowerCase().replace(/[^a-z0-9]+/g, '-')
  let index = 1
  while (nodes.value.some((node) => node.id === `${prefix}-${index}`)) index += 1
  return `${prefix}-${index}`
}

function dropNode(event: DragEvent) {
  const type = event.dataTransfer?.getData('application/datacraft-node')
  const metadata = type ? metadataFor(type) : undefined
  if (!type || !metadata) return
  const target = event.currentTarget as HTMLElement
  const bounds = target.getBoundingClientRect()
  const node = nodeFromMetadata(type, metadata, { x: Math.max(24, event.clientX - bounds.left - 90), y: Math.max(50, event.clientY - bounds.top - 40) }, nodeKeyFor(type))
  ;(node.data as PipelineNodeData).metadata = metadata
  nodes.value = [...nodes.value, node]
  selectedNodeId.value = node.id
  inspectorOpen.value = true
  markDirty()
}

function onConnect(connection: Connection) {
  if (!connection.source || !connection.target) return
  edges.value = [...edges.value, { ...connection, id: `e-${connection.source}-${connection.target}-${Date.now()}`, type: 'smoothstep', animated: true } as PipelineCanvasEdge]
  markDirty()
}

function removeSelectedNode() {
  if (!selectedNodeId.value) return
  const id = selectedNodeId.value
  nodes.value = nodes.value.filter((node) => node.id !== id)
  edges.value = edges.value.filter((edge) => edge.source !== id && edge.target !== id)
  selectedNodeId.value = null
  markDirty()
}

async function saveEditor() {
  if (!pipeline.name.trim()) {
    errorMessage.value = 'Pipeline 名称不能为空'
    return
  }
  saving.value = true
  errorMessage.value = ''
  const creating = isNew.value
  try {
    const request = canvasToRequest(pipeline.name.trim(), pipeline.description, pipeline.status, pipeline.executionStrategy, nodes.value, edges.value)
    const response = creating ? await createPipeline(request) : await updatePipeline(pipelineId.value, request)
    setPipelineFromResponse(response)
    dirty.value = false
    notice.value = '已保存'
    if (creating) await router.replace({ name: 'pipeline-editor', params: { id: response.id } })
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '保存 Pipeline 失败'
  } finally {
    saving.value = false
  }
}

async function removeEditor() {
  if (isNew.value) {
    await router.push({ name: 'pipelines' })
    return
  }
  if (!window.confirm(`确认删除 Pipeline「${pipeline.name}」吗？`)) return
  try {
    await deletePipeline(pipelineId.value)
    await router.push({ name: 'pipelines' })
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '删除 Pipeline 失败'
  }
}

function goBack() {
  router.push({ name: 'pipelines' })
}

function closeInspector() {
  inspectorOpen.value = false
}

function openInspector() {
  inspectorOpen.value = true
}

function onInspectorKeydown(event: KeyboardEvent) {
  if (event.key === 'Escape' && inspectorOpen.value) closeInspector()
}

onMounted(() => {
  loadEditor()
  window.addEventListener('keydown', onInspectorKeydown)
})
onBeforeUnmount(() => window.removeEventListener('keydown', onInspectorKeydown))
</script>

<template>
  <div class="pipeline-editor-page" data-testid="pipeline-editor">
    <header class="pipeline-editor__toolbar">
      <button class="pipeline-editor__back" type="button" aria-label="返回 Pipeline 列表" @click="goBack">←</button>
      <div class="pipeline-editor__identity">
        <span class="pipeline-editor__identity-mark">⌘</span>
        <div><h1>{{ pipeline.name }}</h1><p>{{ isNew ? 'NEW PIPELINE' : `VERSION ${pipeline.version}` }}</p></div>
      </div>
      <div class="pipeline-editor__state" :class="{ 'pipeline-editor__dirty': dirty }"><i class="pipeline-editor__state-dot"></i>{{ dirty ? '有未保存修改' : '已同步' }}</div>
      <div class="pipeline-editor__actions">
        <button data-testid="delete-pipeline" type="button" @click="removeEditor">删除</button>
        <button v-if="!inspectorOpen" class="pipeline-editor__inspector-open" data-testid="inspector-open" type="button" @click="openInspector">属性检查器</button>
        <button class="pipeline-editor__save" data-testid="save-pipeline" type="button" :disabled="saving || loading" @click="saveEditor">{{ saving ? '保存中…' : '保存' }}</button>
      </div>
    </header>
    <div v-if="errorMessage" class="pipeline-editor__error" data-testid="editor-error">{{ errorMessage }}</div>
    <div v-if="notice" class="pipeline-editor__notice">{{ notice }}</div>

    <PipelinePalette :metadata="nodeMetadata" @drag-node="() => undefined" />
    <section class="pipeline-canvas" data-testid="pipeline-canvas" @dragover.prevent @drop.prevent="dropNode">
      <div class="pipeline-canvas__label">FLOW CANVAS / DAG</div>
      <div v-if="!loading && nodes.length === 0" class="pipeline-canvas__empty"><strong>从左侧拖入节点</strong>将数据源、处理和输出节点连接成一条可执行链路。</div>
      <VueFlow v-model:nodes="nodes" v-model:edges="edges" :node-types="nodeTypes" fit-view-on-init @node-click="onNodeClick" @connect="onConnect">
      </VueFlow>
    </section>
    <div v-if="inspectorOpen" class="pipeline-inspector-modal" data-testid="inspector-modal" role="dialog" aria-modal="true" aria-label="Pipeline 属性检查器" @click.self="closeInspector">
      <PipelineInspector :pipeline="pipeline" :selected-node="selectedNode" :node-metadata="nodeMetadata" @update:pipeline="updatePipelineProperties" @update:selected-node="updateSelectedNode" @delete-node="removeSelectedNode" @close="closeInspector" />
    </div>
  </div>
</template>
