package com.datacraft.quality.application;

import com.datacraft.api.quality.QualityRuleRequest;
import com.datacraft.quality.domain.QualityRuleType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class QualityRuleRegistry {
    private final Map<QualityRuleType, QualityRuleProvider> providers = new EnumMap<>(QualityRuleType.class);

    public QualityRuleRegistry() {
        register(new BasicProvider(QualityRuleType.NULL_CHECK) {
            @Override public String predicate(QualityRuleRequest request, SqlDialect dialect) {
                return field(request, dialect) + " IS NULL";
            }
        });
        register(new BasicProvider(QualityRuleType.UNIQUE_CHECK) {
            @Override public String predicate(QualityRuleRequest request, SqlDialect dialect) {
                return field(request, dialect) + " IS NOT NULL";
            }
        });
        register(new BasicProvider(QualityRuleType.RANGE_CHECK) {
            @Override public String predicate(QualityRuleRequest request, SqlDialect dialect) {
                return field(request, dialect) + " < ? OR " + field(request, dialect) + " > ?";
            }
            @Override public List<Object> parameters(QualityRuleRequest request) { return List.of(request.min(), request.max()); }
            @Override public void validate(QualityRuleRequest request) {
                super.validate(request);
                if (request.min() == null || request.max() == null || request.min() > request.max()) {
                    throw new QualityValidationException("范围规则必须提供有效的最小值和最大值");
                }
            }
        });
        register(new BasicProvider(QualityRuleType.REGEX_CHECK) {
            @Override public String predicate(QualityRuleRequest request, SqlDialect dialect) {
                return field(request, dialect) + dialect.regexOperator() + "?";
            }
            @Override public List<Object> parameters(QualityRuleRequest request) { return List.of(request.regex()); }
            @Override public void validate(QualityRuleRequest request) {
                super.validate(request);
                if (request.regex() == null || request.regex().isBlank()) throw new QualityValidationException("正则规则不能为空");
            }
        });
        register(new BasicProvider(QualityRuleType.LENGTH_CHECK) {
            @Override public String predicate(QualityRuleRequest request, SqlDialect dialect) {
                return "CHAR_LENGTH(" + field(request, dialect) + ") < ? OR CHAR_LENGTH(" + field(request, dialect) + ") > ?";
            }
            @Override public List<Object> parameters(QualityRuleRequest request) { return List.of(request.minLength(), request.maxLength()); }
            @Override public void validate(QualityRuleRequest request) {
                super.validate(request);
                if (request.minLength() == null || request.maxLength() == null || request.minLength() < 0 || request.minLength() > request.maxLength()) {
                    throw new QualityValidationException("长度规则必须提供有效的最小值和最大值");
                }
            }
        });
        register(new BasicProvider(QualityRuleType.ENUM_CHECK) {
            @Override public String predicate(QualityRuleRequest request, SqlDialect dialect) {
                return field(request, dialect) + " NOT IN (" + "?, ".repeat(Math.max(0, request.values().size() - 1)) + "?)";
            }
            @Override public List<Object> parameters(QualityRuleRequest request) { return List.copyOf(request.values()); }
            @Override public void validate(QualityRuleRequest request) {
                super.validate(request);
                if (request.values().isEmpty()) throw new QualityValidationException("枚举规则值不能为空");
            }
        });
        register(new BasicProvider(QualityRuleType.CUSTOM_SQL_CHECK) {
            @Override public String predicate(QualityRuleRequest request, SqlDialect dialect) { return "(" + request.condition() + ")"; }
            @Override public void validate(QualityRuleRequest request) {
                new SqlSafetyValidator().validate(request.condition());
            }
        });
    }

    private void register(QualityRuleProvider provider) { providers.put(provider.type(), provider); }

    public List<QualityRuleType> types() { return List.of(QualityRuleType.values()); }

    public QualityRuleProvider get(String type) {
        try {
            QualityRuleProvider provider = providers.get(QualityRuleType.valueOf(type));
            if (provider == null) throw new QualityRuleNotFoundException(type);
            return provider;
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new QualityRuleNotFoundException(type);
        }
    }

    private abstract static class BasicProvider implements QualityRuleProvider {
        private final QualityRuleType type;
        private BasicProvider(QualityRuleType type) { this.type = type; }
        @Override public QualityRuleType type() { return type; }
        @Override public void validate(QualityRuleRequest request) {
            if (request == null || request.field() == null || request.field().isBlank()) {
                throw new QualityValidationException("质量规则字段不能为空");
            }
        }
        protected String field(QualityRuleRequest request, SqlDialect dialect) { return dialect.quoteIdentifier(request.field()); }
    }
}
