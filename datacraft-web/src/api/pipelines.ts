import http from './http'
import type { ApiResponse } from '../types/auth'
import type { NodeMetadataResponse, PipelineDetailResponse, PipelineRequest, PipelineResponse } from '../types/pipeline'

function unwrap<T>(response: ApiResponse<T>): T {
  if (response.code !== '0') throw new Error(response.message)
  return response.data
}

export async function listPipelines(): Promise<PipelineResponse[]> {
  const response = await http.get<ApiResponse<PipelineResponse[]>>('/v1/pipelines')
  return unwrap(response.data)
}

export async function getPipeline(id: number): Promise<PipelineDetailResponse> {
  const response = await http.get<ApiResponse<PipelineDetailResponse>>(`/v1/pipelines/${id}`)
  return unwrap(response.data)
}

export async function createPipeline(request: PipelineRequest): Promise<PipelineResponse> {
  const response = await http.post<ApiResponse<PipelineResponse>>('/v1/pipelines', request)
  return unwrap(response.data)
}

export async function updatePipeline(id: number, request: PipelineRequest): Promise<PipelineResponse> {
  const response = await http.put<ApiResponse<PipelineResponse>>(`/v1/pipelines/${id}`, request)
  return unwrap(response.data)
}

export async function deletePipeline(id: number): Promise<void> {
  await http.delete<ApiResponse<void>>(`/v1/pipelines/${id}`)
}

export async function listNodeTypes(): Promise<NodeMetadataResponse[]> {
  const response = await http.get<ApiResponse<NodeMetadataResponse[]>>('/v1/node-types')
  return unwrap(response.data)
}
