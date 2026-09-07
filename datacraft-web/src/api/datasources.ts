import http from './http'
import type { ApiResponse } from '../types/auth'
import type { DatasourceRequest, DatasourceResponse, DatasourceTestResponse } from '../types/datasource'

function unwrap<T>(response: ApiResponse<T>): T {
  if (response.code !== '0') throw new Error(response.message)
  return response.data
}

export async function listDatasources(): Promise<DatasourceResponse[]> {
  const response = await http.get<ApiResponse<DatasourceResponse[]>>('/v1/datasources')
  return unwrap(response.data)
}

export async function createDatasource(request: DatasourceRequest): Promise<DatasourceResponse> {
  const response = await http.post<ApiResponse<DatasourceResponse>>('/v1/datasources', request)
  return unwrap(response.data)
}

export async function updateDatasource(id: number, request: DatasourceRequest): Promise<DatasourceResponse> {
  const response = await http.put<ApiResponse<DatasourceResponse>>(`/v1/datasources/${id}`, request)
  return unwrap(response.data)
}

export async function deleteDatasource(id: number): Promise<void> {
  await http.delete<ApiResponse<void>>(`/v1/datasources/${id}`)
}

export async function testDatasource(id: number): Promise<DatasourceTestResponse> {
  const response = await http.post<ApiResponse<DatasourceTestResponse>>(`/v1/datasources/${id}/test`)
  return unwrap(response.data)
}
