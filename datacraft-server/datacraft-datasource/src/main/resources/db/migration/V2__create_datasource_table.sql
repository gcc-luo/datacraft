CREATE TABLE IF NOT EXISTS dc_datasource (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    type VARCHAR(32) NOT NULL,
    host VARCHAR(255) NOT NULL,
    port INTEGER NOT NULL,
    database_name VARCHAR(255) NOT NULL,
    username VARCHAR(255) NOT NULL,
    password_ciphertext TEXT NOT NULL,
    remark VARCHAR(500),
    status VARCHAR(32) NOT NULL DEFAULT 'UNKNOWN',
    last_tested_at TIMESTAMPTZ,
    last_test_latency_ms BIGINT,
    last_test_message VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_dc_datasource_type CHECK (type IN ('POSTGRESQL', 'MYSQL')),
    CONSTRAINT ck_dc_datasource_status CHECK (status IN ('UNKNOWN', 'SUCCESS', 'FAILED')),
    CONSTRAINT ck_dc_datasource_port CHECK (port BETWEEN 1 AND 65535)
);

CREATE INDEX IF NOT EXISTS idx_dc_datasource_type ON dc_datasource(type);
CREATE INDEX IF NOT EXISTS idx_dc_datasource_status ON dc_datasource(status);
