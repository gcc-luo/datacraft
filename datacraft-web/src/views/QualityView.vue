<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { listDatasources } from '../api/datasources'
import { listQualityResults, runQualityCheck } from '../api/quality'
import PaginationBar from '../components/common/PaginationBar.vue'
import { usePagination } from '../composables/usePagination'
import type { DatasourceResponse } from '../types/datasource'
import type { QualityResultResponse } from '../types/quality'

const datasources = ref<DatasourceResponse[]>([])
const results = ref<QualityResultResponse[]>([])
const selectedDatasourceId = ref<number | null>(null)
const tableName = ref('')
const fieldName = ref('')
const loading = ref(true)
const running = ref(false)
const errorMessage = ref('')
const notice = ref('')
const pagination = usePagination(results)

async function load() {
  loading.value = true
  errorMessage.value = ''
  try {
    const [sourceRows, resultRows] = await Promise.all([listDatasources(), listQualityResults()])
    datasources.value = sourceRows
    selectedDatasourceId.value = selectedDatasourceId.value || sourceRows[0]?.id || null
    results.value = resultRows
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '质量结果加载失败'
  } finally {
    loading.value = false
  }
}

async function submit() {
  if (!selectedDatasourceId.value || !tableName.value.trim() || !fieldName.value.trim()) {
    errorMessage.value = '请选择数据源并填写表名、字段名'
    return
  }
  running.value = true
  errorMessage.value = ''
  notice.value = ''
  try {
    const created = await runQualityCheck({ datasourceId: selectedDatasourceId.value, tableName: tableName.value.trim(), rules: [{ type: 'NULL_CHECK', field: fieldName.value.trim() }] })
    results.value = [...created, ...results.value]
    pagination.resetPage()
    notice.value = '空值检查已完成'
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '质量检查失败'
  } finally {
    running.value = false
  }
}

function metricRate(value: number) { return `${(value * 100).toFixed(2)}%` }
function sampleValue(value: unknown) { return value === null ? 'null' : value === undefined ? '—' : String(value) }
function ruleLabel(type: string) { return type === 'NULL_CHECK' ? '空值检查' : type }
function formatTime(value: string) { return new Date(value).toLocaleString('zh-CN', { hour12: false }) }

onMounted(load)
</script>

<template>
  <div class="quality-page">
    <div class="page-heading">
      <div><p class="page-heading__eyebrow">QUALITY CONTROL</p><h2>数据质量</h2><p>用可追溯的规则检查数据，并快速定位异常样本。</p></div>
    </div>

    <div v-if="notice" class="inline-notice">{{ notice }}</div>
    <div v-if="errorMessage" class="inline-error">{{ errorMessage }}</div>

    <section class="quality-run-card">
      <div class="quality-run-card__heading"><div><span>QUICK CHECK</span><h3>运行空值检查</h3></div><small>当前结果最多保留 1000 条异常样例</small></div>
      <div class="quality-form">
        <label>数据源<select v-model="selectedDatasourceId" data-testid="quality-datasource"><option :value="null">请选择数据源</option><option v-for="source in datasources" :key="source.id" :value="source.id">{{ source.name }} · {{ source.type === 'POSTGRESQL' ? 'PostgreSQL' : 'MySQL' }}</option></select></label>
        <label>表名<input v-model="tableName" data-testid="quality-table" placeholder="例如 customer" /></label>
        <label>字段名<input v-model="fieldName" data-testid="quality-field" placeholder="例如 phone" /></label>
        <button data-testid="run-quality-check" class="primary-action" type="button" :disabled="running || !datasources.length" @click="submit">{{ running ? '检查中…' : '运行检查' }}</button>
      </div>
    </section>

    <section class="quality-results-card">
      <div class="section-heading"><div><h3>质量结果</h3><p>每条结果都保留统计口径与异常行样例，便于复核。</p></div><span class="quality-results-card__count">{{ results.length }} 条结果</span></div>
      <div v-if="loading" class="quality-empty">正在加载质量结果…</div>
      <div v-else-if="!results.length" class="quality-empty"><strong>还没有质量结果</strong><span>选择一个数据源，运行第一条空值检查。</span></div>
      <div v-else class="quality-result-list">
        <article v-for="result in pagination.paginatedItems.value" :key="result.id" class="quality-result">
          <div class="quality-result__top"><div><span class="quality-result__rule">{{ ruleLabel(result.ruleType) }}</span><h4>{{ result.tableName }}<b v-if="result.fieldName">.{{ result.fieldName }}</b></h4></div><span class="quality-result__status" :class="result.status === 'PASS' ? 'is-pass' : 'is-failed'">{{ result.status === 'PASS' ? '通过' : '有异常' }}</span></div>
          <div class="quality-metrics"><div><span>总行数</span><strong>{{ result.totalRows }}</strong></div><div><span>异常行数</span><strong class="is-error">{{ result.errorRows }}</strong></div><div><span>通过行数</span><strong>{{ result.passRows }}</strong></div><div><span>通过率</span><strong>{{ metricRate(result.passRate) }}</strong></div></div>
          <div class="quality-result__samples"><div class="quality-result__samples-head"><span>异常样例</span><small>{{ formatTime(result.createdAt) }} · {{ result.samples.length }} 条</small></div><div v-if="!result.samples.length" class="quality-no-samples">未发现异常样例</div><div v-else class="quality-sample-row"><code v-for="sample in result.samples.slice(0, 4)" :key="sample.id">{{ Object.entries(sample.data).map(([key, value]) => `${key}: ${sampleValue(value)}`).join(' · ') }}</code></div></div>
        </article>
      </div>
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
