<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { createDatasource, deleteDatasource, listDatasources, testDatasource, updateDatasource } from '../api/datasources'
import type { DatasourceRequest, DatasourceResponse, DatasourceTestResponse, DatasourceType } from '../types/datasource'

type DatasourceForm = DatasourceRequest

const rows = ref<DatasourceResponse[]>([])
const loading = ref(true)
const saving = ref(false)
const formOpen = ref(false)
const editingId = ref<number | null>(null)
const testingId = ref<number | null>(null)
const errorMessage = ref('')
const notice = ref('')
const testResult = ref<DatasourceTestResponse | null>(null)
const form = reactive<DatasourceForm>(emptyForm())

function emptyForm(): DatasourceForm {
  return { name: '', type: 'POSTGRESQL', host: '', port: 5432, databaseName: '', username: '', password: '', remark: '' }
}

async function loadRows() {
  loading.value = true
  errorMessage.value = ''
  try {
    rows.value = await listDatasources()
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '数据源加载失败'
  } finally {
    loading.value = false
  }
}

function startCreate() {
  editingId.value = null
  Object.assign(form, emptyForm())
  testResult.value = null
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
  testResult.value = null
  notice.value = ''
  formOpen.value = true
}

function closeForm() {
  formOpen.value = false
}

async function save() {
  saving.value = true
  errorMessage.value = ''
  notice.value = ''
  const payload: DatasourceRequest = { ...form, password: form.password?.trim() || undefined }
  try {
    if (editingId.value === null) {
      await createDatasource(payload)
      notice.value = '数据源已创建'
    } else {
      await updateDatasource(editingId.value, payload)
      notice.value = '数据源已更新'
    }
    formOpen.value = false
    await loadRows()
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '数据源保存失败'
  } finally {
    saving.value = false
  }
}

async function testConnection(row: DatasourceResponse) {
  testingId.value = row.id
  errorMessage.value = ''
  try {
    testResult.value = await testDatasource(row.id)
    await loadRows()
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '连接测试失败'
  } finally {
    testingId.value = null
  }
}

async function remove(row: DatasourceResponse) {
  if (!window.confirm(`确定删除数据源“${row.name}”吗？`)) return
  errorMessage.value = ''
  try {
    await deleteDatasource(row.id)
    notice.value = '数据源已删除'
    await loadRows()
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '数据源删除失败'
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
    <div v-if="errorMessage" class="inline-error">{{ errorMessage }}</div>

    <section class="datasource-card">
      <div v-if="loading" class="datasource-empty">正在加载数据源…</div>
      <div v-else-if="rows.length === 0" class="datasource-empty"><strong>还没有数据源</strong><span>创建一个 PostgreSQL 或 MySQL 数据源开始使用。</span></div>
      <table v-else class="datasource-table">
        <thead><tr><th>名称</th><th>类型</th><th>地址</th><th>数据库</th><th>状态</th><th>最后测试</th><th>操作</th></tr></thead>
        <tbody>
          <tr v-for="row in rows" :key="row.id">
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
    </section>

    <section v-if="testResult" class="test-result" :class="{ 'test-result--failed': !testResult.success }"><strong>{{ testResult.message }}</strong><span v-if="testResult.success">耗时 {{ testResult.latencyMs }} ms</span></section>

    <section v-if="formOpen" class="datasource-form-card">
      <div class="section-heading"><div><h3>{{ editingId === null ? '新建数据源' : '编辑数据源' }}</h3><p>只支持 PostgreSQL 和 MySQL JDBC 连接。</p></div><button type="button" class="quiet-action" @click="closeForm">关闭</button></div>
      <form class="datasource-form" @submit.prevent="save">
        <label>名称<input v-model="form.name" name="name" required maxlength="100" placeholder="例如：客户库" /></label>
        <label>类型<select v-model="form.type" name="type"><option value="POSTGRESQL">PostgreSQL</option><option value="MYSQL">MySQL</option></select></label>
        <label>主机<input v-model="form.host" name="host" required placeholder="localhost" /></label>
        <label>端口<input v-model.number="form.port" name="port" type="number" min="1" max="65535" required /></label>
        <label>数据库<input v-model="form.databaseName" name="databaseName" required /></label>
        <label>用户名<input v-model="form.username" name="username" required /></label>
        <label class="datasource-form__wide">密码<input v-model="form.password" name="password" type="password" :required="editingId === null" autocomplete="new-password" /><small>{{ editingId === null ? '密码仅用于连接测试，并以密文保存。' : '留空则保留原密码。' }}</small></label>
        <label class="datasource-form__wide">备注<textarea v-model="form.remark" name="remark" rows="2" maxlength="500"></textarea></label>
        <div class="datasource-form__actions"><button class="quiet-action" type="button" @click="closeForm">取消</button><button class="primary-action" type="submit" :disabled="saving">{{ saving ? '保存中…' : '保存数据源' }}</button></div>
      </form>
    </section>
  </div>
</template>
