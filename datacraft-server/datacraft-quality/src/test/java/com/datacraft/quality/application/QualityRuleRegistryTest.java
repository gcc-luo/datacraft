package com.datacraft.quality.application;

import com.datacraft.api.quality.QualityRuleRequest;
import com.datacraft.quality.domain.QualityRuleType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class QualityRuleRegistryTest {
    private final QualityRuleRegistry registry = new QualityRuleRegistry();

    @Test
    void exposesAllDesignQualityRules() {
        assertEquals(7, registry.types().size());
        for (QualityRuleType type : QualityRuleType.values()) {
            assertEquals(type, registry.get(type.name()).type());
        }
    }

    @Test
    void buildsDialectSpecificRegexPredicate() {
        QualityRuleRequest request = new QualityRuleRequest("REGEX_CHECK", "phone", null, null,
                "^1[0-9]{10}$", null, null, null, null);

        assertEquals("\"phone\" ~ ?", registry.get("REGEX_CHECK").predicate(request,
                new PostgresqlSqlDialect()));
        assertEquals("`phone` REGEXP ?", registry.get("REGEX_CHECK").predicate(request,
                new MysqlSqlDialect()));
    }

    @Test
    void rejectsUnknownRule() {
        assertThrows(QualityRuleNotFoundException.class, () -> registry.get("NOPE"));
    }
}
