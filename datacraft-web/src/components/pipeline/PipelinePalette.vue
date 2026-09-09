<script setup lang="ts">
import { computed, ref } from 'vue'
import type { NodeCategory, NodeMetadataResponse } from '../../types/pipeline'

const props = defineProps<{ metadata: NodeMetadataResponse[] }>()
const emit = defineEmits<{ 'drag-node': [type: string] }>()
const search = ref('')

const categoryLabels: Record<NodeCategory, string> = {
  SOURCE: '数据源', TRANSFORM: '处理', QUALITY: '质量', GOVERNANCE: '治理', SINK: '输出', UTILITY: '工具',
}

const groups = computed(() => {
  const query = search.value.trim().toLowerCase()
  const grouped = new Map<NodeCategory, NodeMetadataResponse[]>()
  for (const item of props.metadata) {
    if (query && ![item.type, item.name, item.category].some((value) => value.toLowerCase().includes(query))) continue
    const items = grouped.get(item.category) || []
    items.push(item)
    grouped.set(item.category, items)
  }
  return [...grouped.entries()].map(([category, items]) => ({ category, label: categoryLabels[category], items }))
})

function startDrag(event: DragEvent, item: NodeMetadataResponse) {
  event.dataTransfer?.setData('application/datacraft-node', item.type)
  if (event.dataTransfer) event.dataTransfer.effectAllowed = 'copy'
  emit('drag-node', item.type)
}
</script>

<template>
  <aside class="pipeline-palette" aria-label="节点库">
    <div class="pipeline-panel-kicker">节点库 <span>{{ metadata.length }}</span></div>
    <div class="pipeline-palette__heading"><h2>组件库</h2><p>拖入画布开始编排</p></div>
    <label class="pipeline-search"><span>⌕</span><input v-model="search" data-testid="palette-search" type="search" placeholder="搜索节点" /></label>
    <div class="pipeline-palette__groups" data-testid="palette-scroll-region" aria-label="组件节点列表">
      <section v-for="group in groups" :key="group.category" class="pipeline-palette__group">
        <div class="pipeline-palette__group-title"><span>{{ group.label }}</span><small>{{ group.items.length }}</small></div>
        <button v-for="item in group.items" :key="item.type" :data-testid="`palette-${item.type}`" class="pipeline-palette__item" type="button" draggable="true" @dragstart="startDrag($event, item)">
          <span class="pipeline-palette__item-icon" :class="`is-${item.category.toLowerCase()}`">{{ item.icon.slice(0, 1).toUpperCase() }}</span>
          <span><strong>{{ item.name }}</strong><small>{{ item.type }} · {{ item.defaultEngine }}</small></span>
          <i>＋</i>
        </button>
      </section>
      <div v-if="groups.length === 0" class="pipeline-palette__empty">没有匹配的节点</div>
    </div>
    <div class="pipeline-palette__hint"><span>提示</span><p>连线会在保存时由服务端校验 DAG 是否有环。</p></div>
  </aside>
</template>
