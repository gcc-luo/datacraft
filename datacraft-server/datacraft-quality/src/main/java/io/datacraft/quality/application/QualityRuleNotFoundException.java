package io.datacraft.quality.application;

public class QualityRuleNotFoundException extends RuntimeException {
    public QualityRuleNotFoundException(String type) {
        super("质量规则不存在: " + type);
    }
}
