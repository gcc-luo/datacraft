package com.datacraft.execution.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.datacraft.api.pipeline.ExecutionStrategy;
import com.datacraft.api.pipeline.NodeCategory;
import com.datacraft.execution.domain.DataReference;
import com.datacraft.execution.domain.DataReferenceType;
import com.datacraft.execution.domain.ExecutionPlan;
import com.datacraft.execution.domain.ExecutionStagePlan;
import com.datacraft.execution.domain.ExecutionEngine;
import com.datacraft.pipeline.application.NodeRegistry;
import com.datacraft.pipeline.domain.NodeMetadata;
import com.datacraft.pipeline.domain.Pipeline;
import com.datacraft.pipeline.domain.PipelineEdge;
import com.datacraft.pipeline.domain.PipelineNode;
import com.datacraft.pipeline.domain.PipelineValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeSet;

@Service
public class ExecutionPlanner {
    private final PipelineValidator pipelineValidator;
    private final NodeRegistry nodeRegistry;
    private final EngineRegistry engineRegistry;
    private final ObjectMapper objectMapper;

    @Autowired
    public ExecutionPlanner(PipelineValidator pipelineValidator, NodeRegistry nodeRegistry,
                            EngineRegistry engineRegistry, ObjectMapper objectMapper) {
        this.pipelineValidator = pipelineValidator;
        this.nodeRegistry = nodeRegistry;
        this.engineRegistry = engineRegistry;
        this.objectMapper = objectMapper;
    }

    public ExecutionPlanner(PipelineValidator pipelineValidator, NodeRegistry nodeRegistry,
                            EngineRegistry engineRegistry) {
        this(pipelineValidator, nodeRegistry, engineRegistry, new ObjectMapper());
    }

    public ExecutionPlan plan(Pipeline pipeline) {
        if (pipeline == null || pipeline.id() == null) {
            throw new ExecutionPlanningException("Pipeline 及其 ID 不能为空");
        }
        try {
            pipelineValidator.validate(pipeline.name(), pipeline.nodes(), pipeline.edges(), nodeRegistry.types());
        } catch (RuntimeException exception) {
            throw new ExecutionPlanningException("Pipeline 校验失败: " + exception.getMessage(), exception);
        }

        List<PipelineNode> orderedNodes = topologicalOrder(pipeline.nodes(), pipeline.edges());
        List<ResolvedNode> resolvedNodes = orderedNodes.stream()
                .map(node -> resolve(node, pipeline.executionStrategy()))
                .toList();
        List<StageBuilder> builders = new ArrayList<>();
        for (ResolvedNode resolved : resolvedNodes) {
            StageBuilder current = builders.isEmpty() ? null : builders.get(builders.size() - 1);
            if (current == null || !current.engineCode.equals(resolved.engineCode)) {
                current = new StageBuilder(resolved.engineCode);
                builders.add(current);
            }
            current.nodes.add(resolved);
        }

        List<ExecutionStagePlan> stages = builders.stream()
                .map(builder -> toStagePlan(pipeline.id(), builders, builder))
                .toList();
        return new ExecutionPlan(pipeline.id(), stages, Map.of("pipelineVersion", pipeline.version()));
    }

    private ResolvedNode resolve(PipelineNode node, ExecutionStrategy strategy) {
        NodeMetadata metadata = nodeRegistry.get(node.nodeType());
        String engineCode = selectEngine(node, strategy, metadata);
        if (!metadata.supportedEngines().contains(engineCode)) {
            throw new ExecutionPlanningException("节点 " + node.nodeKey() + " 不支持引擎: " + engineCode);
        }
        try {
            engineRegistry.get(engineCode);
        } catch (EngineNotFoundException exception) {
            throw new ExecutionPlanningException("引擎未注册: " + engineCode, exception);
        }
        return new ResolvedNode(node, metadata, engineCode);
    }

    private String selectEngine(PipelineNode node, ExecutionStrategy strategy, NodeMetadata metadata) {
        if (node.preferredEngine() != null && !node.preferredEngine().isBlank()) {
            return normalizeEngine(node.preferredEngine());
        }
        if (strategy != null && strategy != ExecutionStrategy.AUTO) {
            return strategy.name();
        }
        return normalizeEngine(metadata.defaultEngine());
    }

    private ExecutionStagePlan toStagePlan(Long pipelineId, List<StageBuilder> allStages, StageBuilder builder) {
        int stageIndex = allStages.indexOf(builder);
        List<DataReference> inputs = new ArrayList<>();
        List<DataReference> outputs = new ArrayList<>();
        builder.nodes.stream().filter(node -> node.metadata.category() == NodeCategory.SOURCE)
                .map(node -> databaseReference(node.node))
                .forEach(inputs::add);
        builder.nodes.stream().filter(node -> node.metadata.category() == NodeCategory.SINK)
                .map(node -> databaseReference(node.node))
                .forEach(outputs::add);

        if (stageIndex > 0) {
            inputs.add(stageHandoff(pipelineId, stageIndex - 1));
        }
        if (stageIndex < allStages.size() - 1) {
            outputs.add(stageHandoff(pipelineId, stageIndex));
        }
        return new ExecutionStagePlan(stageIndex, builder.engineCode,
                builder.nodes.stream().map(node -> node.node.nodeKey()).toList(), inputs, outputs);
    }

    private DataReference databaseReference(PipelineNode node) {
        JsonNode config = parseConfig(node);
        JsonNode datasourceId = config.get("datasourceId");
        JsonNode tableName = config.get("tableName");
        if (datasourceId == null || datasourceId.isNull() || datasourceId.asText().isBlank()
                || tableName == null || tableName.isNull() || tableName.asText().isBlank()) {
            throw new ExecutionPlanningException("节点 " + node.nodeKey() + " 缺少 datasourceId 或 tableName");
        }
        String datasource = datasourceId.asText();
        String table = tableName.asText();
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("datasourceId", datasourceId.isNumber() ? datasourceId.numberValue() : datasource);
        metadata.put("tableName", table);
        if (config.hasNonNull("writeMode") && !config.get("writeMode").asText().isBlank()) {
            metadata.put("writeMode", config.get("writeMode").asText());
        }
        return new DataReference(DataReferenceType.DATABASE_TABLE,
                "datacraft://datasource/" + datasource + "/table/" + table, metadata);
    }

    private JsonNode parseConfig(PipelineNode node) {
        try {
            JsonNode config = objectMapper.readTree(node.configJson() == null ? "{}" : node.configJson());
            if (config == null || !config.isObject()) {
                throw new ExecutionPlanningException("节点 " + node.nodeKey() + " 配置必须是 JSON 对象");
            }
            return config;
        } catch (JsonProcessingException exception) {
            throw new ExecutionPlanningException("节点 " + node.nodeKey() + " 配置不是合法 JSON", exception);
        }
    }

    private DataReference stageHandoff(Long pipelineId, int sourceStageIndex) {
        return new DataReference(DataReferenceType.DATABASE_QUERY,
                "datacraft://pipeline/" + pipelineId + "/stage/" + sourceStageIndex + "/output",
                Map.of("handoff", "MATERIALIZATION_REQUIRED", "sourceStage", sourceStageIndex));
    }

    private List<PipelineNode> topologicalOrder(List<PipelineNode> nodes, List<PipelineEdge> edges) {
        Map<String, PipelineNode> byKey = new HashMap<>();
        Map<String, Integer> indegree = new HashMap<>();
        Map<String, Set<String>> adjacency = new HashMap<>();
        for (PipelineNode node : nodes == null ? List.<PipelineNode>of() : nodes) {
            byKey.put(node.nodeKey(), node);
            indegree.put(node.nodeKey(), 0);
            adjacency.put(node.nodeKey(), new TreeSet<>());
        }
        for (PipelineEdge edge : edges == null ? List.<PipelineEdge>of() : edges) {
            if (adjacency.get(edge.sourceNodeKey()).add(edge.targetNodeKey())) {
                indegree.computeIfPresent(edge.targetNodeKey(), (ignored, value) -> value + 1);
            }
        }
        PriorityQueue<String> ready = new PriorityQueue<>();
        indegree.forEach((key, value) -> {
            if (value == 0) {
                ready.add(key);
            }
        });
        List<PipelineNode> ordered = new ArrayList<>();
        while (!ready.isEmpty()) {
            String current = ready.remove();
            ordered.add(byKey.get(current));
            for (String target : adjacency.getOrDefault(current, Set.of())) {
                int remaining = indegree.computeIfPresent(target, (ignored, value) -> value - 1);
                if (remaining == 0) {
                    ready.add(target);
                }
            }
        }
        return ordered;
    }

    private String normalizeEngine(String engine) {
        return engine == null ? null : engine.trim().toUpperCase(Locale.ROOT);
    }

    private record ResolvedNode(PipelineNode node, NodeMetadata metadata, String engineCode) {
    }

    private static class StageBuilder {
        private final String engineCode;
        private final List<ResolvedNode> nodes = new ArrayList<>();

        private StageBuilder(String engineCode) {
            this.engineCode = engineCode;
        }
    }

}
