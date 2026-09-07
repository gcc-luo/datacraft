import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { pinia } from '../stores/pinia'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', name: 'login', component: () => import('../views/LoginView.vue'), meta: { public: true } },
    {
      path: '/', name: 'app', component: () => import('../layouts/AppLayout.vue'),
      children: [
        { path: '', name: 'home', component: () => import('../views/HomeView.vue') },
        { path: 'datasources', name: 'datasources', component: () => import('../views/DatasourceListView.vue') },
        { path: 'assets', name: 'assets', component: () => import('../views/AssetListView.vue') },
        { path: 'pipelines', name: 'pipelines', component: () => import('../views/PipelineListView.vue') },
        { path: 'pipelines/:id', name: 'pipeline-editor', component: () => import('../views/PipelineEditorView.vue') },
        { path: 'quality', name: 'quality', component: () => import('../views/QualityView.vue') },
        { path: 'system', name: 'system', redirect: { name: 'system-users' } },
        { path: 'system/users', name: 'system-users', component: () => import('../views/SystemManagementView.vue'), meta: { systemSection: 'users' } },
        { path: 'system/roles', name: 'system-roles', component: () => import('../views/SystemManagementView.vue'), meta: { systemSection: 'roles' } },
        { path: 'system/menus', name: 'system-menus', component: () => import('../views/SystemManagementView.vue'), meta: { systemSection: 'menus' } },
        { path: ':catchAll(.*)', name: 'placeholder', component: () => import('../views/PlaceholderView.vue') },
      ],
    },
  ],
})

router.beforeEach(async (to) => {
  const auth = useAuthStore(pinia)
  if (!auth.ready) await auth.restoreSession()
  if (to.meta.public && auth.isAuthenticated) return { name: 'home' }
  if (!to.meta.public && !auth.isAuthenticated) return { name: 'login', query: { redirect: to.fullPath } }
  return true
})

export default router
