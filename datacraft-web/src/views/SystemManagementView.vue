<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { createMenu, createRole, createUser, listMenus, listRoles, listUsers, updateMenu, updateRole, updateUser } from '../api/system'
import PaginationBar from '../components/common/PaginationBar.vue'
import { usePagination } from '../composables/usePagination'
import type { AdminMenuRequest, AdminMenuResponse, AdminRoleRequest, AdminRoleResponse, AdminUserRequest, AdminUserResponse } from '../types/system'

type Section = 'users' | 'roles' | 'menus'

const route = useRoute()
const activeSection = computed<Section>(() => {
  const section = route.meta.systemSection
  return section === 'roles' || section === 'menus' ? section : 'users'
})
const users = ref<AdminUserResponse[]>([])
const roles = ref<AdminRoleResponse[]>([])
const menus = ref<AdminMenuResponse[]>([])
const loading = ref(true)
const saving = ref(false)
const errorMessage = ref('')
const notice = ref('')
const forbidden = ref(false)
const userFormOpen = ref(false)
const roleFormOpen = ref(false)
const menuFormOpen = ref(false)
const editingUserId = ref<number | null>(null)
const editingRoleId = ref<number | null>(null)
const editingMenuId = ref<number | null>(null)

const userForm = reactive<AdminUserRequest>(emptyUser())
const roleForm = reactive<AdminRoleRequest>(emptyRole())
const menuForm = reactive<AdminMenuRequest>(emptyMenu())
const userPagination = usePagination(users)
const rolePagination = usePagination(roles)
const menuPagination = usePagination(menus)

function emptyUser(): AdminUserRequest {
  return { username: '', displayName: '', password: '', roleCodes: [], enabled: true }
}

function emptyRole(): AdminRoleRequest {
  return { code: '', name: '', menuIds: [], enabled: true }
}

function emptyMenu(): AdminMenuRequest {
  return { code: '', title: '', path: '', icon: '', parentId: null, sortOrder: 0, enabled: true }
}

function isForbidden(error: unknown) {
  if (!error || typeof error !== 'object' || !('response' in error)) return false
  const response = (error as { response?: { status?: number } }).response
  return response?.status === 403
}

function messageOf(error: unknown, fallback: string) {
  return error instanceof Error ? error.message : fallback
}

async function load() {
  loading.value = true
  errorMessage.value = ''
  forbidden.value = false
  try {
    const [userRows, roleRows, menuRows] = await Promise.all([listUsers(), listRoles(), listMenus()])
    users.value = userRows
    roles.value = roleRows
    menus.value = menuRows
  } catch (error) {
    if (isForbidden(error)) {
      forbidden.value = true
      errorMessage.value = '没有执行此操作的权限'
    } else {
      errorMessage.value = messageOf(error, '系统管理数据加载失败')
    }
  } finally {
    loading.value = false
  }
}

function closeForms() {
  userFormOpen.value = false
  roleFormOpen.value = false
  menuFormOpen.value = false
}

function startCreateUser() {
  editingUserId.value = null
  Object.assign(userForm, emptyUser())
  closeForms()
  userFormOpen.value = true
}

function startEditUser(row: AdminUserResponse) {
  editingUserId.value = row.id
  Object.assign(userForm, { username: row.username, displayName: row.displayName, password: '', roleCodes: [...row.roleCodes], enabled: row.enabled })
  closeForms()
  userFormOpen.value = true
}

function startCreateRole() {
  editingRoleId.value = null
  Object.assign(roleForm, emptyRole())
  closeForms()
  roleFormOpen.value = true
}

function startEditRole(row: AdminRoleResponse) {
  editingRoleId.value = row.id
  Object.assign(roleForm, { code: row.code, name: row.name, menuIds: [...row.menuIds], enabled: row.enabled })
  closeForms()
  roleFormOpen.value = true
}

function startCreateMenu() {
  editingMenuId.value = null
  Object.assign(menuForm, emptyMenu())
  closeForms()
  menuFormOpen.value = true
}

function startEditMenu(row: AdminMenuResponse) {
  editingMenuId.value = row.id
  Object.assign(menuForm, { code: row.code, title: row.title, path: row.path, icon: row.icon || '', parentId: row.parentId, sortOrder: row.sortOrder, enabled: row.enabled })
  closeForms()
  menuFormOpen.value = true
}

async function submitUser() {
  saving.value = true
  errorMessage.value = ''
  notice.value = ''
  try {
    const request = { ...userForm, password: userForm.password?.trim() || undefined, roleCodes: [...userForm.roleCodes] }
    if (editingUserId.value === null) await createUser(request)
    else await updateUser(editingUserId.value, request)
    notice.value = editingUserId.value === null ? '用户已创建' : '用户已更新'
    closeForms()
    await load()
  } catch (error) {
    errorMessage.value = isForbidden(error) ? '没有执行此操作的权限' : messageOf(error, '用户保存失败')
  } finally {
    saving.value = false
  }
}

async function submitRole() {
  saving.value = true
  errorMessage.value = ''
  notice.value = ''
  try {
    const request = { ...roleForm, menuIds: [...roleForm.menuIds] }
    if (editingRoleId.value === null) await createRole(request)
    else await updateRole(editingRoleId.value, request)
    notice.value = editingRoleId.value === null ? '角色已创建' : '角色权限已更新'
    closeForms()
    await load()
  } catch (error) {
    errorMessage.value = isForbidden(error) ? '没有执行此操作的权限' : messageOf(error, '角色保存失败')
  } finally {
    saving.value = false
  }
}

async function submitMenu() {
  saving.value = true
  errorMessage.value = ''
  notice.value = ''
  try {
    const request = { ...menuForm, icon: menuForm.icon?.trim() || undefined }
    if (editingMenuId.value === null) await createMenu(request)
    else await updateMenu(editingMenuId.value, request)
    notice.value = editingMenuId.value === null ? '菜单已创建' : '菜单已更新'
    closeForms()
    await load()
  } catch (error) {
    errorMessage.value = isForbidden(error) ? '没有执行此操作的权限' : messageOf(error, '菜单保存失败')
  } finally {
    saving.value = false
  }
}

function roleMenuLabel(row: AdminRoleResponse) {
  return `${row.menuIds.length} 个菜单权限`
}

onMounted(load)
</script>

<template>
  <div class="system-page">
    <div v-if="notice" class="inline-notice">{{ notice }}</div>
    <div v-if="errorMessage" class="inline-error">{{ errorMessage }}</div>

    <section v-if="loading" class="system-empty">正在加载系统管理数据…</section>
    <section v-else-if="forbidden" class="system-empty system-empty--forbidden"><strong>暂无访问权限</strong><span>只有管理员角色可以配置系统用户、角色和菜单。</span></section>
    <section v-else class="system-workspace">
      <div class="system-workspace__content">
        <template v-if="activeSection === 'users'">
          <div class="system-section-heading"><div><span class="system-section-heading__eyebrow">ACCOUNT DIRECTORY</span><h3>用户管理</h3><p>维护账号状态和角色归属。</p></div><button data-testid="create-user" class="primary-action" type="button" @click="startCreateUser">＋ 新建用户</button></div>
          <div class="system-table-card">
            <template v-if="users.length">
              <table class="system-table"><thead><tr><th>账号</th><th>角色</th><th>状态</th><th>创建时间</th><th>操作</th></tr></thead><tbody><tr v-for="row in userPagination.paginatedItems.value" :key="row.id"><td><strong>{{ row.displayName }}</strong><small>{{ row.username }}</small></td><td><span v-for="code in row.roleCodes" :key="code" class="system-tag">{{ code }}</span><em v-if="!row.roleCodes.length">未分配</em></td><td><span class="system-status" :class="{ 'is-disabled': !row.enabled }"><i></i>{{ row.enabled ? '启用' : '停用' }}</span></td><td>{{ row.createdAt ? new Date(row.createdAt).toLocaleDateString('zh-CN') : '—' }}</td><td><button class="table-action" type="button" @click="startEditUser(row)">编辑</button></td></tr></tbody></table>
            <PaginationBar :current-page="userPagination.currentPage.value" :page-size="userPagination.pageSize.value" :total="userPagination.total.value" @update:current-page="userPagination.setPage" @update:page-size="userPagination.setPageSize" />
            </template>
            <div v-else class="system-empty"><strong>还没有用户</strong><span>创建一个账号并分配角色。</span></div>
          </div>
        </template>

        <template v-else-if="activeSection === 'roles'">
          <div class="system-section-heading"><div><span class="system-section-heading__eyebrow">ROLE AUTHORIZATION</span><h3>角色管理 · 角色授权</h3><p>为角色配置可见菜单和导航入口。</p></div><button class="primary-action" type="button" @click="startCreateRole">＋ 新建角色</button></div>
          <div class="system-table-card"><template v-if="roles.length"><table class="system-table"><thead><tr><th>角色</th><th>编码</th><th>菜单权限</th><th>状态</th><th>操作</th></tr></thead><tbody><tr v-for="row in rolePagination.paginatedItems.value" :key="row.id" :data-testid="`role-row-${row.id}`" @click="startEditRole(row)"><td><strong>{{ row.name }}</strong></td><td><code>{{ row.code }}</code></td><td>{{ roleMenuLabel(row) }}</td><td><span class="system-status" :class="{ 'is-disabled': !row.enabled }"><i></i>{{ row.enabled ? '启用' : '停用' }}</span></td><td><button class="table-action" type="button" @click.stop="startEditRole(row)">配置权限</button></td></tr></tbody></table><PaginationBar :current-page="rolePagination.currentPage.value" :page-size="rolePagination.pageSize.value" :total="rolePagination.total.value" @update:current-page="rolePagination.setPage" @update:page-size="rolePagination.setPageSize" /></template><div v-else class="system-empty"><strong>还没有角色</strong><span>创建角色后即可配置菜单权限。</span></div></div>
        </template>

        <template v-else>
          <div class="system-section-heading"><div><span class="system-section-heading__eyebrow">NAVIGATION CATALOG</span><h3>菜单管理 · 菜单目录</h3><p>维护系统导航的层级、路径和展示顺序。</p></div><button class="primary-action" type="button" @click="startCreateMenu">＋ 新建菜单</button></div>
          <div class="system-table-card"><template v-if="menus.length"><table class="system-table"><thead><tr><th>菜单</th><th>编码</th><th>父级</th><th>路径</th><th>状态</th><th>操作</th></tr></thead><tbody><tr v-for="row in menuPagination.paginatedItems.value" :key="row.id"><td><strong>{{ row.title }}</strong><small>排序 {{ row.sortOrder }}</small></td><td><code>{{ row.code }}</code></td><td>{{ row.parentTitle || '根菜单' }}</td><td>{{ row.path }}</td><td><span class="system-status" :class="{ 'is-disabled': !row.enabled }"><i></i>{{ row.enabled ? '启用' : '停用' }}</span></td><td><button class="table-action" type="button" @click="startEditMenu(row)">编辑</button></td></tr></tbody></table><PaginationBar :current-page="menuPagination.currentPage.value" :page-size="menuPagination.pageSize.value" :total="menuPagination.total.value" @update:current-page="menuPagination.setPage" @update:page-size="menuPagination.setPageSize" /></template><div v-else class="system-empty"><strong>还没有菜单</strong><span>创建一个菜单作为导航入口。</span></div></div>
        </template>

        <div v-if="userFormOpen" class="system-modal" role="presentation" tabindex="-1" @click.self="closeForms" @keydown.esc="closeForms"><section class="system-form-card system-modal__panel" role="dialog" aria-modal="true" aria-labelledby="system-user-form-title"><div class="system-form-card__head"><div><span>{{ editingUserId === null ? 'NEW ACCOUNT' : 'EDIT ACCOUNT' }}</span><h4 id="system-user-form-title">{{ editingUserId === null ? '新建用户' : '编辑用户' }}</h4></div><button class="quiet-action" type="button" @click="closeForms">关闭</button></div><form data-testid="user-form" class="system-form" @submit.prevent="submitUser"><label>用户名<input v-model="userForm.username" name="username" required :disabled="editingUserId !== null" /></label><label>显示名<input v-model="userForm.displayName" name="displayName" required /></label><label>密码<input v-model="userForm.password" name="password" type="password" :required="editingUserId === null" autocomplete="new-password" /><small>{{ editingUserId === null ? '请输入初始密码。' : '留空表示保留原密码。' }}</small></label><label>角色<select v-model="userForm.roleCodes" name="roleCodes" multiple><option v-for="role in roles" :key="role.id" :value="role.code">{{ role.name }} · {{ role.code }}</option></select></label><label class="system-checkbox"><input v-model="userForm.enabled" type="checkbox" />启用账号</label><div class="system-form__actions"><button class="quiet-action" type="button" @click="closeForms">取消</button><button class="primary-action" type="submit" :disabled="saving">{{ saving ? '保存中…' : '保存用户' }}</button></div></form></section></div>

        <div v-if="roleFormOpen" class="system-modal" role="presentation" tabindex="-1" @click.self="closeForms" @keydown.esc="closeForms"><section class="system-form-card system-modal__panel" role="dialog" aria-modal="true" aria-labelledby="system-role-form-title"><div class="system-form-card__head"><div><span>{{ editingRoleId === null ? 'NEW ROLE' : 'ROLE AUTHORIZATION' }}</span><h4 id="system-role-form-title">{{ editingRoleId === null ? '新建角色' : '配置角色权限' }}</h4></div><button class="quiet-action" type="button" @click="closeForms">关闭</button></div><form data-testid="role-form" class="system-form" @submit.prevent="submitRole"><label>角色编码<input v-model="roleForm.code" name="roleCode" required :disabled="roleForm.code === 'ADMIN'" /></label><label>角色名称<input v-model="roleForm.name" name="roleName" required /></label><div class="system-menu-permissions"><span>菜单权限</span><label v-for="menu in menus" :key="menu.id" class="system-permission"><input :data-testid="`menu-permission-${menu.id}`" v-model="roleForm.menuIds" type="checkbox" :value="menu.id" />{{ menu.title }}<small>{{ menu.path }}</small></label></div><label class="system-checkbox"><input v-model="roleForm.enabled" type="checkbox" :disabled="roleForm.code === 'ADMIN'" />启用角色</label><div class="system-form__actions"><button class="quiet-action" type="button" @click="closeForms">取消</button><button class="primary-action" type="submit" :disabled="saving">{{ saving ? '保存中…' : '保存角色权限' }}</button></div></form></section></div>

        <div v-if="menuFormOpen" class="system-modal" role="presentation" tabindex="-1" @click.self="closeForms" @keydown.esc="closeForms"><section class="system-form-card system-modal__panel" role="dialog" aria-modal="true" aria-labelledby="system-menu-form-title"><div class="system-form-card__head"><div><span>{{ editingMenuId === null ? 'NEW MENU' : 'EDIT MENU' }}</span><h4 id="system-menu-form-title">{{ editingMenuId === null ? '新建菜单' : '编辑菜单' }}</h4></div><button class="quiet-action" type="button" @click="closeForms">关闭</button></div><form data-testid="menu-form" class="system-form" @submit.prevent="submitMenu"><label>菜单编码<input v-model="menuForm.code" name="menuCode" required /></label><label>菜单标题<input v-model="menuForm.title" name="menuTitle" required /></label><label>路径<input v-model="menuForm.path" name="menuPath" required placeholder="/example" /></label><label>图标<input v-model="menuForm.icon" name="menuIcon" placeholder="settings" /></label><label>父级菜单<select v-model="menuForm.parentId" name="parentId"><option :value="null">根菜单</option><option v-for="menu in menus.filter(item => item.id !== editingMenuId)" :key="menu.id" :value="menu.id">{{ menu.title }}</option></select></label><label>排序<input v-model.number="menuForm.sortOrder" name="sortOrder" type="number" min="0" /></label><label class="system-checkbox"><input v-model="menuForm.enabled" type="checkbox" />启用菜单</label><div class="system-form__actions"><button class="quiet-action" type="button" @click="closeForms">取消</button><button class="primary-action" type="submit" :disabled="saving">{{ saving ? '保存中…' : '保存菜单' }}</button></div></form></section></div>
      </div>
    </section>
  </div>
</template>
