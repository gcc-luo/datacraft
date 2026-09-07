import http from './http'
import type { ApiResponse, LoginResponse, MenuDto, UserSummary } from '../types/auth'

function unwrap<T>(response: ApiResponse<T>): T {
  if (response.code !== '0') throw new Error(response.message)
  return response.data
}

export async function login(username: string, password: string): Promise<LoginResponse> {
  const response = await http.post<ApiResponse<LoginResponse>>('/v1/auth/login', { username, password })
  return unwrap(response.data)
}

export async function me(): Promise<UserSummary> {
  const response = await http.get<ApiResponse<UserSummary>>('/v1/auth/me')
  return unwrap(response.data)
}

export async function menus(): Promise<MenuDto[]> {
  const response = await http.get<ApiResponse<MenuDto[]>>('/v1/system/menus')
  return unwrap(response.data)
}
