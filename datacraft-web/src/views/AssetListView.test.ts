// @vitest-environment jsdom
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import AssetListView from './AssetListView.vue'
import * as datasourceApi from '../api/datasources'
import * as metadataApi from '../api/metadata'

vi.mock('../api/datasources', () => ({ listDatasources: vi.fn() }))
vi.mock('../api/metadata', () => ({
  listDatasets: vi.fn(),
  getDataset: vi.fn(),
  syncDatasourceMetadata: vi.fn(),
}))

const listDatasources = vi.mocked(datasourceApi.listDatasources)
const listDatasets = vi.mocked(metadataApi.listDatasets)
const getDataset = vi.mocked(metadataApi.getDataset)
const syncDatasourceMetadata = vi.mocked(metadataApi.syncDatasourceMetadata)

const datasource = {
  id: 1, name: 'warehouse', type: 'POSTGRESQL' as const, host: 'localhost', port: 5432,
  databaseName: 'datacraft', username: 'reader', remark: null, status: 'SUCCESS' as const,
  lastTestedAt: null, lastTestLatencyMs: null, lastTestMessage: null,
  createdAt: '2026-09-07T02:00:00Z', updatedAt: '2026-09-07T02:00:00Z',
}

const customer = {
  id: 9, datasourceId: 1, catalogName: 'datacraft', schemaName: 'public', tableName: 'customer',
  tableRemark: '客户主表', estimatedRowCount: 12, collectedAt: '2026-09-07T03:00:00Z',
}

describe('AssetListView', () => {
  beforeEach(() => {
    vi.resetAllMocks()
    listDatasources.mockResolvedValue([datasource])
    listDatasets.mockResolvedValue([customer])
    getDataset.mockResolvedValue({
      ...customer,
      fields: [
        { id: 1, fieldName: 'id', ordinalPosition: 1, dataType: 'BIGINT', nullable: false, primaryKey: true, fieldRemark: null },
        { id: 2, fieldName: 'email', ordinalPosition: 2, dataType: 'VARCHAR(255)', nullable: false, primaryKey: false, fieldRemark: '联系邮箱' },
      ],
    })
    syncDatasourceMetadata.mockResolvedValue({
      datasourceId: 1, datasetCount: 1, fieldCount: 2, schemaCount: 1, collectedAt: '2026-09-07T03:00:00Z',
    })
  })

  it('renders the breadcrumb, schema table list and selected field details', async () => {
    const wrapper = mount(AssetListView)
    await vi.waitFor(() => expect(wrapper.text()).toContain('customer'))

    expect(wrapper.text()).toContain('资产目录')
    expect(wrapper.text()).toContain('public')
    expect(wrapper.text()).toContain('email')
    expect(wrapper.text()).toContain('VARCHAR(255)')
    expect(wrapper.findAll('[data-testid="pagination-bar"]')).toHaveLength(2)
    expect(wrapper.text()).not.toContain('password_ciphertext')
  })

  it('syncs metadata for the selected datasource and reloads the catalog', async () => {
    const wrapper = mount(AssetListView)
    await vi.waitFor(() => expect(wrapper.text()).toContain('customer'))

    await wrapper.get('[data-testid="sync-metadata"]').trigger('click')

    expect(syncDatasourceMetadata).toHaveBeenCalledWith(1)
    await vi.waitFor(() => expect(listDatasets).toHaveBeenCalledTimes(2))
    await vi.waitFor(() => expect(wrapper.text()).toContain('已同步 1 张表'))
  })

  it('filters table and field content by keyword', async () => {
    const wrapper = mount(AssetListView)
    await vi.waitFor(() => expect(wrapper.text()).toContain('customer'))

    await wrapper.get('[data-testid="asset-search"]').setValue('email')

    expect(wrapper.text()).toContain('customer')
    expect(wrapper.text()).toContain('email')
    expect(wrapper.text()).not.toContain('orders')
  })
})
