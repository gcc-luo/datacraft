package io.datacraft.quality.application;

import org.springframework.stereotype.Component;

@Component("postgresqlSqlDialect")
public class PostgresqlSqlDialect implements SqlDialect {
    @Override
    public String quoteIdentifier(String identifier) {
        return "\"" + identifier.replace("\"", "\"\"") + "\"";
    }

    @Override
    public String regexOperator() {
        return " ~ ";
    }
}
