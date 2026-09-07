package com.datacraft.datasource.application;

public class DatasourceDuplicateException extends RuntimeException {
    public DatasourceDuplicateException(String name) {
        super("数据源名称已存在: " + name);
    }
}
