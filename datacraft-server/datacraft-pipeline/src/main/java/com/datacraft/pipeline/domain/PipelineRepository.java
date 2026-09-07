package com.datacraft.pipeline.domain;

import java.util.List;
import java.util.Optional;

public interface PipelineRepository {
    List<Pipeline> findAll();

    Optional<Pipeline> findById(Long id);

    Pipeline save(Pipeline pipeline);

    void replaceGraph(Long pipelineId, List<PipelineNode> nodes, List<PipelineEdge> edges);

    void deleteById(Long id);
}
