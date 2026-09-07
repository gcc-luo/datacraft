import type { Edge, Node } from '@vue-flow/core'

export type PipelineStatus = 'DRAFT' | 'ACTIVE' | 'ARCHIVED'
export type ExecutionStrategy = 'AUTO' | 'NATIVE' | 'DATAX' | 'CAMEL' | 'SEATUNNEL'
export type NodeCategory = 'SOURCE' | 'TRANSFORM' | 'QUALITY' | 'GOVERNANCE' | 'SINK' | 'UTILITY'

export interface PipelineResponse {
  id: number
  name: string
  description: string | null
  status: PipelineStatus
  version: number
  executionStrategy: ExecutionStrategy
  createdAt: string
  updatedAt: string
}

export interface PipelineNodeResponse {
  id: number
  nodeKey: string
  nodeType: string
  nodeName: string
  x: number
  y: number
  configJson: string
  preferredEngine: string | null
}

export interface PipelineEdgeResponse {
  id: number
  sourceNodeKey: string
  targetNodeKey: string
  sourcePort: string | null
  targetPort: string | null
  conditionJson: string | null
}

export interface PipelineDetailResponse extends PipelineResponse {
  nodes: PipelineNodeResponse[]
  edges: PipelineEdgeResponse[]
}

export interface PipelineNodeRequest {
  nodeKey: string
  nodeType: string
  nodeName: string
  x: number
  y: number
  configJson: string
  preferredEngine: string | null
}

export interface PipelineEdgeRequest {
  sourceNodeKey: string
  targetNodeKey: string
  sourcePort: string | null
  targetPort: string | null
  conditionJson: string | null
}

export interface PipelineRequest {
  name: string
  description?: string | null
  status?: PipelineStatus
  executionStrategy?: ExecutionStrategy
  nodes: PipelineNodeRequest[]
  edges: PipelineEdgeRequest[]
}

export interface NodeMetadataResponse {
  type: string
  name: string
  category: NodeCategory
  icon: string
  supportedEngines: string[]
  defaultEngine: string
  configSchema: Record<string, unknown>
}

export interface PipelineNodeData {
  nodeKey: string
  nodeType: string
  nodeName: string
  configJson: string
  preferredEngine: string | null
  metadata?: NodeMetadataResponse
}

export interface PipelineNodePatch extends Partial<PipelineNodeData> {
  position?: { x: number; y: number }
}

export type PipelineCanvasNode = Node<PipelineNodeData>
export type PipelineCanvasEdge = Edge
