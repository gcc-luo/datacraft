// @vitest-environment jsdom
import { describe, expect, it } from 'vitest'
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
})
