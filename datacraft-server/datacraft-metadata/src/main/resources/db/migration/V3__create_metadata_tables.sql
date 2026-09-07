CREATE TABLE IF NOT EXISTS dc_dataset (
    id BIGSERIAL PRIMARY KEY,
    datasource_id BIGINT NOT NULL REFERENCES dc_datasource(id) ON DELETE CASCADE,
    catalog_name VARCHAR(255) NOT NULL DEFAULT '',
    schema_name VARCHAR(255) NOT NULL,
    table_name VARCHAR(255) NOT NULL,
    table_remark VARCHAR(1000),
    estimated_row_count BIGINT,
    collected_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_dc_dataset_identity UNIQUE (datasource_id, catalog_name, schema_name, table_name)
);

CREATE TABLE IF NOT EXISTS dc_dataset_field (
    id BIGSERIAL PRIMARY KEY,
    dataset_id BIGINT NOT NULL REFERENCES dc_dataset(id) ON DELETE CASCADE,
    field_name VARCHAR(255) NOT NULL,
    ordinal_position INTEGER NOT NULL,
    data_type VARCHAR(255) NOT NULL,
    nullable BOOLEAN NOT NULL DEFAULT TRUE,
    primary_key BOOLEAN NOT NULL DEFAULT FALSE,
    field_remark VARCHAR(1000),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_dc_dataset_field_identity UNIQUE (dataset_id, field_name)
);

CREATE INDEX IF NOT EXISTS idx_dc_dataset_datasource_schema ON dc_dataset(datasource_id, schema_name);
CREATE INDEX IF NOT EXISTS idx_dc_dataset_table_name ON dc_dataset(table_name);
CREATE INDEX IF NOT EXISTS idx_dc_dataset_field_dataset_order ON dc_dataset_field(dataset_id, ordinal_position);
