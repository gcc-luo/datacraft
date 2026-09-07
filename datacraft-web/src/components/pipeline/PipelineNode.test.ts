// @vitest-environment jsdom
import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import PipelineNode from './PipelineNode.vue'

describe('PipelineNode', () => {
  it('renders node identity, engine preference and handles', () => {
    const wrapper = mount(PipelineNode, {
      props: {
        data: {
          nodeKey: 'source', nodeType: 'DATABASE_SOURCE', nodeName: 'Customer DB', configJson: '{}', preferredEngine: 'NATIVE',
          metadata: { type: 'DATABASE_SOURCE', name: 'Database Source', category: 'SOURCE', icon: 'database', supportedEngines: ['NATIVE'], defaultEngine: 'NATIVE', configSchema: {} },
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
    expect(wrapper.text()).toContain('NATIVE')
    expect(wrapper.findAll('.vue-flow__handle')).toHaveLength(2)
  })
})
