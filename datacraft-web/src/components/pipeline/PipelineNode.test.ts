// @vitest-environment jsdom
import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import PipelineNode from './PipelineNode.vue'

describe('PipelineNode', () => {
  it('renders node identity, engine preference and handles', () => {
    const wrapper = mount(PipelineNode, {
      props: {
        id: 'source', type: 'pipeline', selected: false, connectable: true, position: { x: 0, y: 0 }, dimensions: { width: 180, height: 80 }, dragging: false, resizing: false, zIndex: 0, events: {} as any,
        data: {
          nodeKey: 'source', nodeType: 'DATABASE_SOURCE', nodeName: 'Customer DB', configJson: '{}', preferredEngine: 'NATIVE',
          metadata: { type: 'DATABASE_SOURCE', name: '数据库源', category: 'SOURCE', icon: 'database', supportedEngines: ['NATIVE'], defaultEngine: 'NATIVE', configSchema: {} },
        },
      },
      global: {
        stubs: {
          Handle: { template: '<span class="vue-flow__handle" />' },
        },
      },
    })

    expect(wrapper.text()).toContain('Customer DB')
    expect(wrapper.text()).toContain('source')
    expect(wrapper.text()).toContain('数据源')
    expect(wrapper.text()).toContain('NATIVE')
    expect(wrapper.findAll('.vue-flow__handle')).toHaveLength(2)
  })
})
