package io.datacraft.datasource.domain;

public record ConnectionTestOutcome(boolean success, long latencyMs) {
    public static ConnectionTestOutcome success(long latencyMs) {
        return new ConnectionTestOutcome(true, latencyMs);
    }

    public static ConnectionTestOutcome failure(long latencyMs) {
        return new ConnectionTestOutcome(false, latencyMs);
    }
}
