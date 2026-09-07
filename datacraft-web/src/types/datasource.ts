export type DatasourceType = 'POSTGRESQL' | 'MYSQL'
export type DatasourceStatus = 'UNKNOWN' | 'SUCCESS' | 'FAILED'

export interface DatasourceRequest {
  name: string
  type: DatasourceType
  host: string
  port: number
  databaseName: string
  username: string
  password?: string
  remark?: string
}

export interface DatasourceResponse {
  id: number
  name: string
  type: DatasourceType
  host: string
  port: number
  databaseName: string
  username: string
  remark: string | null
  status: DatasourceStatus
  lastTestedAt: string | null
  lastTestLatencyMs: number | null
  lastTestMessage: string | null
  createdAt: string
  updatedAt: string
}

export interface DatasourceTestResponse {
  success: boolean
  status: DatasourceStatus
  latencyMs: number
  message: string
  testedAt: string
}
