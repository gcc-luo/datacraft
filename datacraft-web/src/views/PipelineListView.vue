<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { deletePipeline, listPipelines } from '../api/pipelines'
import PaginationBar from '../components/common/PaginationBar.vue'
import { usePagination } from '../composables/usePagination'
import type { PipelineResponse, PipelineStatus } from '../types/pipeline'

const router = useRouter()
const rows = ref<PipelineResponse[]>([])
const loading = ref(true)
const notice = ref('')
const pagination = usePagination(rows)

async function loadRows() {
  loading.value = true
  try {
    rows.value = await listPipelines()
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : 'Pipeline 加载失败')
  } finally {
    loading.value = false
  }
}

function startCreate() {
  void router.push({ name: 'pipeline-editor', params: { id: 'new' } })
}

async function remove(row: PipelineResponse) {
  if (!window.confirm(`确定删除 Pipeline"${row.name}"吗？`)) return
  notice.value = ''
  try {
    await deletePipeline(row.id)
    rows.value = rows.value.filter((item) => item.id !== row.id)
    ElMessage.success('Pipeline 已删除')
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : 'Pipeline 删除失败')
  }
}

function statusLabel(status: PipelineStatus) {
  return status === 'ACTIVE' ? '已启用' : status === 'ARCHIVED' ? '已归档' : '草稿'
}

function formatTime(value: string) {
  return new Date(value).toLocaleString('zh-CN', { hour12: false })
}

onMounted(loadRows)
</script>

<template>
  <div class="pipeline-list-page">
    <div class="page-heading">
      <div><p class="page-heading__eyebrow">PIPELINE STUDIO</p><h2>Pipeline 编排</h2><p>把数据资产组织成清晰、可校验的处理图。</p></div>
      <button data-testid="create-pipeline" class="primary-action" type="button" @click="startCreate">＋ 新建 Pipeline</button>
    </div>

    <div v-if="notice" class="inline-notice">{{ notice }}</div>

    <section class="pipeline-list-card">
      <div v-if="loading" class="pipeline-empty">正在加载 Pipeline…</div>
      <div v-else-if="rows.length === 0" class="pipeline-empty"><strong>还没有 Pipeline</strong><span>创建一个草稿，开始设计你的第一条数据流。</span><button class="quiet-action" type="button" @click="startCreate">创建空白草稿</button></div>
      <table v-else class="pipeline-table">
        <thead><tr><th>名称</th><th>状态</th><th>执行策略</th><th>版本</th><th>最后更新</th><th>操作</th></tr></thead>
        <tbody>
          <tr v-for="row in pagination.paginatedItems.value" :key="row.id">
            <td><RouterLink class="pipeline-table__name" :to="{ name: 'pipeline-editor', params: { id: row.id } }">{{ row.name }}</RouterLink><small>{{ row.description || '未填写描述' }}</small></td>
            <td><span class="pipeline-status" :class="`pipeline-status--${row.status.toLowerCase()}`"><i></i>{{ statusLabel(row.status) }}</span></td>
            <td><span class="pipeline-strategy">{{ row.executionStrategy }}</span></td>
            <td><span class="pipeline-version">v{{ row.version }}</span></td>
            <td>{{ formatTime(row.updatedAt) }}</td>
            <td class="pipeline-actions"><RouterLink :to="{ name: 'pipeline-editor', params: { id: row.id } }">打开编辑器</RouterLink><button data-testid="delete-pipeline" class="danger-action" type="button" @click="remove(row)">删除</button></td>
          </tr>
        </tbody>
      </table>
      <PaginationBar
        :current-page="pagination.currentPage.value"
        :page-size="pagination.pageSize.value"
        :total="pagination.total.value"
        @update:current-page="pagination.setPage"
        @update:page-size="pagination.setPageSize"
      />
    </section>
  </div>
</template>
