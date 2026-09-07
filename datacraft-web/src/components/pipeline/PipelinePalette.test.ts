// @vitest-environment jsdom
import { describe, expect, it, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import PipelinePalette from './PipelinePalette.vue'

const metadata = [
  { type: 'DATABASE_SOURCE', name: 'Database Source', category: 'SOURCE' as const, icon: 'database', supportedEngines: ['NATIVE'], defaultEngine: 'NATIVE', configSchema: {} },
  { type: 'FILTER', name: 'Filter', category: 'TRANSFORM' as const, icon: 'filter', supportedEngines: ['NATIVE'], defaultEngine: 'NATIVE', configSchema: {} },
]

describe('PipelinePalette', () => {
  it('groups metadata and filters searchable node types', async () => {
    const wrapper = mount(PipelinePalette, { props: { metadata } })

    expect(wrapper.text()).toContain('数据源')
    expect(wrapper.text()).toContain('Database Source')
    expect(wrapper.text()).toContain('处理')

    await wrapper.get('[data-testid="palette-search"]').setValue('filter')
    expect(wrapper.text()).toContain('Filter')
    expect(wrapper.text()).not.toContain('Database Source')
  })

  it('emits the node type and configures native drag data', async () => {
    const wrapper = mount(PipelinePalette, { props: { metadata } })
    const dataTransfer = { setData: vi.fn(), effectAllowed: '' }

    await wrapper.get('[data-testid="palette-FILTER"]').trigger('dragstart', { dataTransfer })

    expect(dataTransfer.setData).toHaveBeenCalledWith('application/datacraft-node', 'FILTER')
    expect(wrapper.emitted('drag-node')?.[0]).toEqual(['FILTER'])
  })
})
