CREATE TABLE IF NOT EXISTS dc_quality_result (
    id BIGSERIAL PRIMARY KEY,
    execution_id BIGINT,
    node_execution_id BIGINT,
    rule_type VARCHAR(64) NOT NULL,
    datasource_id BIGINT NOT NULL,
    table_name VARCHAR(256) NOT NULL,
    field_name VARCHAR(256),
    total_rows BIGINT NOT NULL,
    error_rows BIGINT NOT NULL,
    pass_rows BIGINT NOT NULL,
    pass_rate NUMERIC(8, 4) NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS dc_quality_sample (
    id BIGSERIAL PRIMARY KEY,
    quality_result_id BIGINT NOT NULL REFERENCES dc_quality_result(id) ON DELETE CASCADE,
    sample_index INTEGER NOT NULL,
    data_json JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_dc_quality_result_execution
    ON dc_quality_result (execution_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_dc_quality_result_datasource_table
    ON dc_quality_result (datasource_id, table_name, created_at DESC);
