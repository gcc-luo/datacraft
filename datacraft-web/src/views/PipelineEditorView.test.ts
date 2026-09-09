// @vitest-environment jsdom
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createMemoryHistory, createRouter } from 'vue-router'
import PipelineEditorView from './PipelineEditorView.vue'
import * as pipelineApi from '../api/pipelines'

vi.mock('../api/pipelines', () => ({
  getPipeline: vi.fn(),
  listNodeTypes: vi.fn(),
  createPipeline: vi.fn(),
  updatePipeline: vi.fn(),
  deletePipeline: vi.fn(),
}))

const getPipeline = vi.mocked(pipelineApi.getPipeline)
const listNodeTypes = vi.mocked(pipelineApi.listNodeTypes)
const createPipeline = vi.mocked(pipelineApi.createPipeline)
const deletePipeline = vi.mocked(pipelineApi.deletePipeline)

const sourceMetadata = { type: 'DATABASE_SOURCE', name: '数据库', category: 'SOURCE' as const, icon: 'database', supportedEngines: ['NATIVE'], defaultEngine: 'NATIVE', configSchema: {} }
const sample = {
  id: 7, name: 'customer sync', description: 'daily flow', status: 'DRAFT' as const, version: 3,
  executionStrategy: 'AUTO' as const, createdAt: '2026-09-07T03:00:00Z', updatedAt: '2026-09-07T03:00:00Z',
}

function mountView(path = '/pipelines/new') {
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/pipelines', name: 'pipelines', component: { template: '<div>list</div>' } },
      { path: '/pipelines/:id', name: 'pipeline-editor', component: PipelineEditorView },
    ],
  })
  return router.push(path).then(async () => {
    await router.isReady()
    const wrapper = mount(PipelineEditorView, {
      global: {
        plugins: [router],
        stubs: {
          VueFlow: { template: '<div data-testid="flow-stub"><slot /></div>' },
          Background: { template: '<div />' },
          Controls: { template: '<div />' },
          MiniMap: { template: '<div />' },
        },
      },
    })
    return { wrapper, router }
  })
}

describe('PipelineEditorView', () => {
  beforeEach(() => {
    vi.resetAllMocks()
    listNodeTypes.mockResolvedValue([sourceMetadata])
    getPipeline.mockResolvedValue({ ...sample, nodes: [], edges: [] })
    createPipeline.mockResolvedValue({ ...sample, id: 9, name: 'Hourly Orders' })
    deletePipeline.mockResolvedValue()
  })

  it('creates a pipeline from metadata and dropped nodes', async () => {
    const { wrapper, router } = await mountView()
    await vi.waitFor(() => expect(wrapper.text()).toContain('新建 Pipeline'))

    await wrapper.get('[data-testid="pipeline-name"]').setValue('Hourly Orders')
    const dataTransfer = { getData: vi.fn().mockReturnValue('DATABASE_SOURCE') }
    await wrapper.get('[data-testid="pipeline-canvas"]').trigger('drop', { dataTransfer, clientX: 340, clientY: 180 })
    await wrapper.get('[data-testid="save-pipeline"]').trigger('click')

    await vi.waitFor(() => expect(createPipeline).toHaveBeenCalled())
    expect(createPipeline.mock.calls[0][0]).toMatchObject({ name: 'Hourly Orders', nodes: [{ nodeType: 'DATABASE_SOURCE', nodeName: '数据库' }] })
    await vi.waitFor(() => expect(router.currentRoute.value.fullPath).toBe('/pipelines/9'))
  })

  it('deletes an existing pipeline after confirmation', async () => {
    vi.spyOn(window, 'confirm').mockReturnValue(true)
    const { wrapper, router } = await mountView('/pipelines/7')
    await vi.waitFor(() => expect(wrapper.text()).toContain('customer sync'))

    await wrapper.get('[data-testid="delete-pipeline"]').trigger('click')

    expect(deletePipeline).toHaveBeenCalledWith(7)
    await vi.waitFor(() => expect(router.currentRoute.value.fullPath).toBe('/pipelines'))
  })

  it('closes and reopens the modal inspector without changing the canvas', async () => {
    const { wrapper } = await mountView()
    await vi.waitFor(() => expect(wrapper.text()).toContain('新建 Pipeline'))

    expect(wrapper.get('[data-testid="inspector-modal"]').attributes('role')).toBe('dialog')
    await wrapper.get('[data-testid="inspector-close"]').trigger('click')
    expect(wrapper.find('[data-testid="inspector-modal"]').exists()).toBe(false)
    expect(wrapper.get('[data-testid="inspector-open"]').text()).toContain('属性')

    await wrapper.get('[data-testid="inspector-open"]').trigger('click')
    expect(wrapper.find('[data-testid="inspector-modal"]').exists()).toBe(true)

    window.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape' }))
    await vi.waitFor(() => expect(wrapper.find('[data-testid="inspector-modal"]').exists()).toBe(false))
  })
})
