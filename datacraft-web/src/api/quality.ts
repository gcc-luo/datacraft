import http from './http'
import type { ApiResponse } from '../types/auth'
import type { QualityCheckRequest, QualityResultResponse } from '../types/quality'

function unwrap<T>(response: ApiResponse<T>): T {
  if (response.code !== '0') throw new Error(response.message)
  return response.data
}

export async function listQualityResults(): Promise<QualityResultResponse[]> {
  const response = await http.get<ApiResponse<QualityResultResponse[]>>('/v1/quality/results')
  return unwrap(response.data)
}

export async function runQualityCheck(request: QualityCheckRequest): Promise<QualityResultResponse[]> {
  const response = await http.post<ApiResponse<QualityResultResponse[]>>('/v1/quality/checks', request)
  return unwrap(response.data)
}
