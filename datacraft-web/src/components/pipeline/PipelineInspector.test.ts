// @vitest-environment jsdom
import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import PipelineInspector from './PipelineInspector.vue'

const metadata = [
  { type: 'DATABASE_SOURCE', name: '数据库源', category: 'SOURCE' as const, icon: 'database', supportedEngines: ['NATIVE', 'DATAX'], defaultEngine: 'NATIVE', configSchema: {} },
]

const pipeline = { name: 'Daily Orders', description: 'Sync orders', status: 'DRAFT' as const, executionStrategy: 'AUTO' as const }

describe('PipelineInspector', () => {
  it('edits pipeline properties when no node is selected', async () => {
    const wrapper = mount(PipelineInspector, { props: { pipeline, selectedNode: null, nodeMetadata: metadata } })

    await wrapper.get('[data-testid="pipeline-name"]').setValue('Hourly Orders')
    await wrapper.get('[data-testid="pipeline-description"]').setValue('Updated description')

    expect(wrapper.emitted('update:pipeline')).toEqual([
      [{ name: 'Hourly Orders' }],
      [{ description: 'Updated description' }],
    ])
  })

  it('edits node properties and available engine', async () => {
    const selectedNode = {
      nodeKey: 'source', nodeType: 'DATABASE_SOURCE', nodeName: 'Customer DB', configJson: '{}', preferredEngine: 'NATIVE', position: { x: 40, y: 60 }, metadata: metadata[0],
    }
    const wrapper = mount(PipelineInspector, { props: { pipeline, selectedNode, nodeMetadata: metadata } })

    await wrapper.get('[data-testid="node-name"]').setValue('Orders DB')
    await wrapper.get('[data-testid="node-engine"]').setValue('DATAX')

    expect(wrapper.emitted('update:selected-node')).toEqual([
      [{ nodeName: 'Orders DB' }],
      [{ preferredEngine: 'DATAX' }],
    ])
  })

  it('keeps invalid JSON from updating node config', async () => {
    const selectedNode = {
      nodeKey: 'source', nodeType: 'DATABASE_SOURCE', nodeName: 'Customer DB', configJson: '{}', preferredEngine: null, position: { x: 40, y: 60 }, metadata: metadata[0],
    }
    const wrapper = mount(PipelineInspector, { props: { pipeline, selectedNode, nodeMetadata: metadata } })

    await wrapper.get('[data-testid="node-config"]').setValue('{invalid')
    await wrapper.get('[data-testid="node-config"]').trigger('blur')

    expect(wrapper.get('[data-testid="config-error"]').text()).toContain('JSON')
    expect(wrapper.emitted('update:selected-node')).toBeUndefined()
  })
})
