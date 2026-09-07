CREATE TABLE IF NOT EXISTS dc_pipeline (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(1000),
    status VARCHAR(16) NOT NULL DEFAULT 'DRAFT',
    version INTEGER NOT NULL DEFAULT 1,
    execution_strategy VARCHAR(32) NOT NULL DEFAULT 'AUTO',
    graph_json TEXT NOT NULL DEFAULT '{}',
    created_by BIGINT REFERENCES dc_user(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_dc_pipeline_status CHECK (status IN ('DRAFT', 'ACTIVE', 'ARCHIVED')),
    CONSTRAINT ck_dc_pipeline_strategy CHECK (execution_strategy IN ('AUTO', 'NATIVE', 'DATAX', 'CAMEL', 'SEATUNNEL'))
);

CREATE TABLE IF NOT EXISTS dc_pipeline_node (
    id BIGSERIAL PRIMARY KEY,
    pipeline_id BIGINT NOT NULL REFERENCES dc_pipeline(id) ON DELETE CASCADE,
    node_key VARCHAR(100) NOT NULL,
    node_type VARCHAR(100) NOT NULL,
    node_name VARCHAR(200) NOT NULL,
    x DOUBLE PRECISION NOT NULL DEFAULT 0,
    y DOUBLE PRECISION NOT NULL DEFAULT 0,
    config_json TEXT NOT NULL DEFAULT '{}',
    preferred_engine VARCHAR(32),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_dc_pipeline_node_key UNIQUE (pipeline_id, node_key)
);

CREATE TABLE IF NOT EXISTS dc_pipeline_edge (
    id BIGSERIAL PRIMARY KEY,
    pipeline_id BIGINT NOT NULL REFERENCES dc_pipeline(id) ON DELETE CASCADE,
    source_node_key VARCHAR(100) NOT NULL,
    target_node_key VARCHAR(100) NOT NULL,
    source_port VARCHAR(100),
    target_port VARCHAR(100),
    condition_json TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_dc_pipeline_edge_identity UNIQUE
        (pipeline_id, source_node_key, target_node_key, source_port, target_port)
);

CREATE INDEX IF NOT EXISTS idx_dc_pipeline_status ON dc_pipeline(status);
CREATE INDEX IF NOT EXISTS idx_dc_pipeline_node_pipeline ON dc_pipeline_node(pipeline_id, id);
CREATE INDEX IF NOT EXISTS idx_dc_pipeline_edge_pipeline ON dc_pipeline_edge(pipeline_id, id);
