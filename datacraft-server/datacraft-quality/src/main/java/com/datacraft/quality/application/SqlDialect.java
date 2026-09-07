package com.datacraft.quality.application;

public interface SqlDialect {
    String quoteIdentifier(String identifier);

    default String quoteTable(String tableName) {
        String[] parts = tableName.split("\\.");
        return java.util.Arrays.stream(parts).map(this::quoteIdentifier).collect(java.util.stream.Collectors.joining("."));
    }

    String regexOperator();
}
