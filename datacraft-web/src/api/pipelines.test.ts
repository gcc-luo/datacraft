import { beforeEach, describe, expect, it, vi } from 'vitest'
import http from './http'
import { createPipeline, getPipeline, listNodeTypes, listPipelines } from './pipelines'

vi.mock('./http', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
  },
}))

describe('pipeline api', () => {
  beforeEach(() => vi.resetAllMocks())

  it('unwraps pipeline list and node metadata responses', async () => {
    vi.mocked(http.get)
      .mockResolvedValueOnce({ data: { code: '0', message: 'success', data: [{ id: 1, name: 'sync' }] } })
      .mockResolvedValueOnce({ data: { code: '0', message: 'success', data: [{ type: 'FILTER', name: 'Filter' }] } })

    await expect(listPipelines()).resolves.toEqual([{ id: 1, name: 'sync' }])
    await expect(listNodeTypes()).resolves.toEqual([{ type: 'FILTER', name: 'Filter' }])
    expect(http.get).toHaveBeenNthCalledWith(1, '/v1/pipelines')
    expect(http.get).toHaveBeenNthCalledWith(2, '/v1/node-types')
  })

  it('loads and creates pipeline resources through the typed client', async () => {
    vi.mocked(http.get).mockResolvedValue({ data: { code: '0', message: 'success', data: { id: 7, name: 'sync' } } })
    vi.mocked(http.post).mockResolvedValue({ data: { code: '0', message: 'success', data: { id: 7, name: 'sync' } } })

    await expect(getPipeline(7)).resolves.toEqual({ id: 7, name: 'sync' })
    await expect(createPipeline({ name: 'sync', nodes: [], edges: [] })).resolves.toEqual({ id: 7, name: 'sync' })
    expect(http.get).toHaveBeenCalledWith('/v1/pipelines/7')
    expect(http.post).toHaveBeenCalledWith('/v1/pipelines', { name: 'sync', nodes: [], edges: [] })
  })
})
