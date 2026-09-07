export interface QualityRuleRequest {
  type: string
  field: string
  min?: number | null
  max?: number | null
  regex?: string | null
  minLength?: number | null
  maxLength?: number | null
  values?: string[]
  condition?: string | null
}

export interface QualityCheckRequest {
  datasourceId: number
  tableName: string
  rules: QualityRuleRequest[]
}

export interface QualitySampleResponse {
  id: number
  sampleIndex: number
  data: Record<string, unknown>
}

export interface QualityResultResponse {
  id: number
  executionId: number | null
  nodeExecutionId: number | null
  ruleType: string
  datasourceId: number
  tableName: string
  fieldName: string | null
  totalRows: number
  errorRows: number
  passRows: number
  passRate: number
  status: string
  samples: QualitySampleResponse[]
  createdAt: string
}
