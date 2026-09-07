package io.datacraft.metadata.application;

public class DatasetNotFoundException extends RuntimeException {
    public DatasetNotFoundException(Long id) {
        super("数据集不存在: " + id);
    }
}
