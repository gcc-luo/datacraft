import { describe, expect, it } from 'vitest'
import { canvasToRequest, detailToCanvas } from './pipelineGraph'

describe('pipeline graph mapping', () => {
  it('maps API node keys to Vue Flow ids and back to API graph requests', () => {
    const canvas = detailToCanvas({
      id: 7, name: 'sync', description: 'demo', status: 'DRAFT', version: 1,
      executionStrategy: 'AUTO', createdAt: '2026-09-07T03:00:00Z', updatedAt: '2026-09-07T03:00:00Z',
      nodes: [{ id: 11, nodeKey: 'source', nodeType: 'DATABASE_SOURCE', nodeName: 'Source', x: 10, y: 20, configJson: '{}', preferredEngine: 'NATIVE' }],
      edges: [{ id: 12, sourceNodeKey: 'source', targetNodeKey: 'sink', sourcePort: 'out', targetPort: 'in', conditionJson: null }],
    })

    expect(canvas.nodes[0]).toMatchObject({ id: 'source', position: { x: 10, y: 20 }, data: { nodeKey: 'source' } })
    expect(canvas.edges[0]).toMatchObject({ source: 'source', target: 'sink', sourceHandle: 'out', targetHandle: 'in' })

    const request = canvasToRequest('sync', 'demo', 'DRAFT', 'AUTO', canvas.nodes, canvas.edges)
    expect(request.nodes[0]).toMatchObject({ nodeKey: 'source', nodeType: 'DATABASE_SOURCE', x: 10, y: 20 })
    expect(request.edges[0]).toMatchObject({ sourceNodeKey: 'source', targetNodeKey: 'sink', sourcePort: 'out', targetPort: 'in' })
  })
})
