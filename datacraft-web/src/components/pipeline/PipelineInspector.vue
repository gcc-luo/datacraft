<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import type { ExecutionStrategy, NodeMetadataResponse, PipelineNodeData, PipelineNodePatch, PipelineStatus } from '../../types/pipeline'

interface InspectorPipeline {
  name: string
  description: string | null
  status: PipelineStatus
  executionStrategy: ExecutionStrategy
}

const props = defineProps<{
  pipeline: InspectorPipeline
  selectedNode: PipelineNodeData | null
  nodeMetadata: NodeMetadataResponse[]
}>()

const emit = defineEmits<{
  'update:pipeline': [value: Partial<InspectorPipeline>]
  'update:selected-node': [value: PipelineNodePatch]
  'delete-node': []
}>()

const nodeConfig = ref('{}')
const configError = ref('')

const selectedMetadata = computed(() => props.selectedNode?.metadata || props.nodeMetadata.find((item) => item.type === props.selectedNode?.nodeType))
const engineOptions = computed(() => selectedMetadata.value?.supportedEngines || [])

watch(() => props.selectedNode, (node) => {
  nodeConfig.value = node?.configJson || '{}'
  configError.value = ''
}, { immediate: true })

function emitPipeline<K extends keyof InspectorPipeline>(key: K, value: InspectorPipeline[K]) {
  emit('update:pipeline', { [key]: value } as Partial<InspectorPipeline>)
}

function emitNode<K extends keyof PipelineNodeData>(key: K, value: PipelineNodeData[K]) {
  emit('update:selected-node', { [key]: value })
}

function emitPosition(position: { x: number; y: number }) {
  emit('update:selected-node', { position })
}

function validateAndEmitConfig() {
  try {
    JSON.parse(nodeConfig.value)
    configError.value = ''
    emitNode('configJson', nodeConfig.value)
  } catch {
    configError.value = '配置必须是合法 JSON'
  }
}
</script>

<template>
  <aside class="pipeline-inspector" aria-label="属性检查器">
    <template v-if="selectedNode">
      <div class="pipeline-inspector__kicker">NODE INSPECTOR</div>
      <h2>{{ selectedNode.nodeName }}</h2>
      <p class="pipeline-inspector__sub">{{ selectedNode.nodeType }} · {{ selectedNode.nodeKey }}</p>
      <div class="pipeline-inspector__form">
        <label>节点名称<input data-testid="node-name" :value="selectedNode.nodeName" @input="emitNode('nodeName', ($event.target as HTMLInputElement).value)" /></label>
        <label>执行引擎<select data-testid="node-engine" :value="selectedNode.preferredEngine || ''" @change="emitNode('preferredEngine', ($event.target as HTMLSelectElement).value || null)"><option value="">自动选择</option><option v-for="engine in engineOptions" :key="engine" :value="engine">{{ engine }}</option></select></label>
        <div class="pipeline-inspector__row">
          <label>X 坐标<input type="number" :value="0" @change="emitPosition({ x: Number(($event.target as HTMLInputElement).value), y: 0 })" /></label>
          <label>Y 坐标<input type="number" :value="0" @change="emitPosition({ x: 0, y: Number(($event.target as HTMLInputElement).value) })" /></label>
        </div>
        <label>节点配置<textarea data-testid="node-config" v-model="nodeConfig" rows="10" spellcheck="false" @blur="validateAndEmitConfig" /></label>
        <p v-if="configError" data-testid="config-error" class="pipeline-inspector__invalid">{{ configError }}</p>
        <p class="pipeline-inspector__hint">配置将按节点类型的 schema 在保存时由服务端再次校验。</p>
        <button class="pipeline-inspector__delete" type="button" @click="emit('delete-node')">删除节点</button>
      </div>
    </template>
    <template v-else>
      <div class="pipeline-inspector__kicker">PIPELINE SETTINGS</div>
      <h2>管道属性</h2>
      <p class="pipeline-inspector__sub">选择画布节点查看节点配置，或先完善管道基本信息。</p>
      <div class="pipeline-inspector__form">
        <label>管道名称<input data-testid="pipeline-name" :value="pipeline.name" @input="emitPipeline('name', ($event.target as HTMLInputElement).value)" /></label>
        <label>描述<textarea data-testid="pipeline-description" :value="pipeline.description || ''" rows="4" @input="emitPipeline('description', ($event.target as HTMLTextAreaElement).value || null)" /></label>
        <label>状态<select data-testid="pipeline-status" :value="pipeline.status" @change="emitPipeline('status', ($event.target as HTMLSelectElement).value as PipelineStatus)"><option value="DRAFT">草稿</option><option value="ACTIVE">启用</option><option value="ARCHIVED">归档</option></select></label>
        <label>执行策略<select data-testid="pipeline-strategy" :value="pipeline.executionStrategy" @change="emitPipeline('executionStrategy', ($event.target as HTMLSelectElement).value as ExecutionStrategy)"><option value="AUTO">自动选择</option><option value="NATIVE">Native</option><option value="DATAX">DataX</option><option value="CAMEL">Camel</option><option value="SEATUNNEL">SeaTunnel</option></select></label>
        <p class="pipeline-inspector__hint">当前阶段只编辑和保存编排模型；执行、发布与调度将在后续阶段接入。</p>
      </div>
    </template>
  </aside>
</template>
