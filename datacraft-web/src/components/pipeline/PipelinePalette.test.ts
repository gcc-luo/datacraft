// @vitest-environment jsdom
import { describe, expect, it, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import PipelinePalette from './PipelinePalette.vue'

const metadata = [
  { type: 'DATABASE_SOURCE', name: '数据库源', category: 'SOURCE' as const, icon: 'database', supportedEngines: ['NATIVE'], defaultEngine: 'NATIVE', configSchema: {} },
  { type: 'FILTER', name: '数据筛选', category: 'TRANSFORM' as const, icon: 'filter', supportedEngines: ['NATIVE'], defaultEngine: 'NATIVE', configSchema: {} },
]

describe('PipelinePalette', () => {
  it('groups metadata and filters searchable node types', async () => {
    const wrapper = mount(PipelinePalette, { props: { metadata } })

    expect(wrapper.text()).toContain('节点库')
    expect(wrapper.text()).toContain('数据源')
    expect(wrapper.text()).toContain('数据库源')
    expect(wrapper.text()).toContain('处理')
    expect(wrapper.text()).toContain('提示')

    await wrapper.get('[data-testid="palette-search"]').setValue('filter')
    expect(wrapper.text()).toContain('数据筛选')
    expect(wrapper.text()).not.toContain('数据库源')
  })

  it('emits the node type and configures native drag data', async () => {
    const wrapper = mount(PipelinePalette, { props: { metadata } })
    const dataTransfer = { setData: vi.fn(), effectAllowed: '' }

    await wrapper.get('[data-testid="palette-FILTER"]').trigger('dragstart', { dataTransfer })

    expect(dataTransfer.setData).toHaveBeenCalledWith('application/datacraft-node', 'FILTER')
    expect(wrapper.emitted('drag-node')?.[0]).toEqual(['FILTER'])
  })
})
