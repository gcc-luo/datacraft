import http from './http'
import type { ApiResponse } from '../types/auth'
import type { AdminMenuRequest, AdminMenuResponse, AdminRoleRequest, AdminRoleResponse, AdminUserRequest, AdminUserResponse } from '../types/system'

function unwrap<T>(response: ApiResponse<T>): T {
  if (response.code !== '0') throw new Error(response.message)
  return response.data
}

async function get<T>(path: string): Promise<T> {
  const response = await http.get<ApiResponse<T>>(path)
  return unwrap(response.data)
}

async function post<T, R>(path: string, body: T): Promise<R> {
  const response = await http.post<ApiResponse<R>>(path, body)
  return unwrap(response.data)
}

async function put<T, R>(path: string, body: T): Promise<R> {
  const response = await http.put<ApiResponse<R>>(path, body)
  return unwrap(response.data)
}

export const listUsers = () => get<AdminUserResponse[]>('/v1/system/admin/users')
export const createUser = (request: AdminUserRequest) => post<AdminUserRequest, AdminUserResponse>('/v1/system/admin/users', request)
export const updateUser = (id: number, request: AdminUserRequest) => put<AdminUserRequest, AdminUserResponse>(`/v1/system/admin/users/${id}`, request)

export const listRoles = () => get<AdminRoleResponse[]>('/v1/system/admin/roles')
export const createRole = (request: AdminRoleRequest) => post<AdminRoleRequest, AdminRoleResponse>('/v1/system/admin/roles', request)
export const updateRole = (id: number, request: AdminRoleRequest) => put<AdminRoleRequest, AdminRoleResponse>(`/v1/system/admin/roles/${id}`, request)

export const listMenus = () => get<AdminMenuResponse[]>('/v1/system/admin/menus')
export const createMenu = (request: AdminMenuRequest) => post<AdminMenuRequest, AdminMenuResponse>('/v1/system/admin/menus', request)
export const updateMenu = (id: number, request: AdminMenuRequest) => put<AdminMenuRequest, AdminMenuResponse>(`/v1/system/admin/menus/${id}`, request)
