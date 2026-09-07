package com.datacraft.metadata.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.Instant;

@TableName("dc_dataset")
public class DatasetEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long datasourceId;
    private String catalogName;
    private String schemaName;
    private String tableName;
    private String tableRemark;
    private Long estimatedRowCount;
    private Instant collectedAt;
    private Instant createdAt;
    private Instant updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getDatasourceId() { return datasourceId; }
    public void setDatasourceId(Long datasourceId) { this.datasourceId = datasourceId; }
    public String getCatalogName() { return catalogName; }
    public void setCatalogName(String catalogName) { this.catalogName = catalogName; }
    public String getSchemaName() { return schemaName; }
    public void setSchemaName(String schemaName) { this.schemaName = schemaName; }
    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }
    public String getTableRemark() { return tableRemark; }
    public void setTableRemark(String tableRemark) { this.tableRemark = tableRemark; }
    public Long getEstimatedRowCount() { return estimatedRowCount; }
    public void setEstimatedRowCount(Long estimatedRowCount) { this.estimatedRowCount = estimatedRowCount; }
    public Instant getCollectedAt() { return collectedAt; }
    public void setCollectedAt(Instant collectedAt) { this.collectedAt = collectedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
