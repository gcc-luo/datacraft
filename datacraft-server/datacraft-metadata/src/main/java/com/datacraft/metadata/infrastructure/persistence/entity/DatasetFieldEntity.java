package com.datacraft.metadata.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.Instant;

@TableName("dc_dataset_field")
public class DatasetFieldEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long datasetId;
    private String fieldName;
    private Integer ordinalPosition;
    private String dataType;
    private Boolean nullable;
    private Boolean primaryKey;
    private String fieldRemark;
    private Instant createdAt;
    private Instant updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getDatasetId() { return datasetId; }
    public void setDatasetId(Long datasetId) { this.datasetId = datasetId; }
    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }
    public Integer getOrdinalPosition() { return ordinalPosition; }
    public void setOrdinalPosition(Integer ordinalPosition) { this.ordinalPosition = ordinalPosition; }
    public String getDataType() { return dataType; }
    public void setDataType(String dataType) { this.dataType = dataType; }
    public Boolean getNullable() { return nullable; }
    public void setNullable(Boolean nullable) { this.nullable = nullable; }
    public Boolean getPrimaryKey() { return primaryKey; }
    public void setPrimaryKey(Boolean primaryKey) { this.primaryKey = primaryKey; }
    public String getFieldRemark() { return fieldRemark; }
    public void setFieldRemark(String fieldRemark) { this.fieldRemark = fieldRemark; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
