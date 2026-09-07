package io.datacraft.pipeline.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.Instant;

@TableName("dc_pipeline_edge")
public class PipelineEdgeEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long pipelineId;
    private String sourceNodeKey;
    private String targetNodeKey;
    private String sourcePort;
    private String targetPort;
    private String conditionJson;
    private Instant createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPipelineId() { return pipelineId; }
    public void setPipelineId(Long pipelineId) { this.pipelineId = pipelineId; }
    public String getSourceNodeKey() { return sourceNodeKey; }
    public void setSourceNodeKey(String sourceNodeKey) { this.sourceNodeKey = sourceNodeKey; }
    public String getTargetNodeKey() { return targetNodeKey; }
    public void setTargetNodeKey(String targetNodeKey) { this.targetNodeKey = targetNodeKey; }
    public String getSourcePort() { return sourcePort; }
    public void setSourcePort(String sourcePort) { this.sourcePort = sourcePort; }
    public String getTargetPort() { return targetPort; }
    public void setTargetPort(String targetPort) { this.targetPort = targetPort; }
    public String getConditionJson() { return conditionJson; }
    public void setConditionJson(String conditionJson) { this.conditionJson = conditionJson; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
