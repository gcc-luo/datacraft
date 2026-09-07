package com.datacraft.metadata.domain;

public record DatasetField(
        Long id,
        String fieldName,
        int ordinalPosition,
        String dataType,
        boolean nullable,
        boolean primaryKey,
        String fieldRemark
) {
    public DatasetField(String fieldName, int ordinalPosition, String dataType, boolean nullable,
                        boolean primaryKey, String fieldRemark) {
        this(null, fieldName, ordinalPosition, dataType, nullable, primaryKey, fieldRemark);
    }
}
