package com.datacraft.quality.application;

public class QualityResultNotFoundException extends RuntimeException {
    public QualityResultNotFoundException(Long id) {
        super("质量结果不存在: " + id);
    }
}
