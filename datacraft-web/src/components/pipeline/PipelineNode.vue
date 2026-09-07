<script setup lang="ts">
import { Handle, Position } from '@vue-flow/core'
import type { NodeCategory, PipelineNodeData } from '../../types/pipeline'

const props = defineProps<{ data: PipelineNodeData }>()

const categoryLabels: Record<NodeCategory, string> = {
  SOURCE: 'SOURCE', TRANSFORM: 'TRANSFORM', QUALITY: 'QUALITY', GOVERNANCE: 'GOVERNANCE', SINK: 'SINK', UTILITY: 'UTILITY',
}

function categoryClass() {
  return `is-${(props.data.metadata?.category || 'UTILITY').toLowerCase()}`
}
</script>

<template>
  <div class="pipeline-node" :class="categoryClass()" :data-testid="`canvas-node-${data.nodeKey}`">
    <Handle id="in" type="target" :position="Position.Left" />
    <div class="pipeline-node__rail"></div>
    <div class="pipeline-node__body">
      <div class="pipeline-node__topline"><span class="pipeline-node__icon">{{ data.metadata?.icon?.slice(0, 1).toUpperCase() || 'N' }}</span><span>{{ categoryLabels[data.metadata?.category || 'UTILITY'] }}</span></div>
      <strong>{{ data.nodeName }}</strong>
      <small>{{ data.nodeKey }} <b>·</b> {{ data.preferredEngine || 'AUTO' }}</small>
    </div>
    <Handle id="out" type="source" :position="Position.Right" />
  </div>
</template>
