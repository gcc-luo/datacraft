import http from './http'
import type { ApiResponse } from '../types/auth'
import type { DatasetDetailResponse, DatasetResponse, MetadataSyncResponse } from '../types/metadata'

function unwrap<T>(response: ApiResponse<T>): T {
  if (response.code !== '0') throw new Error(response.message)
  return response.data
}

export async function listDatasets(datasourceId?: number, schemaName?: string, keyword?: string): Promise<DatasetResponse[]> {
  const response = await http.get<ApiResponse<DatasetResponse[]>>('/v1/datasets', {
    params: { datasourceId, schemaName, keyword },
  })
  return unwrap(response.data)
}

export async function getDataset(id: number): Promise<DatasetDetailResponse> {
  const response = await http.get<ApiResponse<DatasetDetailResponse>>(`/v1/datasets/${id}`)
  return unwrap(response.data)
}

export async function syncDatasourceMetadata(datasourceId: number): Promise<MetadataSyncResponse> {
  const response = await http.post<ApiResponse<MetadataSyncResponse>>(`/v1/datasources/${datasourceId}/metadata/sync`)
  return unwrap(response.data)
}
