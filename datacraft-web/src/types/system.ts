export interface AdminUserRequest {
  username: string
  displayName: string
  password?: string
  roleCodes: string[]
  enabled: boolean
}

export interface AdminUserResponse {
  id: number
  username: string
  displayName: string
  enabled: boolean
  roleCodes: string[]
  createdAt: string | null
  updatedAt: string | null
}

export interface AdminRoleRequest {
  code: string
  name: string
  menuIds: number[]
  enabled: boolean
}

export interface AdminRoleResponse {
  id: number
  code: string
  name: string
  enabled: boolean
  menuIds: number[]
  createdAt: string | null
  updatedAt: string | null
}

export interface AdminMenuRequest {
  code: string
  title: string
  path: string
  icon?: string
  parentId: number | null
  sortOrder: number
  enabled: boolean
}

export interface AdminMenuResponse extends AdminMenuRequest {
  id: number
  parentTitle: string | null
  createdAt: string | null
  updatedAt: string | null
}
