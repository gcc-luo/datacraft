package io.datacraft.quality.application;

import org.springframework.stereotype.Component;

@Component("mysqlSqlDialect")
public class MysqlSqlDialect implements SqlDialect {
    @Override
    public String quoteIdentifier(String identifier) {
        return "`" + identifier.replace("`", "``") + "`";
    }

    @Override
    public String regexOperator() {
        return " REGEXP ";
    }
}
