package com.datacraft.pipeline.application;

import com.datacraft.api.pipeline.ExecutionStrategy;
import com.datacraft.api.pipeline.NodeMetadataResponse;
import com.datacraft.api.pipeline.PipelineDetailResponse;
import com.datacraft.api.pipeline.PipelineEdgeRequest;
import com.datacraft.api.pipeline.PipelineEdgeResponse;
import com.datacraft.api.pipeline.PipelineNodeRequest;
import com.datacraft.api.pipeline.PipelineNodeResponse;
import com.datacraft.api.pipeline.PipelineRequest;
import com.datacraft.api.pipeline.PipelineResponse;
import com.datacraft.api.pipeline.PipelineStatus;
import com.datacraft.pipeline.domain.NodeMetadata;
import com.datacraft.pipeline.domain.Pipeline;
import com.datacraft.pipeline.domain.PipelineEdge;
import com.datacraft.pipeline.domain.PipelineNode;
import com.datacraft.pipeline.domain.PipelineRepository;
import com.datacraft.pipeline.domain.PipelineValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class PipelineApplicationService {
    private final PipelineRepository repository;
    private final NodeRegistry nodeRegistry;
    private final PipelineValidator validator;

    public PipelineApplicationService(PipelineRepository repository, NodeRegistry nodeRegistry,
                                      PipelineValidator validator) {
        this.repository = repository;
        this.nodeRegistry = nodeRegistry;
        this.validator = validator;
    }

    public List<PipelineResponse> list() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    public PipelineDetailResponse get(Long id) {
        return toDetail(require(id));
    }

    @Transactional
    public PipelineResponse create(PipelineRequest request) {
        Graph graph = toGraph(request);
        PipelineStatus status = request.status() == null ? PipelineStatus.DRAFT : request.status();
        ExecutionStrategy strategy = request.executionStrategy() == null ? ExecutionStrategy.AUTO : request.executionStrategy();
        validator.validate(request.name(), graph.nodes(), graph.edges(), nodeRegistry.types());
        Instant now = Instant.now();
        Pipeline draft = new Pipeline(null, request.name(), request.description(), status, 1, strategy,
                "{}", null, now, now, graph.nodes(), graph.edges());
        Pipeline saved = repository.save(draft);
        replaceGraph(saved);
        return toResponse(saved);
    }

    @Transactional
    public PipelineResponse update(Long id, PipelineRequest request) {
        Pipeline existing = require(id);
        Graph graph = toGraph(request);
        PipelineStatus status = request.status() == null ? existing.status() : request.status();
        ExecutionStrategy strategy = request.executionStrategy() == null ? existing.executionStrategy() : request.executionStrategy();
        validator.validate(request.name(), graph.nodes(), graph.edges(), nodeRegistry.types());
        Pipeline draft = new Pipeline(id, request.name(), request.description(), status, existing.version() + 1,
                strategy, existing.graphJson(), existing.createdBy(), existing.createdAt(), Instant.now(),
                graph.nodes(), graph.edges());
        Pipeline saved = repository.save(draft);
        replaceGraph(saved);
        return toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        require(id);
        repository.deleteById(id);
    }

    public List<NodeMetadataResponse> listNodeTypes() {
        return nodeRegistry.list().stream().map(this::toResponse).toList();
    }

    public NodeMetadataResponse getNodeType(String type) {
        return toResponse(nodeRegistry.get(type));
    }

    private Pipeline require(Long id) {
        return repository.findById(id).orElseThrow(() -> new PipelineNotFoundException(id));
    }

    private void replaceGraph(Pipeline saved) {
        if (saved.id() == null) {
            throw new IllegalStateException("Pipeline 保存后未返回 ID");
        }
        repository.replaceGraph(saved.id(), saved.nodes(), saved.edges());
    }

    private Graph toGraph(PipelineRequest request) {
        List<PipelineNode> nodes = request.nodes() == null ? List.of() : request.nodes().stream().map(this::toDomain).toList();
        List<PipelineEdge> edges = request.edges() == null ? List.of() : request.edges().stream().map(this::toDomain).toList();
        return new Graph(nodes, edges);
    }

    private PipelineNode toDomain(PipelineNodeRequest request) {
        String key = request.nodeKey();
        return new PipelineNode(null, key, request.nodeType(),
                request.nodeName() == null || request.nodeName().isBlank() ? key : request.nodeName(),
                request.x() == null ? 0D : request.x(), request.y() == null ? 0D : request.y(),
                request.configJson() == null || request.configJson().isBlank() ? "{}" : request.configJson(),
                request.preferredEngine());
    }

    private PipelineEdge toDomain(PipelineEdgeRequest request) {
        return new PipelineEdge(null, request.sourceNodeKey(), request.targetNodeKey(), request.sourcePort(),
                request.targetPort(), request.conditionJson());
    }

    private PipelineResponse toResponse(Pipeline pipeline) {
        return new PipelineResponse(pipeline.id(), pipeline.name(), pipeline.description(), pipeline.status(),
                pipeline.version(), pipeline.executionStrategy(), pipeline.createdAt(), pipeline.updatedAt());
    }

    private PipelineDetailResponse toDetail(Pipeline pipeline) {
        return new PipelineDetailResponse(pipeline.id(), pipeline.name(), pipeline.description(), pipeline.status(),
                pipeline.version(), pipeline.executionStrategy(), pipeline.createdAt(), pipeline.updatedAt(),
                pipeline.nodes().stream().map(node -> new PipelineNodeResponse(node.id(), node.nodeKey(), node.nodeType(),
                        node.nodeName(), node.x(), node.y(), node.configJson(), node.preferredEngine())).toList(),
                pipeline.edges().stream().map(edge -> new PipelineEdgeResponse(edge.id(), edge.sourceNodeKey(),
                        edge.targetNodeKey(), edge.sourcePort(), edge.targetPort(), edge.conditionJson())).toList());
    }

    private NodeMetadataResponse toResponse(NodeMetadata metadata) {
        return new NodeMetadataResponse(metadata.type(), metadata.name(), metadata.category(), metadata.icon(),
                metadata.supportedEngines(), metadata.defaultEngine(), metadata.configSchema());
    }

    private record Graph(List<PipelineNode> nodes, List<PipelineEdge> edges) {
    }
}
