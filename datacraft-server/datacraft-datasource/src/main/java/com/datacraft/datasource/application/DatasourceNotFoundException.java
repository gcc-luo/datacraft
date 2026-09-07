package com.datacraft.datasource.application;

public class DatasourceNotFoundException extends RuntimeException {
    public DatasourceNotFoundException(Long id) {
        super("数据源不存在: " + id);
    }
}
