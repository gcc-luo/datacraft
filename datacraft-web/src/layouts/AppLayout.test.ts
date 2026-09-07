// @vitest-environment jsdom
import { describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { mount } from '@vue/test-utils'
import { createMemoryHistory, createRouter } from 'vue-router'
import AppLayout from './AppLayout.vue'
import { useAuthStore } from '../stores/auth'

describe('AppLayout', () => {
  it('renders menu labels from the auth store', async () => {
    const pinia = createPinia()
    setActivePinia(pinia)
    const store = useAuthStore()
    store.menuTree = [{ code: 'home', title: '工作台', path: '/', icon: 'home', sortOrder: 10, children: [] }]
    store.user = { id: 1, username: 'admin', displayName: '管理员', roles: ['ADMIN'] }
    store.token = 'token'
    const router = createRouter({ history: createMemoryHistory(), routes: [{ path: '/', component: AppLayout }] })
    await router.push('/')
    await router.isReady()

    const wrapper = mount(AppLayout, { global: { plugins: [pinia, router], stubs: { RouterView: true } } })

    expect(wrapper.text()).toContain('工作台')
    expect(wrapper.text()).toContain('管理员')
    expect(wrapper.find('.app-nav__icon svg').exists()).toBe(true)
    expect(wrapper.find('.app-nav__icon svg path').attributes('d')).toContain('M3 10.5')
  })

  it('renders system settings children and navigates between second-level routes', async () => {
    const pinia = createPinia()
    setActivePinia(pinia)
    const store = useAuthStore()
    store.menuTree = [{
      code: 'system', title: '系统设置', path: '/system', icon: 'settings', sortOrder: 70,
      children: [
        { code: 'system-users', title: '用户管理', path: '/system/users', icon: 'users', sortOrder: 10, children: [] },
        { code: 'system-roles', title: '角色管理', path: '/system/roles', icon: 'shield', sortOrder: 20, children: [] },
        { code: 'system-menus', title: '菜单管理', path: '/system/menus', icon: 'menu', sortOrder: 30, children: [] },
      ],
    }]
    store.user = { id: 1, username: 'admin', displayName: '管理员', roles: ['ADMIN'] }
    store.token = 'token'
    const router = createRouter({
      history: createMemoryHistory(),
      routes: [
        { path: '/system/users', component: AppLayout },
        { path: '/system/roles', component: AppLayout },
        { path: '/system/menus', component: AppLayout },
      ],
    })
    await router.push('/system/users')
    await router.isReady()

    const wrapper = mount(AppLayout, { global: { plugins: [pinia, router], stubs: { RouterView: true } } })

    expect(wrapper.get('[data-testid="nav-system"]').text()).toContain('系统设置')
    expect(wrapper.get('[data-testid="nav-system-users"]').text()).toContain('用户管理')
    expect(wrapper.get('[data-testid="nav-system-roles"]').text()).toContain('角色管理')
    expect(wrapper.get('[data-testid="nav-system-menus"]').text()).toContain('菜单管理')
    expect(wrapper.get('[data-testid="nav-system-users"]').classes()).toContain('is-active')

    await wrapper.get('[data-testid="nav-system-roles"]').trigger('click')
    await vi.waitFor(() => expect(router.currentRoute.value.path).toBe('/system/roles'))
  })
})
