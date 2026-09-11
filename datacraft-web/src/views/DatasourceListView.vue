<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { createDatasource, deleteDatasource, listDatasources, testDatasource, updateDatasource } from '../api/datasources'
import PaginationBar from '../components/common/PaginationBar.vue'
import { usePagination } from '../composables/usePagination'
import type { DatasourceRequest, DatasourceResponse, DatasourceTestResponse, DatasourceType } from '../types/datasource'

type DatasourceForm = DatasourceRequest

const rows = ref<DatasourceResponse[]>([])
const loading = ref(true)
const saving = ref(false)
const formOpen = ref(false)
const editingId = ref<number | null>(null)
const testingId = ref<number | null>(null)
const formTesting = ref(false)
const notice = ref('')
const rowTestResult = ref<DatasourceTestResponse | null>(null)
const formTestResult = ref<DatasourceTestResponse | null>(null)
const testedFormSignature = ref('')
const formElement = ref<HTMLFormElement | null>(null)
const form = reactive<DatasourceForm>(emptyForm())
const pagination = usePagination(rows)

function emptyForm(): DatasourceForm {
  return { name: '', type: 'POSTGRESQL', host: '', port: 5432, databaseName: '', username: '', password: '', remark: '' }
}

async function loadRows() {
  loading.value = true
  try {
    rows.value = await listDatasources()
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '数据源加载失败')
  } finally {
    loading.value = false
  }
}

function startCreate() {
  editingId.value = null
  Object.assign(form, emptyForm())
  formTestResult.value = null
  testedFormSignature.value = ''
  notice.value = ''
  formOpen.value = true
}

function startEdit(row: DatasourceResponse) {
  editingId.value = row.id
  Object.assign(form, {
    name: row.name,
    type: row.type,
    host: row.host,
    port: row.port,
    databaseName: row.databaseName,
    username: row.username,
    password: '',
    remark: row.remark || '',
  })
  formTestResult.value = null
  testedFormSignature.value = ''
  notice.value = ''
  formOpen.value = true
}

function closeForm() {
  formOpen.value = false
}

async function save() {
  if (!canSave.value) return
  saving.value = true
  notice.value = ''
  const payload: DatasourceRequest = { ...form, password: form.password?.trim() || undefined }
  try {
    if (editingId.value === null) {
      await createDatasource(payload)
      ElMessage.success('数据源已创建')
    } else {
      await updateDatasource(editingId.value, payload)
      ElMessage.success('数据源已更新')
    }
    formOpen.value = false
    await loadRows()
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '数据源保存失败')
  } finally {
    saving.value = false
  }
}

async function testConnection(row: DatasourceResponse) {
  testingId.value = row.id
  try {
    rowTestResult.value = await testDatasource(row.id)
    await loadRows()
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '连接测试失败')
  } finally {
    testingId.value = null
  }
}

function formSignature() {
  return JSON.stringify(form)
}

function formPayload(): DatasourceRequest {
  return { ...form, password: form.password?.trim() || undefined }
}

function invalidateConnectionTest() {
  if (formTestResult.value) {
    formTestResult.value = null
    testedFormSignature.value = ''
  }
}

async function testFormConnection() {
  if (!formElement.value?.reportValidity()) return
  formTesting.value = true
  formTestResult.value = null
  try {
    formTestResult.value = await testDatasource(formPayload(), editingId.value ?? undefined)
    if (formTestResult.value.success) {
      testedFormSignature.value = formSignature()
      ElMessage.success('连接测试成功')
    } else {
      ElMessage.error(formTestResult.value.message || '连接测试失败')
    }
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '连接测试失败')
    testedFormSignature.value = ''
  } finally {
    formTesting.value = false
  }
}

const canSave = computed(() => formTestResult.value?.success === true && testedFormSignature.value === formSignature())

async function remove(row: DatasourceResponse) {
  if (!window.confirm(`确定删除数据源"${row.name}"吗？`)) return
  try {
    await deleteDatasource(row.id)
    ElMessage.success('数据源已删除')
    await loadRows()
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '数据源删除失败')
  }
}

function typeLabel(type: DatasourceType) {
  return type === 'POSTGRESQL' ? 'PostgreSQL' : 'MySQL'
}

function statusLabel(status: DatasourceResponse['status']) {
  return status === 'SUCCESS' ? '连接正常' : status === 'FAILED' ? '连接失败' : '未测试'
}

function formatTime(value: string | null) {
  return value ? new Date(value).toLocaleString('zh-CN', { hour12: false }) : '—'
}

onMounted(loadRows)
</script>

<template>
  <div class="datasource-page">
    <div class="page-heading">
      <div><p class="page-heading__eyebrow">DATA SOURCES</p><h2>数据源</h2><p>管理平台可访问的数据库连接，凭据在服务端加密保存。</p></div>
      <button class="primary-action" data-testid="create-datasource" type="button" @click="startCreate">＋ 新建数据源</button>
    </div>

    <div v-if="notice" class="inline-notice">{{ notice }}</div>

    <section class="datasource-card">
      <div v-if="loading" class="datasource-empty">正在加载数据源…</div>
      <div v-else-if="rows.length === 0" class="datasource-empty"><strong>还没有数据源</strong><span>创建一个 PostgreSQL 或 MySQL 数据源开始使用。</span></div>
      <table v-else class="datasource-table">
        <thead><tr><th>名称</th><th>类型</th><th>地址</th><th>数据库</th><th>状态</th><th>最后测试</th><th>操作</th></tr></thead>
        <tbody>
          <tr v-for="row in pagination.paginatedItems.value" :key="row.id">
            <td><strong>{{ row.name }}</strong><small>{{ row.username }}</small></td>
            <td><span class="type-badge">{{ typeLabel(row.type) }}</span></td>
            <td>{{ row.host }}:{{ row.port }}</td>
            <td>{{ row.databaseName }}</td>
            <td><span class="datasource-status" :class="`datasource-status--${row.status.toLowerCase()}`"><i></i>{{ statusLabel(row.status) }}</span></td>
            <td>{{ formatTime(row.lastTestedAt) }}<small v-if="row.lastTestLatencyMs !== null">{{ row.lastTestLatencyMs }} ms</small></td>
            <td class="datasource-actions"><button :data-testid="`test-datasource`" type="button" :disabled="testingId === row.id" @click="testConnection(row)">{{ testingId === row.id ? '测试中…' : '测试' }}</button><button data-testid="edit-datasource" type="button" @click="startEdit(row)">编辑</button><button class="danger-action" data-testid="delete-datasource" type="button" @click="remove(row)">删除</button></td>
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

    <section v-if="rowTestResult" class="test-result" :class="{ 'test-result--failed': !rowTestResult.success }"><strong>{{ rowTestResult.message }}</strong><span v-if="rowTestResult.success">耗时 {{ rowTestResult.latencyMs }} ms</span></section>

    <div v-if="formOpen" class="datasource-modal" role="presentation" @click.self="closeForm" @keydown.esc="closeForm">
      <section class="datasource-modal__panel" role="dialog" aria-modal="true" aria-labelledby="datasource-modal-title" tabindex="-1">
        <div class="section-heading"><div><p class="datasource-modal__eyebrow">DATA SOURCE CONFIGURATION</p><h3 id="datasource-modal-title">{{ editingId === null ? '新建数据源' : '编辑数据源' }}</h3><p>填写连接信息，测试成功后才能保存。</p></div><button type="button" class="quiet-action" aria-label="关闭弹框" @click="closeForm">关闭</button></div>

        <form ref="formElement" class="datasource-form" @input="invalidateConnectionTest" @change="invalidateConnectionTest" @submit.prevent="save">
        <label>名称<input v-model="form.name" name="name" required maxlength="100" placeholder="例如：客户库" /></label>
        <label>类型<select v-model="form.type" name="type"><option value="POSTGRESQL">PostgreSQL</option><option value="MYSQL">MySQL</option></select></label>
        <label>主机<input v-model="form.host" name="host" required placeholder="localhost" /></label>
        <label>端口<input v-model.number="form.port" name="port" type="number" min="1" max="65535" required /></label>
        <label>数据库<input v-model="form.databaseName" name="databaseName" required /></label>
        <label>用户名<input v-model="form.username" name="username" required /></label>
        <label class="datasource-form__wide">密码<input v-model="form.password" name="password" type="password" :required="editingId === null" autocomplete="new-password" /><small>{{ editingId === null ? '密码仅用于连接测试，并以密文保存。' : '留空则使用已保存密码测试并保留原密码。' }}</small></label>
        <label class="datasource-form__wide">备注<textarea v-model="form.remark" name="remark" rows="2" maxlength="500"></textarea></label>
          <div v-if="formTestResult" class="form-test-result" :class="{ 'form-test-result--failed': !formTestResult.success }"><strong>{{ formTestResult.message }}</strong><span v-if="formTestResult.success">耗时 {{ formTestResult.latencyMs }} ms</span></div>
          <div class="datasource-form__actions"><span v-if="!canSave" class="datasource-form__hint">请先测试连接，成功后才能保存</span><button class="quiet-action" type="button" @click="closeForm">取消</button><button class="secondary-action" data-testid="test-form-datasource" type="button" :disabled="formTesting || saving" @click="testFormConnection">{{ formTesting ? '测试中…' : '测试连接' }}</button><button class="primary-action" data-testid="save-datasource" type="submit" :disabled="saving || formTesting || !canSave">{{ saving ? '保存中…' : '保存数据源' }}</button></div>
        </form>
      </section>
    </div>
  </div>
</template>
