package com.datacraft.quality.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.Instant;

@TableName("dc_quality_result")
public class QualityResultEntity {
    @TableId(type = IdType.AUTO) private Long id;
    private Long executionId;
    private Long nodeExecutionId;
    private String ruleType;
    private Long datasourceId;
    private String tableName;
    private String fieldName;
    private Long totalRows;
    private Long errorRows;
    private Long passRows;
    private BigDecimal passRate;
    private String status;
    private Instant createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getExecutionId() { return executionId; }
    public void setExecutionId(Long executionId) { this.executionId = executionId; }
    public Long getNodeExecutionId() { return nodeExecutionId; }
    public void setNodeExecutionId(Long nodeExecutionId) { this.nodeExecutionId = nodeExecutionId; }
    public String getRuleType() { return ruleType; }
    public void setRuleType(String ruleType) { this.ruleType = ruleType; }
    public Long getDatasourceId() { return datasourceId; }
    public void setDatasourceId(Long datasourceId) { this.datasourceId = datasourceId; }
    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }
    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }
    public Long getTotalRows() { return totalRows; }
    public void setTotalRows(Long totalRows) { this.totalRows = totalRows; }
    public Long getErrorRows() { return errorRows; }
    public void setErrorRows(Long errorRows) { this.errorRows = errorRows; }
    public Long getPassRows() { return passRows; }
    public void setPassRows(Long passRows) { this.passRows = passRows; }
    public BigDecimal getPassRate() { return passRate; }
    public void setPassRate(BigDecimal passRate) { this.passRate = passRate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
