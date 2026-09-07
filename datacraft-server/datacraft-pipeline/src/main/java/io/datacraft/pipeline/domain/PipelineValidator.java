package io.datacraft.pipeline.domain;

import io.datacraft.pipeline.application.PipelineValidationException;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Component
public class PipelineValidator {
    public void validate(String name, List<PipelineNode> nodes, List<PipelineEdge> edges,
                         Set<String> knownNodeTypes) {
        if (name == null || name.isBlank()) {
            throw new PipelineValidationException("Pipeline 名称不能为空");
        }
        if (name.length() > 100) {
            throw new PipelineValidationException("Pipeline 名称长度不能超过 100 个字符");
        }

        List<PipelineNode> safeNodes = nodes == null ? List.of() : nodes;
        List<PipelineEdge> safeEdges = edges == null ? List.of() : edges;
        Set<String> keys = new HashSet<>();
        Set<String> types = knownNodeTypes == null ? Set.of() : knownNodeTypes;
        for (PipelineNode node : safeNodes) {
            if (node == null || node.nodeKey() == null || node.nodeKey().isBlank()) {
                throw new PipelineValidationException("节点 key 不能为空");
            }
            if (!keys.add(node.nodeKey())) {
                throw new PipelineValidationException("节点 key 重复: " + node.nodeKey());
            }
            if (node.nodeType() == null || !types.contains(node.nodeType())) {
                throw new PipelineValidationException("节点类型不存在: " + node.nodeType());
            }
        }

        Set<EdgeIdentity> edgeIdentities = new HashSet<>();
        Map<String, Integer> indegree = new HashMap<>();
        Map<String, Set<String>> adjacency = new HashMap<>();
        for (String key : keys) {
            indegree.put(key, 0);
            adjacency.put(key, new HashSet<>());
        }
        for (PipelineEdge edge : safeEdges) {
            if (edge == null || !keys.contains(edge.sourceNodeKey()) || !keys.contains(edge.targetNodeKey())) {
                throw new PipelineValidationException("边引用了不存在的节点");
            }
            if (Objects.equals(edge.sourceNodeKey(), edge.targetNodeKey())) {
                throw new PipelineValidationException("节点不能连接到自身: " + edge.sourceNodeKey());
            }
            if (!edgeIdentities.add(new EdgeIdentity(edge))) {
                throw new PipelineValidationException("存在重复的边: " + edge.sourceNodeKey() + " -> " + edge.targetNodeKey());
            }
            if (adjacency.get(edge.sourceNodeKey()).add(edge.targetNodeKey())) {
                indegree.computeIfPresent(edge.targetNodeKey(), (ignored, value) -> value + 1);
            }
        }

        ArrayDeque<String> ready = new ArrayDeque<>();
        indegree.forEach((key, value) -> {
            if (value == 0) {
                ready.add(key);
            }
        });
        int visited = 0;
        while (!ready.isEmpty()) {
            String current = ready.remove();
            visited++;
            for (String target : adjacency.getOrDefault(current, Set.of())) {
                int remaining = indegree.computeIfPresent(target, (ignored, value) -> value - 1);
                if (remaining == 0) {
                    ready.add(target);
                }
            }
        }
        if (visited != keys.size()) {
            throw new PipelineValidationException("Pipeline 图不能包含环");
        }
    }

    private record EdgeIdentity(String sourceNodeKey, String targetNodeKey,
                                String sourcePort, String targetPort) {
        private EdgeIdentity(PipelineEdge edge) {
            this(edge.sourceNodeKey(), edge.targetNodeKey(), edge.sourcePort(), edge.targetPort());
        }
    }
}
