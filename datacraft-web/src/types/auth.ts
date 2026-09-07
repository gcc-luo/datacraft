export interface UserSummary {
  id: number
  username: string
  displayName: string
  roles: string[]
}

export interface MenuDto {
  code: string
  title: string
  path: string
  icon?: string
  parentId?: number | null
  sortOrder: number
  children: MenuDto[]
}

export interface LoginResponse {
  accessToken: string
  tokenType: string
  expiresIn: number
  user: UserSummary
}

export interface ApiResponse<T> {
  code: string
  message: string
  data: T
}
