export interface MetadataSyncResponse {
  datasourceId: number
  datasetCount: number
  fieldCount: number
  schemaCount: number
  collectedAt: string
}

export interface DatasetResponse {
  id: number
  datasourceId: number
  catalogName: string | null
  schemaName: string
  tableName: string
  tableRemark: string | null
  estimatedRowCount: number | null
  collectedAt: string
}

export interface DatasetFieldResponse {
  id: number
  fieldName: string
  ordinalPosition: number
  dataType: string
  nullable: boolean
  primaryKey: boolean
  fieldRemark: string | null
}

export interface DatasetDetailResponse extends DatasetResponse {
  fields: DatasetFieldResponse[]
}
