package com.datacraft.api.metadata;

public record DatasetFieldResponse(Long id, String fieldName, int ordinalPosition, String dataType,
                                   boolean nullable, boolean primaryKey, String fieldRemark) {
}
