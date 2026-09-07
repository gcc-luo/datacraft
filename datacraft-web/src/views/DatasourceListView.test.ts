// @vitest-environment jsdom
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import DatasourceListView from './DatasourceListView.vue'
import * as datasourceApi from '../api/datasources'

vi.mock('../api/datasources', () => ({
  listDatasources: vi.fn(),
  createDatasource: vi.fn(),
  updateDatasource: vi.fn(),
  testDatasource: vi.fn(),
  deleteDatasource: vi.fn(),
}))

const listDatasources = vi.mocked(datasourceApi.listDatasources)
const createDatasource = vi.mocked(datasourceApi.createDatasource)
const updateDatasource = vi.mocked(datasourceApi.updateDatasource)
const testDatasource = vi.mocked(datasourceApi.testDatasource)
const deleteDatasource = vi.mocked(datasourceApi.deleteDatasource)

const sample = {
  id: 1,
  name: 'warehouse',
  type: 'POSTGRESQL' as const,
  host: 'localhost',
  port: 5432,
  databaseName: 'datacraft',
  username: 'reader',
  remark: null,
  status: 'UNKNOWN' as const,
  lastTestedAt: null,
  lastTestLatencyMs: null,
  lastTestMessage: null,
  createdAt: '2026-09-07T02:00:00Z',
  updatedAt: '2026-09-07T02:00:00Z',
}

describe('DatasourceListView', () => {
  beforeEach(() => {
    vi.resetAllMocks()
    listDatasources.mockResolvedValue([sample])
    createDatasource.mockResolvedValue(sample)
    updateDatasource.mockResolvedValue(sample)
    testDatasource.mockResolvedValue({ success: true, status: 'SUCCESS', latencyMs: 12, message: '连接成功', testedAt: '2026-09-07T02:00:00Z' })
    deleteDatasource.mockResolvedValue()
  })

  it('loads datasource rows without exposing a password field', async () => {
    const wrapper = mount(DatasourceListView)
    await vi.waitFor(() => expect(wrapper.text()).toContain('warehouse'))

    expect(wrapper.text()).toContain('PostgreSQL')
    expect(wrapper.find('input[name="password"]').exists()).toBe(false)
    expect(wrapper.text()).not.toContain('password_ciphertext')
  })

  it('creates a datasource with a password and leaves edit password blank', async () => {
    const wrapper = mount(DatasourceListView)
    await vi.waitFor(() => expect(wrapper.text()).toContain('warehouse'))
    await wrapper.get('[data-testid="create-datasource"]').trigger('click')

    await wrapper.get('input[name="name"]').setValue('analytics')
    await wrapper.get('input[name="host"]').setValue('localhost')
    await wrapper.get('input[name="databaseName"]').setValue('analytics')
    await wrapper.get('input[name="username"]').setValue('reader')
    await wrapper.get('input[name="password"]').setValue('secret')
    await wrapper.get('[data-testid="save-datasource"]').trigger('click')
    expect(createDatasource).not.toHaveBeenCalled()

    await wrapper.get('[data-testid="test-form-datasource"]').trigger('click')
    expect(testDatasource).toHaveBeenCalledWith(expect.objectContaining({ name: 'analytics', password: 'secret' }), undefined)
    await wrapper.get('form').trigger('submit')

    expect(createDatasource).toHaveBeenCalledWith(expect.objectContaining({ name: 'analytics', password: 'secret' }))

    await wrapper.get('[data-testid="edit-datasource"]').trigger('click')
    expect((wrapper.get('input[name="password"]').element as HTMLInputElement).value).toBe('')
  })

  it('requires another successful connection test after changing the form', async () => {
    const wrapper = mount(DatasourceListView)
    await vi.waitFor(() => expect(wrapper.text()).toContain('warehouse'))
    await wrapper.get('[data-testid="create-datasource"]').trigger('click')
    await wrapper.get('input[name="name"]').setValue('analytics')
    await wrapper.get('input[name="host"]').setValue('localhost')
    await wrapper.get('input[name="databaseName"]').setValue('analytics')
    await wrapper.get('input[name="username"]').setValue('reader')
    await wrapper.get('input[name="password"]').setValue('secret')
    await wrapper.get('[data-testid="test-form-datasource"]').trigger('click')

    expect((wrapper.get('[data-testid="save-datasource"]').element as HTMLButtonElement).disabled).toBe(false)
    await wrapper.get('input[name="host"]').setValue('db.internal')

    expect((wrapper.get('[data-testid="save-datasource"]').element as HTMLButtonElement).disabled).toBe(true)
  })

  it('tests and deletes a saved datasource after confirmation', async () => {
    vi.spyOn(window, 'confirm').mockReturnValue(true)
    const wrapper = mount(DatasourceListView)
    await vi.waitFor(() => expect(wrapper.text()).toContain('warehouse'))

    await wrapper.get('[data-testid="test-datasource"]').trigger('click')
    expect(testDatasource).toHaveBeenCalledWith(1)
    await wrapper.get('[data-testid="delete-datasource"]').trigger('click')
    expect(deleteDatasource).toHaveBeenCalledWith(1)
  })
})
