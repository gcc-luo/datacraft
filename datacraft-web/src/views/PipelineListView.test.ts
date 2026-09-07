// @vitest-environment jsdom
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createMemoryHistory, createRouter } from 'vue-router'
import PipelineListView from './PipelineListView.vue'
import * as pipelineApi from '../api/pipelines'

vi.mock('../api/pipelines', () => ({
  listPipelines: vi.fn(),
  deletePipeline: vi.fn(),
}))

const listPipelines = vi.mocked(pipelineApi.listPipelines)
const deletePipeline = vi.mocked(pipelineApi.deletePipeline)

const sample = {
  id: 7, name: 'customer sync', description: 'daily flow', status: 'DRAFT' as const, version: 3,
  executionStrategy: 'AUTO' as const, createdAt: '2026-09-07T03:00:00Z', updatedAt: '2026-09-07T03:00:00Z',
}

function mountView() {
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/', redirect: '/pipelines' },
      { path: '/pipelines', name: 'pipelines', component: PipelineListView },
      { path: '/pipelines/:id', name: 'pipeline-editor', component: { template: '<div>editor</div>' } },
    ],
  })
  return { wrapper: mount(PipelineListView, { global: { plugins: [router] } }), router }
}

describe('PipelineListView', () => {
  beforeEach(() => {
    vi.resetAllMocks()
    listPipelines.mockResolvedValue([sample])
    deletePipeline.mockResolvedValue()
  })

  it('renders pipeline identity and links to its editor', async () => {
    const { wrapper } = mountView()
    await vi.waitFor(() => expect(wrapper.text()).toContain('customer sync'))

    expect(wrapper.text()).toContain('草稿')
    expect(wrapper.text()).toContain('v3')
    expect(wrapper.find('a[href="/pipelines/7"]').exists()).toBe(true)
  })

  it('navigates to a new pipeline editor', async () => {
    const { wrapper, router } = mountView()
    await vi.waitFor(() => expect(wrapper.text()).toContain('customer sync'))

    await wrapper.get('[data-testid="create-pipeline"]').trigger('click')
    await vi.waitFor(() => expect(router.currentRoute.value.fullPath).toBe('/pipelines/new'))
  })

  it('deletes a pipeline after confirmation and shows the empty state', async () => {
    vi.spyOn(window, 'confirm').mockReturnValue(true)
    const { wrapper } = mountView()
    await vi.waitFor(() => expect(wrapper.text()).toContain('customer sync'))

    await wrapper.get('[data-testid="delete-pipeline"]').trigger('click')
    expect(deletePipeline).toHaveBeenCalledWith(7)
    await vi.waitFor(() => expect(wrapper.text()).toContain('还没有 Pipeline'))
  })
})
