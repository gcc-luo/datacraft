package com.datacraft.quality.application;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class SqlSafetyValidator {
    private static final Pattern FORBIDDEN = Pattern.compile(
            "(?i)\\b(SELECT|INSERT|UPDATE|DELETE|DROP|ALTER|CREATE|TRUNCATE|GRANT|REVOKE|MERGE|CALL|EXEC)\\b");

    public void validate(String condition) {
        if (condition == null || condition.isBlank()) {
            throw new QualityValidationException("自定义 SQL 条件不能为空");
        }
        if (condition.contains(";") || condition.contains("--") || condition.contains("/*") || condition.contains("*/")) {
            throw new QualityValidationException("自定义 SQL 条件包含不安全内容");
        }
        if (FORBIDDEN.matcher(condition).find()) {
            throw new QualityValidationException("自定义 SQL 条件只能包含行级条件表达式");
        }
    }
}
