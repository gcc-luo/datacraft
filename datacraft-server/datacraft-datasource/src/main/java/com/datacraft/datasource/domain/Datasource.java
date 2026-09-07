package com.datacraft.datasource.domain;

import com.datacraft.api.datasource.DatasourceStatus;
import com.datacraft.api.datasource.DatasourceType;

import java.time.Instant;

public record Datasource(
        Long id,
        String name,
        DatasourceType type,
        String host,
        Integer port,
        String databaseName,
        String username,
        String passwordCiphertext,
        String remark,
        DatasourceStatus status,
        Instant lastTestedAt,
        Long lastTestLatencyMs,
        String lastTestMessage,
        Instant createdAt,
        Instant updatedAt
) {
    public Datasource withPasswordCiphertext(String value) {
        return new Datasource(id, name, type, host, port, databaseName, username, value, remark,
                status, lastTestedAt, lastTestLatencyMs, lastTestMessage, createdAt, updatedAt);
    }

    public Datasource withTestResult(DatasourceStatus resultStatus, Instant testedAt, Long latencyMs, String message) {
        return new Datasource(id, name, type, host, port, databaseName, username, passwordCiphertext, remark,
                resultStatus, testedAt, latencyMs, message, createdAt, updatedAt);
    }
}
