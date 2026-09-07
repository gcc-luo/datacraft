import type { Edge, Node } from '@vue-flow/core'
import type {
  PipelineCanvasEdge,
  PipelineCanvasNode,
  PipelineDetailResponse,
  PipelineEdgeRequest,
  PipelineNodeRequest,
  PipelineRequest,
  PipelineStatus,
  ExecutionStrategy,
} from '../types/pipeline'

export interface PipelineCanvasGraph {
  nodes: PipelineCanvasNode[]
  edges: PipelineCanvasEdge[]
}

export function detailToCanvas(detail: PipelineDetailResponse): PipelineCanvasGraph {
  const nodes = detail.nodes.map((node) => ({
    id: node.nodeKey,
    type: 'pipeline',
    position: { x: node.x, y: node.y },
    data: {
      nodeKey: node.nodeKey,
      nodeType: node.nodeType,
      nodeName: node.nodeName,
      configJson: node.configJson,
      preferredEngine: node.preferredEngine,
    },
  }))
  const edges = detail.edges.map((edge, index) => ({
    id: `e-${edge.sourceNodeKey}-${edge.targetNodeKey}-${index}`,
    source: edge.sourceNodeKey,
    target: edge.targetNodeKey,
    sourceHandle: edge.sourcePort,
    targetHandle: edge.targetPort,
    type: 'smoothstep',
    animated: true,
  }))
  return { nodes, edges }
}

export function canvasToRequest(name: string, description: string | null, status: PipelineStatus,
                                executionStrategy: ExecutionStrategy, nodes: PipelineCanvasNode[], edges: Edge[]): PipelineRequest {
  const nodeRequests: PipelineNodeRequest[] = nodes.map((node) => ({
    nodeKey: node.data.nodeKey,
    nodeType: node.data.nodeType,
    nodeName: node.data.nodeName,
    x: node.position.x,
    y: node.position.y,
    configJson: node.data.configJson,
    preferredEngine: node.data.preferredEngine,
  }))
  const edgeRequests: PipelineEdgeRequest[] = edges.map((edge) => ({
    sourceNodeKey: edge.source,
    targetNodeKey: edge.target,
    sourcePort: edge.sourceHandle || null,
    targetPort: edge.targetHandle || null,
    conditionJson: null,
  }))
  return { name, description, status, executionStrategy, nodes: nodeRequests, edges: edgeRequests }
}

export function nodeFromMetadata(type: string, metadata: { name: string; defaultEngine: string; supportedEngines: string[] }, position: { x: number; y: number }, nodeKey: string): PipelineCanvasNode {
  return {
    id: nodeKey,
    type: 'pipeline',
    position,
    data: {
      nodeKey,
      nodeType: type,
      nodeName: metadata.name,
      configJson: '{}',
      preferredEngine: metadata.defaultEngine || metadata.supportedEngines[0] || null,
    },
  }
}
