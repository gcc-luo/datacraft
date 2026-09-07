package com.datacraft.quality.application;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SqlSafetyValidatorTest {
    private final SqlSafetyValidator validator = new SqlSafetyValidator();

    @Test
    void acceptsAConditionExpression() {
        assertDoesNotThrow(() -> validator.validate("age >= 0 AND status = 'ACTIVE'"));
    }

    @Test
    void rejectsStatementsCommentsAndDml() {
        assertThrows(QualityValidationException.class, () -> validator.validate("age > 0; DELETE FROM customer"));
        assertThrows(QualityValidationException.class, () -> validator.validate("age > 0 /* bypass */"));
        assertThrows(QualityValidationException.class, () -> validator.validate("SELECT * FROM customer"));
    }
}
