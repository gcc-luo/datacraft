package com.datacraft.quality.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.Instant;

@TableName("dc_quality_sample")
public class QualitySampleEntity {
    @TableId(type = IdType.AUTO) private Long id;
    private Long qualityResultId;
    private Integer sampleIndex;
    private String dataJson;
    private Instant createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getQualityResultId() { return qualityResultId; }
    public void setQualityResultId(Long qualityResultId) { this.qualityResultId = qualityResultId; }
    public Integer getSampleIndex() { return sampleIndex; }
    public void setSampleIndex(Integer sampleIndex) { this.sampleIndex = sampleIndex; }
    public String getDataJson() { return dataJson; }
    public void setDataJson(String dataJson) { this.dataJson = dataJson; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
