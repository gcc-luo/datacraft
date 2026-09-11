<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { listDatasources } from '../api/datasources'
import { getDataset, listDatasets, syncDatasourceMetadata } from '../api/metadata'
import PaginationBar from '../components/common/PaginationBar.vue'
import { usePagination } from '../composables/usePagination'
import type { DatasourceResponse } from '../types/datasource'
import type { DatasetDetailResponse, DatasetResponse } from '../types/metadata'

const datasources = ref<DatasourceResponse[]>([])
const selectedDatasourceId = ref<number | null>(null)
const datasets = ref<DatasetResponse[]>([])
const selectedDatasetId = ref<number | null>(null)
const detail = ref<DatasetDetailResponse | null>(null)
const keyword = ref('')
const loading = ref(true)
const detailLoading = ref(false)
const syncing = ref(false)
const notice = ref('')

const selectedDatasource = computed(() => datasources.value.find((item) => item.id === selectedDatasourceId.value) || null)
const selectedDataset = computed(() => datasets.value.find((item) => item.id === selectedDatasetId.value) || null)

const filteredDatasets = computed(() => {
  const query = keyword.value.trim().toLowerCase()
  if (!query) return datasets.value
  return datasets.value.filter((dataset) => {
    const datasetMatches = [dataset.schemaName, dataset.tableName, dataset.tableRemark || ''].some((value) => value.toLowerCase().includes(query))
    const fieldMatches = dataset.id === detail.value?.id && detail.value.fields.some((field) =>
      [field.fieldName, field.dataType, field.fieldRemark || ''].some((value) => value.toLowerCase().includes(query)))
    return datasetMatches || fieldMatches
  })
})

const datasetPagination = usePagination(filteredDatasets)

const groupedDatasets = computed(() => {
  const groups = new Map<string, DatasetResponse[]>()
  for (const dataset of datasetPagination.paginatedItems.value) {
    const items = groups.get(dataset.schemaName) || []
    items.push(dataset)
    groups.set(dataset.schemaName, items)
  }
  return [...groups.entries()].map(([schemaName, items]) => ({ schemaName, items }))
})

const visibleFields = computed(() => {
  const fields = detail.value?.fields || []
  const query = keyword.value.trim().toLowerCase()
  if (!query) return fields
  return fields.filter((field) => [field.fieldName, field.dataType, field.fieldRemark || ''].some((value) => value.toLowerCase().includes(query)))
})

const fieldPagination = usePagination(visibleFields)

async function loadDatasets(selectFirst = true) {
  if (selectedDatasourceId.value === null) {
    datasets.value = []
    detail.value = null
    datasetPagination.resetPage()
    fieldPagination.resetPage()
    return
  }
  datasets.value = await listDatasets(selectedDatasourceId.value)
  datasetPagination.resetPage()
  const nextId = selectFirst ? datasets.value[0]?.id || null : selectedDatasetId.value
  if (nextId && datasets.value.some((dataset) => dataset.id === nextId)) {
    await selectDataset(nextId)
  } else {
    selectedDatasetId.value = null
    detail.value = null
  }
}

async function loadPage() {
  loading.value = true
  try {
    datasources.value = await listDatasources()
    selectedDatasourceId.value = datasources.value[0]?.id || null
    await loadDatasets()
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '数据资产加载失败')
  } finally {
    loading.value = false
  }
}

async function changeDatasource() {
  loading.value = true
  selectedDatasetId.value = null
  detail.value = null
  datasetPagination.resetPage()
  fieldPagination.resetPage()
  try {
    await loadDatasets()
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '数据表加载失败')
  } finally {
    loading.value = false
  }
}

async function selectDataset(id: number) {
  selectedDatasetId.value = id
  fieldPagination.resetPage()
  detailLoading.value = true
  try {
    detail.value = await getDataset(id)
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '数据表详情加载失败')
  } finally {
    detailLoading.value = false
  }
}

async function syncMetadata() {
  if (selectedDatasourceId.value === null) return
  syncing.value = true
  notice.value = ''
  try {
    const result = await syncDatasourceMetadata(selectedDatasourceId.value)
    await loadDatasets(true)
    notice.value = `已同步 ${result.datasetCount} 张表、${result.fieldCount} 个字段`
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '元数据同步失败')
  } finally {
    syncing.value = false
  }
}

function formatCount(value: number | null) {
  return value === null ? '—' : new Intl.NumberFormat('zh-CN').format(value)
}

function formatTime(value: string) {
  return new Date(value).toLocaleString('zh-CN', { hour12: false })
}

onMounted(loadPage)
watch(keyword, () => {
  datasetPagination.resetPage()
  fieldPagination.resetPage()
})
</script>

<template>
  <div class="asset-page">
    <div class="page-heading">
      <div><p class="page-heading__eyebrow">DATA ASSETS</p><h2>资产目录</h2><p>浏览已同步的数据表、字段与基础结构信息。</p></div>
      <button data-testid="sync-metadata" class="primary-action" type="button" :disabled="syncing || selectedDatasourceId === null" @click="syncMetadata">{{ syncing ? '同步中…' : '↻ 同步元数据' }}</button>
    </div>

    <div v-if="notice" class="inline-notice">{{ notice }}</div>

    <section class="asset-toolbar">
      <label>数据源<select v-model="selectedDatasourceId" data-testid="datasource-select" @change="changeDatasource"><option v-for="source in datasources" :key="source.id" :value="source.id">{{ source.name }} · {{ source.type === 'POSTGRESQL' ? 'PostgreSQL' : 'MySQL' }}</option></select></label>
      <label class="asset-search">搜索表或字段<input v-model="keyword" data-testid="asset-search" type="search" placeholder="输入名称、类型或备注" /></label>
      <div class="asset-toolbar__stats"><strong>{{ filteredDatasets.length }}</strong><span>张数据表</span></div>
    </section>

    <section v-if="loading" class="asset-empty">正在加载资产目录…</section>
    <section v-else-if="datasources.length === 0" class="asset-empty"><strong>还没有可用数据源</strong><span>请先在数据源页面创建连接。</span></section>
    <section v-else class="asset-browser">
      <aside class="asset-browser__sidebar">
        <div class="asset-browser__sidebar-head"><span>表目录</span><small>{{ selectedDatasource?.databaseName }}</small></div>
        <div v-if="groupedDatasets.length === 0" class="asset-browser__no-results">没有匹配的表</div>
        <div v-for="group in groupedDatasets" :key="group.schemaName" class="asset-schema">
          <h3><span>◇</span>{{ group.schemaName }}<small>{{ group.items.length }}</small></h3>
          <button v-for="dataset in group.items" :key="dataset.id" :data-testid="`dataset-${dataset.id}`" class="asset-table-item" :class="{ 'is-active': selectedDatasetId === dataset.id }" type="button" @click="selectDataset(dataset.id)">
            <span class="asset-table-item__icon">▦</span><span><strong>{{ dataset.tableName }}</strong><small>{{ dataset.tableRemark || '未填写备注' }}</small></span>
          </button>
        </div>
        <PaginationBar
          :current-page="datasetPagination.currentPage.value"
          :page-size="datasetPagination.pageSize.value"
          :total="datasetPagination.total.value"
          @update:current-page="datasetPagination.setPage"
          @update:page-size="datasetPagination.setPageSize"
        />
      </aside>

      <div class="asset-browser__content">
        <div v-if="!selectedDataset" class="asset-empty asset-empty--content"><strong>选择一张数据表</strong><span>从左侧目录查看字段详情。</span></div>
        <template v-else>
          <nav class="asset-breadcrumb" aria-label="当前位置"><span>资产目录</span><i>/</i><span>{{ selectedDataset.schemaName }}</span><i>/</i><strong>{{ selectedDataset.tableName }}</strong></nav>
          <div class="asset-detail-heading"><div><p class="page-heading__eyebrow">TABLE</p><h3>{{ selectedDataset.tableName }}</h3><p>{{ selectedDataset.tableRemark || '暂无表备注' }}</p></div><span class="asset-detail-heading__source">{{ selectedDatasource?.name }}</span></div>
          <div class="asset-metrics"><div><span>字段数</span><strong>{{ detail?.fields.length || 0 }}</strong></div><div><span>预估行数</span><strong>{{ formatCount(selectedDataset.estimatedRowCount) }}</strong></div><div><span>采集时间</span><strong>{{ formatTime(selectedDataset.collectedAt) }}</strong></div></div>
          <div v-if="detailLoading" class="asset-empty asset-empty--fields">正在加载字段…</div>
          <div v-else class="asset-fields"><div class="section-heading"><div><h4>字段列表</h4><p>{{ visibleFields.length }} 个字段 · 只展示结构元数据</p></div></div><table><thead><tr><th>#</th><th>字段名</th><th>数据类型</th><th>可空</th><th>主键</th><th>备注</th></tr></thead><tbody><tr v-for="field in fieldPagination.paginatedItems.value" :key="field.id"><td>{{ field.ordinalPosition }}</td><td><strong>{{ field.fieldName }}</strong></td><td><code>{{ field.dataType }}</code></td><td><span :class="field.nullable ? 'field-muted' : 'field-required'">{{ field.nullable ? '是' : '否' }}</span></td><td><span v-if="field.primaryKey" class="field-key">PK</span><span v-else class="field-muted">—</span></td><td>{{ field.fieldRemark || '—' }}</td></tr></tbody></table><PaginationBar :current-page="fieldPagination.currentPage.value" :page-size="fieldPagination.pageSize.value" :total="fieldPagination.total.value" @update:current-page="fieldPagination.setPage" @update:page-size="fieldPagination.setPageSize" /></div>
        </template>
      </div>
    </section>
  </div>
</template>
