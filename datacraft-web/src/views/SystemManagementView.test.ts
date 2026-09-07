// @vitest-environment jsdom
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createMemoryHistory, createRouter } from 'vue-router'
import SystemManagementView from './SystemManagementView.vue'
import * as systemApi from '../api/system'

vi.mock('../api/system', () => ({
  listUsers: vi.fn(), createUser: vi.fn(), updateUser: vi.fn(),
  listRoles: vi.fn(), createRole: vi.fn(), updateRole: vi.fn(),
  listMenus: vi.fn(), createMenu: vi.fn(), updateMenu: vi.fn(),
}))

const api = vi.mocked(systemApi)

const user = { id: 1, username: 'admin', displayName: '管理员', enabled: true, roleCodes: ['ADMIN'], createdAt: null, updatedAt: null }
const role = { id: 1, code: 'ADMIN', name: '管理员', enabled: true, menuIds: [1], createdAt: null, updatedAt: null }
const menu = { id: 1, code: 'home', title: '工作台', path: '/', icon: 'home', parentId: null, parentTitle: null, sortOrder: 10, enabled: true, createdAt: null, updatedAt: null }

async function mountView(path = '/system/users') {
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/system/users', component: SystemManagementView, meta: { systemSection: 'users' } },
      { path: '/system/roles', component: SystemManagementView, meta: { systemSection: 'roles' } },
      { path: '/system/menus', component: SystemManagementView, meta: { systemSection: 'menus' } },
    ],
  })
  await router.push(path)
  await router.isReady()
  return mount(SystemManagementView, { global: { plugins: [router] } })
}

describe('SystemManagementView', () => {
  beforeEach(() => {
    vi.resetAllMocks()
    api.listUsers.mockResolvedValue([user])
    api.createUser.mockResolvedValue(user)
    api.updateUser.mockResolvedValue(user)
    api.listRoles.mockResolvedValue([role])
    api.createRole.mockResolvedValue(role)
    api.updateRole.mockResolvedValue(role)
    api.listMenus.mockResolvedValue([menu])
    api.createMenu.mockResolvedValue(menu)
    api.updateMenu.mockResolvedValue(menu)
  })

  it('uses the current second-level route and does not duplicate global navigation', async () => {
    const wrapper = await mountView('/system/roles')
    await vi.waitFor(() => expect(wrapper.text()).toContain('管理员'))

    expect(wrapper.text()).toContain('角色授权')
    expect(wrapper.text()).not.toContain('SYSTEM CONTROL')
    expect(wrapper.text()).not.toContain('ADMIN ONLY')
    expect(wrapper.text()).not.toContain('集中维护用户、角色和菜单权限')
    expect(wrapper.find('[data-testid="system-nav-users"]').exists()).toBe(false)
    expect(wrapper.find('[data-testid="system-nav-roles"]').exists()).toBe(false)
  })

  it('renders pagination for users, roles and menus independently', async () => {
    const users = await mountView('/system/users')
    await vi.waitFor(() => expect(users.text()).toContain('管理员'))
    expect(users.find('[data-testid="pagination-bar"]').exists()).toBe(true)

    const roles = await mountView('/system/roles')
    await vi.waitFor(() => expect(roles.text()).toContain('角色授权'))
    expect(roles.find('[data-testid="pagination-bar"]').exists()).toBe(true)

    const menus = await mountView('/system/menus')
    await vi.waitFor(() => expect(menus.text()).toContain('工作台'))
    expect(menus.find('[data-testid="pagination-bar"]').exists()).toBe(true)
  })

  it('creates a user with selected roles', async () => {
    const wrapper = await mountView()
    await vi.waitFor(() => expect(wrapper.text()).toContain('管理员'))
    await wrapper.get('[data-testid="create-user"]').trigger('click')
    expect(wrapper.find('.system-modal').exists()).toBe(true)
    await wrapper.get('input[name="username"]').setValue('analyst')
    await wrapper.get('input[name="displayName"]').setValue('分析员')
    await wrapper.get('input[name="password"]').setValue('secret')
    await wrapper.get('select[name="roleCodes"]').setValue(['ADMIN'])
    await wrapper.get('[data-testid="user-form"]').trigger('submit')

    expect(api.createUser).toHaveBeenCalledWith({ username: 'analyst', displayName: '分析员', password: 'secret', roleCodes: ['ADMIN'], enabled: true })
  })

  it('submits role menu authorization', async () => {
    const wrapper = await mountView('/system/roles')
    await vi.waitFor(() => expect(wrapper.text()).toContain('管理员'))
    await wrapper.get('[data-testid="role-row-1"]').trigger('click')
    expect(wrapper.find('.system-modal').exists()).toBe(true)
    await wrapper.get('[data-testid="menu-permission-1"]').setValue(true)
    await wrapper.get('[data-testid="role-form"]').trigger('submit')

    expect(api.updateRole).toHaveBeenCalledWith(1, expect.objectContaining({ code: 'ADMIN', menuIds: [1] }))
  })

  it('opens menu creation in a modal dialog', async () => {
    const wrapper = await mountView('/system/menus')
    await vi.waitFor(() => expect(wrapper.text()).toContain('工作台'))
    await wrapper.get('.system-section-heading .primary-action').trigger('click')

    expect(wrapper.find('.system-modal').exists()).toBe(true)
    expect(wrapper.find('[data-testid="menu-form"]').exists()).toBe(true)
  })

  it('shows a permission message when management data is forbidden', async () => {
    const forbidden = Object.assign(new Error('forbidden'), { response: { status: 403 } })
    api.listUsers.mockRejectedValue(forbidden)
    api.listRoles.mockRejectedValue(forbidden)
    api.listMenus.mockRejectedValue(forbidden)
    const wrapper = await mountView()

    await vi.waitFor(() => expect(wrapper.text()).toContain('没有执行此操作的权限'))
  })
})
