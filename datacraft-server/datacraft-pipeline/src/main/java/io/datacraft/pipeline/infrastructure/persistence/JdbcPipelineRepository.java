package io.datacraft.pipeline.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.datacraft.api.pipeline.ExecutionStrategy;
import io.datacraft.api.pipeline.PipelineStatus;
import io.datacraft.pipeline.domain.Pipeline;
import io.datacraft.pipeline.domain.PipelineEdge;
import io.datacraft.pipeline.domain.PipelineNode;
import io.datacraft.pipeline.domain.PipelineRepository;
import io.datacraft.pipeline.infrastructure.persistence.entity.PipelineEdgeEntity;
import io.datacraft.pipeline.infrastructure.persistence.entity.PipelineEntity;
import io.datacraft.pipeline.infrastructure.persistence.entity.PipelineNodeEntity;
import io.datacraft.pipeline.infrastructure.persistence.mapper.PipelineEdgeMapper;
import io.datacraft.pipeline.infrastructure.persistence.mapper.PipelineMapper;
import io.datacraft.pipeline.infrastructure.persistence.mapper.PipelineNodeMapper;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class JdbcPipelineRepository implements PipelineRepository {
    private final PipelineMapper pipelineMapper;
    private final PipelineNodeMapper nodeMapper;
    private final PipelineEdgeMapper edgeMapper;
    private final ObjectMapper objectMapper;

    public JdbcPipelineRepository(PipelineMapper pipelineMapper, PipelineNodeMapper nodeMapper,
                                  PipelineEdgeMapper edgeMapper, ObjectMapper objectMapper) {
        this.pipelineMapper = pipelineMapper;
        this.nodeMapper = nodeMapper;
        this.edgeMapper = edgeMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<Pipeline> findAll() {
        QueryWrapper<PipelineEntity> query = new QueryWrapper<>();
        query.orderByDesc("created_at").orderByAsc("id");
        return pipelineMapper.selectList(query).stream().map(entity -> toDomain(entity, List.of(), List.of())).toList();
    }

    @Override
    public Optional<Pipeline> findById(Long id) {
        PipelineEntity entity = pipelineMapper.selectById(id);
        if (entity == null) {
            return Optional.empty();
        }
        QueryWrapper<PipelineNodeEntity> nodeQuery = new QueryWrapper<PipelineNodeEntity>()
                .eq("pipeline_id", id).orderByAsc("id");
        QueryWrapper<PipelineEdgeEntity> edgeQuery = new QueryWrapper<PipelineEdgeEntity>()
                .eq("pipeline_id", id).orderByAsc("id");
        List<PipelineNode> nodes = nodeMapper.selectList(nodeQuery).stream().map(this::toDomain).toList();
        List<PipelineEdge> edges = edgeMapper.selectList(edgeQuery).stream().map(this::toDomain).toList();
        return Optional.of(toDomain(entity, nodes, edges));
    }

    @Override
    public Pipeline save(Pipeline pipeline) {
        Instant now = Instant.now();
        PipelineEntity entity = new PipelineEntity();
        entity.setId(pipeline.id());
        entity.setName(pipeline.name());
        entity.setDescription(pipeline.description());
        entity.setStatus(pipeline.status().name());
        entity.setVersion(pipeline.version());
        entity.setExecutionStrategy(pipeline.executionStrategy().name());
        entity.setGraphJson(serializeGraph(pipeline.nodes(), pipeline.edges()));
        entity.setCreatedBy(pipeline.createdBy());
        entity.setCreatedAt(pipeline.createdAt() == null ? now : pipeline.createdAt());
        entity.setUpdatedAt(pipeline.updatedAt() == null ? now : pipeline.updatedAt());
        if (entity.getId() == null) {
            pipelineMapper.insert(entity);
        } else {
            pipelineMapper.updateById(entity);
        }
        return toDomain(entity, pipeline.nodes(), pipeline.edges());
    }

    @Override
    public void replaceGraph(Long pipelineId, List<PipelineNode> nodes, List<PipelineEdge> edges) {
        nodeMapper.delete(new QueryWrapper<PipelineNodeEntity>().eq("pipeline_id", pipelineId));
        edgeMapper.delete(new QueryWrapper<PipelineEdgeEntity>().eq("pipeline_id", pipelineId));
        Instant now = Instant.now();
        for (PipelineNode node : nodes) {
            PipelineNodeEntity entity = new PipelineNodeEntity();
            entity.setPipelineId(pipelineId);
            entity.setNodeKey(node.nodeKey());
            entity.setNodeType(node.nodeType());
            entity.setNodeName(node.nodeName());
            entity.setX(node.x());
            entity.setY(node.y());
            entity.setConfigJson(node.configJson());
            entity.setPreferredEngine(node.preferredEngine());
            entity.setCreatedAt(now);
            entity.setUpdatedAt(now);
            nodeMapper.insert(entity);
        }
        for (PipelineEdge edge : edges) {
            PipelineEdgeEntity entity = new PipelineEdgeEntity();
            entity.setPipelineId(pipelineId);
            entity.setSourceNodeKey(edge.sourceNodeKey());
            entity.setTargetNodeKey(edge.targetNodeKey());
            entity.setSourcePort(edge.sourcePort());
            entity.setTargetPort(edge.targetPort());
            entity.setConditionJson(edge.conditionJson());
            entity.setCreatedAt(now);
            edgeMapper.insert(entity);
        }
    }

    @Override
    public void deleteById(Long id) {
        pipelineMapper.deleteById(id);
    }

    private Pipeline toDomain(PipelineEntity entity, List<PipelineNode> nodes, List<PipelineEdge> edges) {
        return new Pipeline(entity.getId(), entity.getName(), entity.getDescription(),
                PipelineStatus.valueOf(entity.getStatus()), entity.getVersion(),
                ExecutionStrategy.valueOf(entity.getExecutionStrategy()), entity.getGraphJson(), entity.getCreatedBy(),
                entity.getCreatedAt(), entity.getUpdatedAt(), List.copyOf(nodes), List.copyOf(edges));
    }

    private PipelineNode toDomain(PipelineNodeEntity entity) {
        return new PipelineNode(entity.getId(), entity.getNodeKey(), entity.getNodeType(), entity.getNodeName(),
                entity.getX(), entity.getY(), entity.getConfigJson(), entity.getPreferredEngine());
    }

    private PipelineEdge toDomain(PipelineEdgeEntity entity) {
        return new PipelineEdge(entity.getId(), entity.getSourceNodeKey(), entity.getTargetNodeKey(),
                entity.getSourcePort(), entity.getTargetPort(), entity.getConditionJson());
    }

    private String serializeGraph(List<PipelineNode> nodes, List<PipelineEdge> edges) {
        Map<String, Object> graph = new LinkedHashMap<>();
        graph.put("nodes", nodes == null ? List.of() : nodes);
        graph.put("edges", edges == null ? List.of() : edges);
        try {
            return objectMapper.writeValueAsString(graph);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Pipeline 图快照序列化失败", exception);
        }
    }
}
