// @vitest-environment jsdom
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import QualityView from './QualityView.vue'
import * as qualityApi from '../api/quality'
import * as datasourceApi from '../api/datasources'

vi.mock('../api/datasources', () => ({ listDatasources: vi.fn() }))

vi.mock('../api/quality', () => ({
  listQualityResults: vi.fn(),
  runQualityCheck: vi.fn(),
}))

const listQualityResults = vi.mocked(qualityApi.listQualityResults)
const runQualityCheck = vi.mocked(qualityApi.runQualityCheck)
const listDatasources = vi.mocked(datasourceApi.listDatasources)

const sample = {
  id: 11, executionId: null, nodeExecutionId: null, ruleType: 'NULL_CHECK', datasourceId: 1,
  tableName: 'customer', fieldName: 'phone', totalRows: 100, errorRows: 3, passRows: 97,
  passRate: 0.97, status: 'FAILED', samples: [{ id: 1, sampleIndex: 0, data: { id: 9, phone: null } }],
  createdAt: '2026-09-07T03:00:00Z',
}

describe('QualityView', () => {
  beforeEach(() => {
    vi.resetAllMocks()
    listQualityResults.mockResolvedValue([sample])
    runQualityCheck.mockResolvedValue([sample])
    listDatasources.mockResolvedValue([{ id: 1, name: 'main', type: 'POSTGRESQL', host: 'localhost', port: 5432, databaseName: 'db', username: 'admin', remark: null, status: 'SUCCESS', lastTestedAt: null, lastTestLatencyMs: null, lastTestMessage: null, createdAt: '', updatedAt: '' }])
  })

  it('renders quality metrics and error sample data', async () => {
    const wrapper = mount(QualityView)
    await vi.waitFor(() => expect(wrapper.text()).toContain('customer.phone'))
    expect(wrapper.text()).toContain('100')
    expect(wrapper.text()).toContain('97.00%')
    expect(wrapper.text()).toContain('phone: null')
    expect(wrapper.find('[data-testid="pagination-bar"]').exists()).toBe(true)
  })

  it('submits a null check and refreshes results', async () => {
    const wrapper = mount(QualityView)
    await vi.waitFor(() => expect(wrapper.text()).toContain('customer.phone'))
    await wrapper.get('[data-testid="quality-table"]').setValue('orders')
    await wrapper.get('[data-testid="quality-field"]').setValue('email')
    await wrapper.get('[data-testid="run-quality-check"]').trigger('click')
    expect(runQualityCheck).toHaveBeenCalledWith({ datasourceId: 1, tableName: 'orders', rules: [{ type: 'NULL_CHECK', field: 'email' }] })
  })
})
