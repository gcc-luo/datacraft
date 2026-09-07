package io.datacraft.datasource.application;

import io.datacraft.datasource.domain.Datasource;
import org.springframework.stereotype.Component;

@Component
public class JdbcUrlBuilder {
    public String build(Datasource datasource) {
        String host = datasource.host().trim();
        String database = datasource.databaseName().trim();
        return switch (datasource.type()) {
            case POSTGRESQL -> "jdbc:postgresql://%s:%d/%s?connectTimeout=5".formatted(host, datasource.port(), database);
            case MYSQL -> "jdbc:mysql://%s:%d/%s?connectTimeout=5000&socketTimeout=5000&useSSL=false"
                    .formatted(host, datasource.port(), database);
        };
    }
}
